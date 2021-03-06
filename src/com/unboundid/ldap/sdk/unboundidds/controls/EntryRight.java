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
package com.unboundid.ldap.sdk.unboundidds.controls;



import java.util.EnumSet;

import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.util.StaticUtils.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This enum contains the set of possible entry-level rights that may be
 * described in an entry retrieved with the get effective rights control.
 */
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public enum EntryRight
{
  /**
   * The entry right that indicates that the user has sufficient permission to
   * add a subordinate of the target entry.
   */
  ADD("add"),



  /**
   * The entry right that indicates that the user has sufficient permission to
   * delete the target entry.
   */
  DELETE("delete"),



  /**
   * The entry right that indicates that the user has sufficient permission to
   * perform read operations with the entry.
   */
  READ("read"),



  /**
   * The entry right that indicates that the user has sufficient permission to
   * perform write operations with the entry.
   */
  WRITE("write"),



  /**
   * The entry right that indicates that the user has sufficient permission to
   * perform operations involving proxied authorization with the entry.
   */
  PROXY("proxy");



  // The name of this entry right.
  private final String name;



  /**
   * Creates a new entry right with the specified name.
   *
   * @param  name  The name for this entry right.
   */
  private EntryRight(final String name)
  {
    this.name = name;
  }



  /**
   * Retrieves the name of this entry right.
   *
   * @return  The name of this entry right.
   */
  public String getName()
  {
    return name;
  }



  /**
   * Retrieves the entry right for the specified name.
   *
   * @param  name  The name for which to retrieve the corresponding entry right.
   *
   * @return  The requested entry right, or {@code null} if there is no such
   *          right.
   */
  public static EntryRight forName(final String name)
  {
    final String lowerName = toLowerCase(name);

    for (final EntryRight r : EnumSet.allOf(EntryRight.class))
    {
      if (r.name.equals(lowerName))
      {
        return r;
      }
    }

    return null;
  }



  /**
   * Retrieves a string representation of this entry right.
   *
   * @return  A string representation of this entry right.
   */
  @Override()
  public String toString()
  {
    return name;
  }
}
