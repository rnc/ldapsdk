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



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.logs.LogMessages.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides a mechanism for reading message from a Directory Server
 * access log.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class AccessLogReader
{
  // The reader used to read the contents of the log file.
  private final BufferedReader reader;



  /**
   * Creates a new access log reader that will read messages from the specified
   * log file.
   *
   * @param  path  The path of the log file to read.
   *
   * @throws  IOException  If a problem occurs while opening the file for
   *                       reading.
   */
  public AccessLogReader(final String path)
         throws IOException
  {
    reader = new BufferedReader(new FileReader(path));
  }



  /**
   * Creates a new access log reader that will read messages from the specified
   * log file.
   *
   * @param  file  The log file to read.
   *
   * @throws  IOException  If a problem occurs while opening the file for
   *                       reading.
   */
  public AccessLogReader(final File file)
         throws IOException
  {
    reader = new BufferedReader(new FileReader(file));
  }



  /**
   * Creates a new access log reader that will read messages using the provided
   * {@code Reader} object.
   *
   * @param  reader  The reader to use to read log messages.
   */
  public AccessLogReader(final Reader reader)
  {
    if (reader instanceof BufferedReader)
    {
      this.reader = (BufferedReader) reader;
    }
    else
    {
      this.reader = new BufferedReader(reader);
    }
  }



  /**
   * Reads the next access log message from the log file.
   *
   * @return  The access log message read from the log file, or {@code null} if
   *          there are no more messages to be read.
   *
   * @throws  IOException  If an error occurs while trying to read from the
   *                       file.
   *
   * @throws  LogException  If an error occurs while trying to parse the log
   *                        message.
   */
  public AccessLogMessage read()
         throws IOException, LogException
  {
    while (true)
    {
      final String line = reader.readLine();
      if (line == null)
      {
        return null;
      }

      if ((line.length() == 0) || (line.charAt(0) == '#'))
      {
        continue;
      }

      return parse(line);
    }
  }



  /**
   * Parses the provided string as an access log message.
   *
   * @param  s  The string to parse as an access log message.
   *
   * @return  The parsed access log message.
   *
   * @throws  LogException  If an error occurs while trying to parse the log
   *                        message.
   */
  public static AccessLogMessage parse(final String s)
         throws LogException
  {
    final LogMessage m = new LogMessage(s);
    if (m.hasUnnamedValue(AccessLogMessageType.CONNECT.getLogIdentifier()))
    {
      return new ConnectAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.DISCONNECT.
                  getLogIdentifier()))
    {
      return new DisconnectAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.CLIENT_CERTIFICATE.
                  getLogIdentifier()))
    {
      return new ClientCertificateAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.SECURITY_NEGOTIATION.
                  getLogIdentifier()))
    {
      return new SecurityNegotiationAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.ENTRY_REBALANCING_REQUEST.
                  getLogIdentifier()))
    {
      return new EntryRebalancingRequestAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.ENTRY_REBALANCING_RESULT.
                  getLogIdentifier()))
    {
      return new EntryRebalancingResultAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.REQUEST.
                  getLogIdentifier()))
    {
      if (m.hasUnnamedValue(AccessLogOperationType.ABANDON.
               getLogIdentifier()))
      {
        return new AbandonRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.ADD.
                    getLogIdentifier()))
      {
        return new AddRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.BIND.
                    getLogIdentifier()))
      {
        return new BindRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.COMPARE.
                    getLogIdentifier()))
      {
        return new CompareRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.DELETE.
                    getLogIdentifier()))
      {
        return new DeleteRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.EXTENDED.
                    getLogIdentifier()))
      {
        return new ExtendedRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODIFY.
                    getLogIdentifier()))
      {
        return new ModifyRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODDN.
                    getLogIdentifier()))
      {
        return new ModifyDNRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.SEARCH.
                    getLogIdentifier()))
      {
        return new SearchRequestAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.UNBIND.
                    getLogIdentifier()))
      {
        return new UnbindRequestAccessLogMessage(m);
      }
      else
      {
        throw new LogException(s,
             ERR_LOG_MESSAGE_INVALID_REQUEST_OPERATION_TYPE.get());
      }
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.RESULT.
                  getLogIdentifier()))
    {
      if (m.hasUnnamedValue(AccessLogOperationType.ABANDON.
               getLogIdentifier()))
      {
        return new AbandonResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.ADD.
                    getLogIdentifier()))
      {
        return new AddResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.BIND.
                    getLogIdentifier()))
      {
        return new BindResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.COMPARE.
                    getLogIdentifier()))
      {
        return new CompareResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.DELETE.
                    getLogIdentifier()))
      {
        return new DeleteResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.EXTENDED.
                    getLogIdentifier()))
      {
        return new ExtendedResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODIFY.
                    getLogIdentifier()))
      {
        return new ModifyResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODDN.
                    getLogIdentifier()))
      {
        return new ModifyDNResultAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.SEARCH.
                    getLogIdentifier()))
      {
        return new SearchResultAccessLogMessage(m);
      }
      else
      {
        throw new LogException(s,
             ERR_LOG_MESSAGE_INVALID_RESULT_OPERATION_TYPE.get());
      }
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.FORWARD.
                  getLogIdentifier()))
    {
      if (m.hasUnnamedValue(AccessLogOperationType.ABANDON.
               getLogIdentifier()))
      {
        return new AbandonForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.ADD.
                    getLogIdentifier()))
      {
        return new AddForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.BIND.
                    getLogIdentifier()))
      {
        return new BindForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.COMPARE.
                    getLogIdentifier()))
      {
        return new CompareForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.DELETE.
                    getLogIdentifier()))
      {
        return new DeleteForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.EXTENDED.
                    getLogIdentifier()))
      {
        return new ExtendedForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODIFY.
                    getLogIdentifier()))
      {
        return new ModifyForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODDN.
                    getLogIdentifier()))
      {
        return new ModifyDNForwardAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.SEARCH.
                    getLogIdentifier()))
      {
        return new SearchForwardAccessLogMessage(m);
      }
      else
      {
        throw new LogException(s,
             ERR_LOG_MESSAGE_INVALID_FORWARD_OPERATION_TYPE.get());
      }
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.FORWARD_FAILED.
                  getLogIdentifier()))
    {
      if (m.hasUnnamedValue(AccessLogOperationType.ADD.getLogIdentifier()))
      {
        return new AddForwardFailedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.BIND.
                    getLogIdentifier()))
      {
        return new BindForwardFailedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.COMPARE.
                    getLogIdentifier()))
      {
        return new CompareForwardFailedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.DELETE.
                    getLogIdentifier()))
      {
        return new DeleteForwardFailedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.EXTENDED.
                    getLogIdentifier()))
      {
        return new ExtendedForwardFailedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODIFY.
                    getLogIdentifier()))
      {
        return new ModifyForwardFailedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODDN.
                    getLogIdentifier()))
      {
        return new ModifyDNForwardFailedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.SEARCH.
                    getLogIdentifier()))
      {
        return new SearchForwardFailedAccessLogMessage(m);
      }
      else
      {
        throw new LogException(s,
             ERR_LOG_MESSAGE_INVALID_FORWARD_FAILED_OPERATION_TYPE.get());
      }
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.ASSURANCE_COMPLETE.
                  getLogIdentifier()))
    {
      if (m.hasUnnamedValue(AccessLogOperationType.ADD.getLogIdentifier()))
      {
        return new AddAssuranceCompletedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.DELETE.
                    getLogIdentifier()))
      {
        return new DeleteAssuranceCompletedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODIFY.
                    getLogIdentifier()))
      {
        return new ModifyAssuranceCompletedAccessLogMessage(m);
      }
      else if (m.hasUnnamedValue(AccessLogOperationType.MODDN.
                    getLogIdentifier()))
      {
        return new ModifyDNAssuranceCompletedAccessLogMessage(m);
      }
      else
      {
        throw new LogException(s,
             ERR_LOG_MESSAGE_INVALID_ASSURANCE_COMPLETE_OPERATION_TYPE.get());
      }
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.ENTRY.getLogIdentifier()))
    {
      return new SearchEntryAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.REFERENCE.
                  getLogIdentifier()))
    {
      return new SearchReferenceAccessLogMessage(m);
    }
    else if (m.hasUnnamedValue(AccessLogMessageType.INTERMEDIATE_RESPONSE.
                  getLogIdentifier()))
    {
      return new IntermediateResponseAccessLogMessage(m);
    }
    else
    {
      throw new LogException(s,
           ERR_LOG_MESSAGE_INVALID_ACCESS_MESSAGE_TYPE.get());
    }
  }



  /**
   * Closes this error log reader.
   *
   * @throws  IOException  If a problem occurs while closing the reader.
   */
  public void close()
         throws IOException
  {
    reader.close();
  }
}
