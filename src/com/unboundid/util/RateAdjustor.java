/*
 * Copyright 2014-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2014-2016 UnboundID Corp.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.util;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.unboundid.util.args.ArgumentException;
import com.unboundid.util.args.DurationArgument;

import static com.unboundid.util.Debug.*;
import static com.unboundid.util.UtilityMessages.*;



/**
 * This class allows a FixedRateBarrier to change dynamically.  The rate changes
 * are governed by lines read from a {@code Reader} (typically backed by a
 * file). The input starts with a header that provides some global options and
 * then has a list of lines, where each line contains a single rate per second,
 * a comma, and a duration to maintain that rate.  Rates are specified as an
 * absolute rate per second or as a rate relative to the base rate per second.
 * The duration is an integer followed by a time unit (ms=milliseconds,
 * s=seconds, m=minutes, h=hours, and d=days).
 * <BR><BR>
 * The following simple example will run at a target rate of 1000 per second
 * for one minute, and then 10000 per second for 10 seconds.
 * <pre>
 *   # format=rate-duration
 *   1000,1m
 *   10000,10s
 * </pre>
 * <BR>
 * The following example has a default duration of one minute, and will repeat
 * the two intervals until this RateAdjustor is shut down.  The first interval
 * is run for the default of 1 minute at two and half times the base rate, and
 * then run for 10 seconds at 10000 per second.
 * <pre>
 *   # format=rate-duration
 *   # default-duration=1m
 *   # repeat=true
 *   2.5X
 *   10000,10s
 * </pre>
 * A {@code RateAdjustor} is a daemon thread.  It is necessary to call the
 * {@code start()} method to start the thread and begin the rate changes.
 * Once this finished processing the rates, the thread will complete.
 * It can be stopped prematurely by calling {@code shutDown()}.
 * <BR><BR>
 * The header can contain the following options:
 * <UL>
 *   <LI>{@code format} (required):  This must currently have the value
 *       {@code rate-duration}.</LI>
 *   <LI>{@code default-duration} (optional):  This can specify a default
 *       duration for intervals that do not include a duration.  The format
 *       is an integer followed by a time unit as described above.</LI>
 *   <LI>{@code repeat} (optional):  If this has a value of {@code true}, then
 *       the rates in the input will be repeated until {@code shutDown()} is
 *       called.</LI>
 * </UL>
 */
@ThreadSafety(level = ThreadSafetyLevel.MOSTLY_THREADSAFE)
public final class RateAdjustor extends Thread
{
  /**
   * This starts a comment in the input.
   */
  public static final char COMMENT_START = '#';



  /**
   * The text that must appear on a line by itself in order to denote that the
   * end of the file header has been reached.
   */
  public static final String END_HEADER_TEXT = "END HEADER";



  /**
   * The header key that represents the default duration.
   */
  public static final String DEFAULT_DURATION_KEY = "default-duration";



  /**
   * The header key that represents the format of the file.
   */
  public static final String FORMAT_KEY = "format";



  /**
   * The value of the format key that represents a list of rates and durations
   * within the input file.
   */
  public static final String FORMAT_VALUE_RATE_DURATION = "rate-and-duration";



  /**
   * A list of all formats that we support.
   */
  public static final List<String> FORMATS =
       Arrays.asList(FORMAT_VALUE_RATE_DURATION);



  /**
   * The header key that represents whether the input should be repeated.
   */
  public static final String REPEAT_KEY = "repeat";



  /**
   * A list of all header keys that we support.
   */
  public static final List<String> KEYS =
       Arrays.asList(DEFAULT_DURATION_KEY, FORMAT_KEY, REPEAT_KEY);



  // Other headers to consider:
  // * rate-multiplier, so you can easily proportionally increase or decrease
  //   every target rate without changing all the target rates directly.
  // * duration-multiplier, so you can easily proportionally increase or
  //   decrease the length of time to spend at target rates.
  // * rate-change-behavior, so you can specify the behavior that should be
  //   exhibited when transitioning from one rate to another (e.g., instant
  //   jump, linear acceleration, sine-based acceleration, etc.).
  // * jitter, so we can introduce some amount of random jitter in the target
  //   rate (in which the actual target rate may be frequently adjusted to be
  //   slightly higher or lower than the designated target rate).
  // * spike, so we can introduce periodic, substantial increases in the target
  //   rate.



  // The barrier whose rate is adjusted.
  private final FixedRateBarrier barrier;

  // A list of rates per second and the number of milliseconds that the
  // specified rate should be maintained.
  private final List<ObjectPair<Double,Long>> ratesAndDurations;

  // If this is true, then the ratesAndDurations will be repeated until this is
  // shut down.
  private final boolean repeat;

  // Set to true when this should shut down.
  private volatile boolean shutDown = false;

  // This is used to make sure we set the initial rate before start() returns.
  private final CountDownLatch initialRateSetLatch = new CountDownLatch(1);

  // This allows us to interrupt when we are sleeping.
  private final WakeableSleeper sleeper = new WakeableSleeper();



  /**
   * Returns a new RateAdjustor with the specified parameters.  See the
   * class-level javadoc for more information.
   *
   * @param  barrier            The barrier to update based on the specified
   *                            rates.
   * @param  baseRatePerSecond  The baseline rate per second, or {@code null}
   *                            if none was specified.
   * @param  rates              A file containing a list of rates and durations
   *                            as described in the class-level javadoc.
   *
   * @return  A new RateAdjustor constructed from the specified parameters.
   *
   * @throws  IOException               If there is a problem reading from
   *                                    the rates Reader.
   * @throws  IllegalArgumentException  If there is a problem with the rates
   *                                    input.
   */
  public static RateAdjustor newInstance(final FixedRateBarrier barrier,
                                         final Integer baseRatePerSecond,
                                         final File rates)
         throws IOException, IllegalArgumentException
  {
    final Reader reader = new FileReader(rates);
    return new RateAdjustor(
         barrier,
         (baseRatePerSecond == null) ? 0 : baseRatePerSecond,
         reader);
  }



  /**
   * Retrieves a string that may be used as the description of the argument that
   * specifies the path to a variable rate data file for use in conjunction with
   * this rate adjustor.
   *
   * @param  genArgName  The name of the argument that may be used to generate a
   *                     sample variable rate data file.
   *
   * @return   A string that may be used as the description of the argument that
   *           specifies the path to a variable rate data file for use in
   *           conjunction with this rate adjustor.
   */
  public static String getVariableRateDataArgumentDescription(
                            final String genArgName)
  {
    return INFO_RATE_ADJUSTOR_VARIABLE_RATE_DATA_ARG_DESCRIPTION.get(
         genArgName);
  }



  /**
   * Retrieves a string that may be used as the description of the argument that
   * generates a sample variable rate data file that serves as documentation of
   * the variable rate data format.
   *
   * @param  dataFileArgName  The name of the argument that specifies the path
   *                          to a file
   *
   * @return   A string that may be used as the description of the argument that
   *           generates a sample variable rate data file that serves as
   *           documentation of the variable rate data format.
   */
  public static String getGenerateSampleVariableRateFileDescription(
                            final String dataFileArgName)
  {
    return INFO_RATE_ADJUSTOR_GENERATE_SAMPLE_RATE_FILE_ARG_DESCRIPTION.get(
         dataFileArgName);
  }



  /**
   * Writes a sample variable write data file to the specified location.
   *
   * @param  f  The path to the file to be written.
   *
   * @throws  IOException  If a problem is encountered while writing to the
   *                       specified file.
   */
  public static void writeSampleVariableRateFile(final File f)
         throws IOException
  {
    final PrintWriter w = new PrintWriter(f);
    try
    {
      w.println("# This is an example variable rate data file.  All blank " +
           "lines will be ignored.");
      w.println("# All lines starting with the '#' character are considered " +
           "comments and will");
      w.println("# also be ignored.");
      w.println();
      w.println("# The beginning of the file must be a header containing " +
           "properties pertaining");
      w.println("# to the variable rate data.  All headers must be in the " +
           "format 'name=value',");
      w.println("# in which any spaces surrounding the equal sign will be " +
           "ignored.");
      w.println();
      w.println("# The first header should be the 'format' header, which " +
           "specifies the format");
      w.println("# for the variable rate data file.  This header is " +
           "required.  At present, the");
      w.println("# only supported format is 'rate-and-duration', although " +
           "additional formats may");
      w.println("# be added in the future.");
      w.println("format = rate-and-duration");
      w.println();
      w.println("# The optional 'default-duration' header may be used to " +
           "specify a duration that");
      w.println("# will be used for any interval that does not explicitly " +
           "specify a duration.");
      w.println("# The duration must consist of a positive integer value " +
           "followed by a time");
      w.println("# unit (with zero or more spaces separating the integer " +
           "value from the unit).");
      w.println("# The supported time units are:");
      w.println("#");
      w.println("# - nanoseconds, nanosecond, nanos, nano, ns");
      w.println("# - microseconds, microseconds, micros, micro, us");
      w.println("# - milliseconds, millisecond, millis, milli, ms");
      w.println("# - seconds, second, secs, sec, s");
      w.println("# - minutes, minute, mins, min, m");
      w.println("# - hours, hour, hrs, hr, h");
      w.println("# - days, day, d");
      w.println("#");
      w.println("# If no 'default-duration' header is present, then every " +
           "data interval must");
      w.println("# include an explicitly-specified duration.");
      w.println("default-duration = 10 seconds");
      w.println();
      w.println("# The optional 'repeat' header may be used to indicate how " +
           "the tool should");
      w.println("# behave once the end of the variable rate data definitions " +
           "has been reached.");
      w.println("# If the 'repeat' header is present with a value of 'true', " +
           "then the tool will");
      w.println("# operate in an endless loop, returning to the beginning of " +
           "the variable rate");
      w.println("# definitions once the end has been reached.  If the " +
           "'repeat' header is present");
      w.println("# with a value of 'false', or if the 'repeat' header is " +
           "absent, then the tool");
      w.println("# will exit after it has processed all of the variable " +
           "rate definitions.");
      w.println("repeat = true");
      w.println();
      w.println("# After all header properties have been specified, the end " +
           "of the header must");
      w.println("# be signified with a line containing only the text 'END " +
           "HEADER'.");
      w.println("END HEADER");
      w.println();
      w.println();
      w.println("# After the header is complete, the variable rate " +
           "definitions should be");
      w.println("# provided.  Each definition should be given on a line by " +
           "itself, and should");
      w.println("# contain a target rate per second and an optional length " +
           "of time to maintain");
      w.println("# that rate.");
      w.println("#");
      w.println("# The target rate must always be present in a variable " +
           "rate definition.  It may");
      w.println("# be either a positive integer value that specifies the " +
           "absolute target rate");
      w.println("# per second (e.g., a value of '1000' indicates a target " +
           "rate of 1000");
      w.println("# operations per second), or it may be a floating-point " +
           "value followed by the");
      w.println("# letter 'x' to indicate that it is a multiplier of the " +
           "value specified by the");
      w.println("# '--ratePerSecond' argument (e.g., if the " +
           "'--ratePerSecond' argument is");
      w.println("# present with a value of 1000, then a target rate value " +
           "of '0.75x' indicates a");
      w.println("# target rate that is 75% of the '--ratePerSecond' value, " +
           "or 750 operations per");
      w.println("# second).  If the latter format is used, then the " +
           "'--ratePerSecond' argument");
      w.println("# must be provided.");
      w.println("#");
      w.println("# The duration may optionally be present in a variable " +
           "rate definition.  If");
      w.println("# present, it must be separated from the target rate by a " +
           "comma (and there may");
      w.println("# be zero or more spaces on either side of the comma).  " +
           "The duration must be in");
      w.println("# the same format as specified in the description of the " +
           "'default-duration'");
      w.println("# header above (i.e., a positive integer followed by a " +
           "time unit).  If a");
      w.println("# variable rate definition does not include a duration, " +
           "then the");
      w.println("# 'default-duration' header must have been specified, and " +
           "that default duration");
      w.println("# will be used for that variable rate definition.");
      w.println("#");
      w.println("# The following variable rate definitions may be used to " +
           "stairstep the target");
      w.println("# rate from 1000 operations per second to 10000 operations " +
           "per second, in");
      w.println("# increments of 1000 operations per second, spending one " +
           "minute at each level.");
      w.println("# If the 'repeat' header is present with a value of 'true', " +
           "then the process");
      w.println("# will start back over at 1000 operations per second after " +
           "completing one");
      w.println("# minute at 10000 operations per second.  Otherwise, the " +
           "tool will exit after");
      w.println("# completing the 10000 operation-per-second interval.");
      w.println("1000, 1 minute");
      w.println("2000, 1 minute");
      w.println("3000, 1 minute");
      w.println("4000, 1 minute");
      w.println("5000, 1 minute");
      w.println("6000, 1 minute");
      w.println("7000, 1 minute");
      w.println("8000, 1 minute");
      w.println("9000, 1 minute");
      w.println("10000, 1 minute");
      w.println();
    }
    finally
    {
      w.close();
    }
  }



  /**
   * Constructs a new RateAdjustor with the specified parameters.  See the
   * class-level javadoc for more information.
   *
   * @param  barrier            The barrier to update based on the specified
   *                            rates.
   * @param  baseRatePerSecond  The baseline rate per second, or 0 if none was
   *                            specified.
   * @param  rates              A list of rates and durations as described in
   *                            the class-level javadoc.  The reader will
   *                            always be closed before this method returns.
   *
   * @throws  IOException               If there is a problem reading from
   *                                    the rates Reader.
   * @throws  IllegalArgumentException  If there is a problem with the rates
   *                                    input.
   */
  public RateAdjustor(final FixedRateBarrier barrier,
                      final long baseRatePerSecond,
                      final Reader rates)
         throws IOException, IllegalArgumentException
  {
    // Read the header first.
    final List<String> lines;
    try
    {
      Validator.ensureNotNull(barrier, rates);
      setDaemon(true);
      this.barrier = barrier;

      lines = readLines(rates);
    }
    finally
    {
      rates.close();
    }

    final Map<String,String> header = consumeHeader(lines);

    final Set<String> invalidKeys = new LinkedHashSet<String>(header.keySet());
    invalidKeys.removeAll(KEYS);
    if (! invalidKeys.isEmpty())
    {
      throw new IllegalArgumentException(
           ERR_RATE_ADJUSTOR_INVALID_KEYS.get(invalidKeys, KEYS));
    }

    final String format = header.get(FORMAT_KEY);
    if (format == null)
    {
      throw new IllegalArgumentException(ERR_RATE_ADJUSTOR_MISSING_FORMAT.get(
           FORMAT_KEY, FORMATS, COMMENT_START));
    }

    if (! format.equals(FORMAT_VALUE_RATE_DURATION))
    {
      // For now this is the only format that we support.
      throw new IllegalArgumentException(
           ERR_RATE_ADJUSTOR_INVALID_FORMAT.get(format, FORMAT_KEY, FORMATS));
    }

    repeat = Boolean.parseBoolean(header.get(REPEAT_KEY));

    // This will be non-zero if it's set in the input.
    long defaultDurationMillis = 0;
    final String defaultDurationStr = header.get(DEFAULT_DURATION_KEY);
    if (defaultDurationStr != null)
    {
      try
      {
        defaultDurationMillis = DurationArgument.parseDuration(
             defaultDurationStr, TimeUnit.MILLISECONDS);
      }
      catch (final ArgumentException e)
      {
        debugException(e);
        throw new IllegalArgumentException(
             ERR_RATE_ADJUSTOR_INVALID_DEFAULT_DURATION.get(
                        defaultDurationStr, e.getExceptionMessage()),
             e);
      }
    }

    // Now parse out the rates and durations, which will look like this:
    //  1000,1s
    //  1.5,1d
    //  0.5X, 1m
    //  # Duration can be omitted if default-duration header was included.
    //  1000
    final List<ObjectPair<Double,Long>> ratesAndDurationList =
            new ArrayList<ObjectPair<Double,Long>>(10);
    final Pattern splitPattern = Pattern.compile("\\s*,\\s*");
    for (final String fullLine: lines)
    {
      // Strip out comments and white space.
      String line = fullLine;
      final int commentStart = fullLine.indexOf(COMMENT_START);
      if (commentStart >= 0)
      {
        line = line.substring(0, commentStart);
      }
      line = line.trim();

      if (line.length() == 0)
      {
        continue;
      }

      final String[] fields = splitPattern.split(line);
      if (!((fields.length == 2) ||
            ((fields.length == 1) && defaultDurationMillis != 0)))
      {
        throw new IllegalArgumentException(ERR_RATE_ADJUSTOR_INVALID_LINE.get(
             fullLine, DEFAULT_DURATION_KEY));
      }

      String rateStr = fields[0];

      boolean isRateMultiplier = false;
      if (rateStr.endsWith("X") || rateStr.endsWith("x"))
      {
        rateStr = rateStr.substring(0, rateStr.length() - 1).trim();
        isRateMultiplier = true;
      }

      double rate;
      try
      {
        rate = Double.parseDouble(rateStr);
      }
      catch (final NumberFormatException e)
      {
        debugException(e);
        throw new IllegalArgumentException(
             ERR_RATE_ADJUSTOR_INVALID_RATE.get(rateStr, fullLine), e);
      }

      // Values that look like 2X are a multiplier on the base rate.
      if (isRateMultiplier)
      {
        if (baseRatePerSecond <= 0)
        {
          throw new IllegalArgumentException(
                  ERR_RATE_ADJUSTOR_RELATIVE_RATE_WITHOUT_BASELINE.get(
                          rateStr, fullLine));
        }

        rate *= baseRatePerSecond;
      }

      final long durationMillis;
      if (fields.length < 2)
      {
        durationMillis = defaultDurationMillis;
      }
      else
      {
        final String duration = fields[1];
        try
        {
          durationMillis = DurationArgument.parseDuration(
                  duration, TimeUnit.MILLISECONDS);
        }
        catch (final ArgumentException e)
        {
          debugException(e);
          throw new IllegalArgumentException(
               ERR_RATE_ADJUSTOR_INVALID_DURATION.get(duration, fullLine,
                    e.getExceptionMessage()),
               e);
        }
      }

      ratesAndDurationList.add(
           new ObjectPair<Double,Long>(rate, durationMillis));
    }
    ratesAndDurations = Collections.unmodifiableList(ratesAndDurationList);
  }



  /**
   * Starts this thread and waits for the initial rate to be set.
   */
  @Override
  public void start()
  {
    super.start();

    // Wait until the initial rate is set.  Assuming the caller starts this
    // RateAdjustor before the FixedRateBarrier is used by other threads,
    // this will guarantee that the initial rate is in place before the
    // barrier is used.
    try
    {
      initialRateSetLatch.await();
    }
    catch (final InterruptedException e)
    {
      debugException(e);
    }
  }



  /**
   * Adjusts the rate in FixedRateBarrier as described in the rates.
   */
  @Override
  public void run()
  {
    try
    {
      if (ratesAndDurations.isEmpty())
      {
        return;
      }

      do
      {
        final List<ObjectPair<Double,Long>> ratesAndEndTimes =
             new ArrayList<ObjectPair<Double,Long>>(ratesAndDurations.size());
        long endTime = System.currentTimeMillis();
        for (final ObjectPair<Double,Long> rateAndDuration : ratesAndDurations)
        {
          endTime += rateAndDuration.getSecond();
          ratesAndEndTimes.add(new ObjectPair<Double,Long>(
               rateAndDuration.getFirst(), endTime));
        }

        for (final ObjectPair<Double,Long> rateAndEndTime: ratesAndEndTimes)
        {
          if (shutDown)
          {
            return;
          }

          final double rate = rateAndEndTime.getFirst();
          final long intervalMillis = barrier.getTargetRate().getFirst();
          final int perInterval = calculatePerInterval(intervalMillis, rate);

          barrier.setRate(intervalMillis, perInterval);

          // Signal start() that we've set the initial rate.
          if (initialRateSetLatch.getCount() > 0)
          {
            initialRateSetLatch.countDown();
          }

          // Hold at this rate for the specified duration.
          final long durationMillis =
               rateAndEndTime.getSecond() - System.currentTimeMillis();
          if (durationMillis > 0L)
          {
            sleeper.sleep(durationMillis);
          }
        }
      }
      while (repeat);
    }
    finally
    {
      // Just in case we happened to be shutdown before we were started.
      // We still want start() to be able to return.
      if (initialRateSetLatch.getCount() > 0)
      {
        initialRateSetLatch.countDown();
      }
    }
  }



  /**
   * Signals this to shut down.
   */
  public void shutDown()
  {
    shutDown = true;
    sleeper.wakeup();
  }



  /**
   * Returns the of rates and durations.  This is primarily here for testing
   * purposes.
   *
   * @return  The list of rates and durations.
   */
  List<ObjectPair<Double,Long>> getRatesAndDurations()
  {
    return ratesAndDurations;
  }



  /**
   * Calculates the rate per interval given the specified interval width
   * and the target rate per second.  (This is static and non-private so that
   * it can be unit tested.)
   *
   * @param intervalDurationMillis  The duration of the interval in
   *                                milliseconds.
   * @param ratePerSecond           The target rate per second.
   *
   * @return  The rate per interval, which will be at least 1.
   */
  static int calculatePerInterval(final long intervalDurationMillis,
                                  final double ratePerSecond)
  {
    final double intervalDurationSeconds = intervalDurationMillis / 1000.0;
    final double ratePerInterval = ratePerSecond * intervalDurationSeconds;
    return (int)Math.max(1, Math.round(ratePerInterval));
  }



  /**
   * This reads the header at the start of the file.  All blank lines and
   * comment lines will be ignored.  The end of the header will be signified by
   * a line containing only the text "END HEADER".  All non-blank, non-comment
   * lines in the header must be in the format "name=value", where there may be
   * zero or more spaces on either side of the equal sign, the name must not
   * contain either the space or the equal sign character, and the value must
   * not begin or end with a space.  Header lines must not contain partial-line
   * comments.
   *
   * @param  lines  The lines of input that include the header.
   *
   * @return  A map of key/value pairs extracted from the header.
   *
   * @throws  IllegalArgumentException  If a problem is encountered while
   *                                    parsing the header (e.g., a malformed
   *                                    header line is encountered, multiple
   *                                    headers have the same key, there is no
   *                                    end of header marker, etc.).
   */
  static Map<String,String> consumeHeader(final List<String> lines)
         throws IllegalArgumentException
  {
    // The header will look like this:
    // key1=value1
    // key2 = value2
    // END HEADER
    boolean endHeaderFound = false;
    final Map<String,String> headerMap = new LinkedHashMap<String,String>(3);
    final Iterator<String> lineIter = lines.iterator();
    while (lineIter.hasNext())
    {
      final String line = lineIter.next().trim();
      lineIter.remove();

      if ((line.length() == 0) ||
           line.startsWith(String.valueOf(COMMENT_START)))
      {
        continue;
      }

      if (line.equalsIgnoreCase(END_HEADER_TEXT))
      {
        endHeaderFound = true;
        break;
      }

      final int equalPos = line.indexOf('=');
      if (equalPos < 0)
      {
        throw new IllegalArgumentException(
             ERR_RATE_ADJUSTOR_HEADER_NO_EQUAL.get(line));
      }

      final String key = line.substring(0, equalPos).trim();
      if (key.length() == 0)
      {
        throw new IllegalArgumentException(
             ERR_RATE_ADJUSTOR_HEADER_EMPTY_KEY.get(line));
      }

      final String newValue = line.substring(equalPos+1).trim();
      final String existingValue = headerMap.get(key);
      if (existingValue != null)
      {
        throw new IllegalArgumentException(
             ERR_RATE_ADJUSTOR_DUPLICATE_HEADER_KEY.get(key, existingValue,
                  newValue));
      }

      headerMap.put(key, newValue);
    }

    if (! endHeaderFound)
    {
      // This means we iterated across all lines without finding the end header
      // marker.
      throw new IllegalArgumentException(
           ERR_RATE_ADJUSTOR_NO_END_HEADER_FOUND.get(END_HEADER_TEXT));
    }

    return headerMap;
  }



  /**
   * Returns a list of the lines read from the specified Reader.
   *
   * @param  reader  The Reader to read from.
   *
   * @return  A list of the lines read from the specified Reader.
   *
   * @throws  IOException  If there is a problem reading from the Reader.
   */
  private static List<String> readLines(final Reader reader) throws IOException
  {
    final BufferedReader bufferedReader = new BufferedReader(reader);

    // We remove items from the front of the list, so a linked list works best.
    final List<String> lines = new LinkedList<String>();

    String line;
    while ((line = bufferedReader.readLine()) != null)
    {
      lines.add(line);
    }

    return lines;
  }
}

