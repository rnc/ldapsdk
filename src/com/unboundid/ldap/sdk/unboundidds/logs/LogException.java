/*
 * Copyright 2009-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2015-2016 UnboundID Corp.
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
package com.unboundid.ldap.sdk.unboundidds.logs;



import com.unboundid.util.LDAPSDKException;
import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.util.Validator.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class defines an exception that may be thrown if a problem occurs while
 * attempting to parse a log message.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class LogException
       extends LDAPSDKException
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -5936254058683765082L;



  // The malformed log message that triggered this exception.
  private final String logMessage;



  /**
   * Creates a new log exception with the provided information.
   *
   * @param  logMessage   The malformed log message string that triggered this
   *                      exception.  It must not be {@code null}.
   * @param  explanation  A message explaining the problem that occurred.  It
   *                      must not be {@code null}.
   */
  public LogException(final String logMessage, final String explanation)
  {
    this(logMessage, explanation, null);
  }



  /**
   * Creates a new log exception with the provided information.
   *
   * @param  logMessage   The malformed log message string that triggered this
   *                      exception.  It must not be {@code null}.
   * @param  explanation  A message explaining the problem that occurred.  It
   *                      must not be {@code null}.
   * @param  cause        An underlying exception that triggered this exception.
   */
  public LogException(final String logMessage, final String explanation,
                      final Throwable cause)
  {
    super(explanation, cause);

    ensureNotNull(logMessage, explanation);

    this.logMessage = logMessage;
  }



  /**
   * Retrieves the malformed log message string that triggered this exception.
   *
   * @return  The malformed log message string that triggered this exception.
   */
  public String getLogMessage()
  {
    return logMessage;
  }
}
