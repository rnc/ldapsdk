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
package com.unboundid.ldap.sdk.unboundidds.monitors;



import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides information about the health check states that may be
 * held by an LDAP external server.
 */
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public enum HealthCheckState
{
  /**
   * The health check state that indicates that the associated LDAP external
   * server is available.
   */
  AVAILABLE("available"),



  /**
   * The health check state that indicates that the associated LDAP external
   * server is in a degraded state.
   */
  DEGRADED("degraded"),



  /**
   * The health check state that indicates that the associated LDAP external
   * server is unavailable.
   */
  UNAVAILABLE("unavailable"),



  /**
   * The health check state that indicates that there are no local servers
   * defined, and therefore no health information is available for local
   * servers.
   */
  NO_LOCAL_SERVERS("no-local-servers"),



  /**
   * The health check state that indicates that there are no local servers
   * defined, and therefore no health information is available for remote
   * servers.
   */
  NO_REMOTE_SERVERS("no-remote-servers");



  // The name for this health check state.
  private final String name;



  /**
   * Creates a new health check state with the specified name.
   *
   * @param  name  The name for this health check state.
   */
  private HealthCheckState(final String name)
  {
    this.name = name;
  }



  /**
   * Retrieves the name for this health check state.
   *
   * @return  The name for this health check state.
   */
  public String getName()
  {
    return name;
  }



  /**
   * Retrieves the health check state with the specified name.
   *
   * @param  name  The name of the health check state to retrieve.
   *
   * @return  The health check state with the specified name, or {@code null} if
   *          there is no health check state with the given name.
   */
  public static HealthCheckState forName(final String name)
  {
    final String lowerName = StaticUtils.toLowerCase(name);

    if (lowerName.equals("available"))
    {
      return AVAILABLE;
    }
    else if (lowerName.equals("degraded"))
    {
      return DEGRADED;
    }
    else if (lowerName.equals("unavailable"))
    {
      return UNAVAILABLE;
    }
    else if (lowerName.equals("no-local-servers") ||
             lowerName.equals("no_local_servers"))
    {
      return NO_LOCAL_SERVERS;
    }
    else if (lowerName.equals("no-remote-servers") ||
             lowerName.equals("no_remote_servers"))
    {
      return NO_REMOTE_SERVERS;
    }
    else
    {
      return null;
    }
  }



  /**
   * Retrieves a string representation of this health check state.
   *
   * @return  A string representation of this health check state.
   */
  @Override()
  public String toString()
  {
    return name;
  }
}
