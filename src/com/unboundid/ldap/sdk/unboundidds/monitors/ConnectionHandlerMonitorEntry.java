/*
 * Copyright 2008-2016 UnboundID Corp.
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
package com.unboundid.ldap.sdk.unboundidds.monitors;



import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.monitors.MonitorMessages.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class defines a monitor entry that provides general information about a
 * Directory Server connection handler.  Information that may be available in
 * a connection handler monitor entry includes:
 * <UL>
 *   <LI>The total number of connections that are established.</LI>
 *   <LI>The protocol that the connection handler uses to communicate with
 *       clients.</LI>
 *   <LI>A list of the listeners (addresses and ports on which the connection
 *       handler is listening for connections.</LI>
 *   <LI>Information about each of the connections established to the connection
 *       handler.  The information available for these connections may vary by
 *       connection handler type.</LI>
 * </UL>
 * The connection handler monitor entries provided by the server can be
 * retrieved using the {@link MonitorManager#getConnectionHandlerMonitorEntries}
 * method.  These entries provide specific methods for accessing information
 * about the connection handler (e.g., the
 * {@link ConnectionHandlerMonitorEntry#getNumConnections} method can be used
 * to retrieve the total number of connections established).  Alternately, this
 * information may be accessed using the generic API.  See the
 * {@link MonitorManager} class documentation for an example that demonstrates
 * the use of the generic API for accessing monitor data.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class ConnectionHandlerMonitorEntry
       extends MonitorEntry
{
  /**
   * The structural object class used in connection handler monitor entries.
   */
  static final String CONNECTION_HANDLER_MONITOR_OC =
       "ds-connectionhandler-monitor-entry";



  /**
   * The name of the attribute that contains information about the established
   * connections.
   */
  private static final String ATTR_CONNECTION =
       "ds-connectionhandler-connection";



  /**
   * The name of the attribute that contains information about the listeners.
   */
  private static final String ATTR_LISTENER =
       "ds-connectionhandler-listener";



  /**
   * The name of the attribute that contains information about the number of
   * established connections.
   */
  private static final String ATTR_NUM_CONNECTIONS =
       "ds-connectionhandler-num-connections";



  /**
   * The name of the attribute that contains information about the protocol.
   */
  private static final String ATTR_PROTOCOL =
       "ds-connectionhandler-protocol";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -2922139631867367609L;



  // The list of connections currently established.
  private final List<String> connections;

  // The list of listeners for the connection handler.
  private final List<String> listeners;

  // The number of connections established.
  private final Long numConnections;

  // The protocol used by the connection handler.
  private final String protocol;



  /**
   * Creates a new connection handler monitor entry from the provided entry.
   *
   * @param  entry  The entry to be parsed as a connection handler monitor
   *                entry.  It must not be {@code null}.
   */
  public ConnectionHandlerMonitorEntry(final Entry entry)
  {
    super(entry);

    connections    = getStrings(ATTR_CONNECTION);
    listeners      = getStrings(ATTR_LISTENER);
    numConnections = getLong(ATTR_NUM_CONNECTIONS);
    protocol       = getString(ATTR_PROTOCOL);
  }



  /**
   * Retrieves a list of the string representations of the connections
   * established to the associated connection handler.  Values should be
   * space-delimited name-value pairs with the values surrounded by quotation
   * marks.
   *
   * @return  A list of the string representations of the connections
   *          established to the associated connection handler, or an empty list
   *          if it was not included in the monitor entry or there are no
   *          established connections.
   */
  public List<String> getConnections()
  {
    return connections;
  }



  /**
   * Retrieves a list of the listeners for the associated connection handler.
   *
   * @return  A list of the listeners for the associated connection handler, or
   *          an empty list if it was not included in the monitor entry or the
   *          connection handler does not have any listeners.
   */
  public List<String> getListeners()
  {
    return listeners;
  }



  /**
   * Retrieves the number of connections currently established to the associated
   * connection handler.
   *
   * @return  The number of connections currently established to the associated
   *          connection handler, or {@code null} if it was not included in the
   *          monitor entry.
   */
  public Long getNumConnections()
  {
    return numConnections;
  }



  /**
   * Retrieves the protocol for the associated connection handler.
   *
   * @return  The protocol for the associated connection handler, or
   *          {@code null} if it was not included in the monitor entry.
   */
  public String getProtocol()
  {
    return protocol;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getMonitorDisplayName()
  {
    return INFO_CONNECTION_HANDLER_MONITOR_DISPNAME.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getMonitorDescription()
  {
    return INFO_CONNECTION_HANDLER_MONITOR_DESC.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public Map<String,MonitorAttribute> getMonitorAttributes()
  {
    final LinkedHashMap<String,MonitorAttribute> attrs =
         new LinkedHashMap<String,MonitorAttribute>();

    if (protocol != null)
    {
      addMonitorAttribute(attrs,
           ATTR_PROTOCOL,
           INFO_CONNECTION_HANDLER_DISPNAME_PROTOCOL.get(),
           INFO_CONNECTION_HANDLER_DESC_PROTOCOL.get(),
           protocol);
    }

    if (! listeners.isEmpty())
    {
      addMonitorAttribute(attrs,
           ATTR_LISTENER,
           INFO_CONNECTION_HANDLER_DISPNAME_LISTENER.get(),
           INFO_CONNECTION_HANDLER_DESC_LISTENER.get(),
           listeners);
    }

    if (numConnections != null)
    {
      addMonitorAttribute(attrs,
           ATTR_NUM_CONNECTIONS,
           INFO_CONNECTION_HANDLER_DISPNAME_NUM_CONNECTIONS.get(),
           INFO_CONNECTION_HANDLER_DESC_NUM_CONNECTIONS.get(),
           numConnections);
    }

    if (! connections.isEmpty())
    {
      addMonitorAttribute(attrs,
           ATTR_CONNECTION,
           INFO_CONNECTION_HANDLER_DISPNAME_CONNECTION.get(),
           INFO_CONNECTION_HANDLER_DESC_CONNECTION.get(),
           connections);
    }

    return Collections.unmodifiableMap(attrs);
  }
}
