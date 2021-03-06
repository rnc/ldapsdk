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



import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides a data structure that holds information about a log
 * message that may appear in the Directory Server access log about a
 * connection that has been established.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class ConnectAccessLogMessage
       extends AccessLogMessage
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 4254346309071273212L;



  // The name of the client connection policy selected for the client.
  private final String clientConnectionPolicy;

  // The name of the protocol used by the client.
  private final String protocolName;

  // The source address for the client connection.
  private final String sourceAddress;

  // The server address to which the client connection is established.
  private final String targetAddress;




  /**
   * Creates a new connect access log message from the provided message string.
   *
   * @param  s  The string to be parsed as a connect access log message.
   *
   * @throws  LogException  If the provided string cannot be parsed as a valid
   *                        log message.
   */
  public ConnectAccessLogMessage(final String s)
         throws LogException
  {
    this(new LogMessage(s));
  }



  /**
   * Creates a new connect access log message from the provided log message.
   *
   * @param  m  The log message to be parsed as a connect access log message.
   */
  public ConnectAccessLogMessage(final LogMessage m)
  {
    super(m);

    sourceAddress          = getNamedValue("from");
    targetAddress          = getNamedValue("to");
    protocolName           = getNamedValue("protocol");
    clientConnectionPolicy = getNamedValue("clientConnectionPolicy");
  }



  /**
   * Retrieves the source address for the client connection.
   *
   * @return  The source address for the client connection, or {@code null} if
   *          it is not included in the log message.
   */
  public String getSourceAddress()
  {
    return sourceAddress;
  }



  /**
   * Retrieves the server address to which the client connection is established.
   *
   * @return  The server address to which the client connection is established,
   *          or {@code null} if it is not included in the log message.
   */
  public String getTargetAddress()
  {
    return targetAddress;
  }



  /**
   * Retrieves the name of the protocol the client is using to communicate with
   * the Directory Server.
   *
   * @return  The name of the protocol the client is using to communicate with
   *          the Directory Server, or {@code null} if it is not included in the
   *          log message.
   */
  public String getProtocolName()
  {
    return protocolName;
  }



  /**
   * Retrieves the name of the client connection policy that was selected for
   * the client connection.
   *
   * @return  The name of the client connection policy that was selected for the
   *          client connection, or {@code null} if it is not included in the
   *          log message.
   */
  public String getClientConnectionPolicy()
  {
    return clientConnectionPolicy;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public AccessLogMessageType getMessageType()
  {
    return AccessLogMessageType.CONNECT;
  }
}
