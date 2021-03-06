/*
 * Copyright 2012-2016 UnboundID Corp.
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



import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.controls.ControlMessages.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides a request control which may be used to request that the
 * server return resource limit information for the authenticated user in the
 * response to a successful bind operation.  Resource limits that may be
 * returned include custom size limit, time limit, idle time limit, lookthrough
 * limit, equivalent authorization user DN, client connection policy name, and
 * privilege names.
 * <BR><BR>
 * This control does not have a value.  The criticality may be either
 * {@code true} or {@code false}.
 *
 * @see GetUserResourceLimitsResponseControl
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class GetUserResourceLimitsRequestControl
       extends Control
{
  /**
   * The OID (1.3.6.1.4.1.30221.2.5.25) for the get user resource limits request
   * control.
   */
  public static final String GET_USER_RESOURCE_LIMITS_REQUEST_OID =
       "1.3.6.1.4.1.30221.2.5.25";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 3355139762944763749L;



  /**
   * Creates a new get user resource limits request control.  It will not be
   * marked critical.
   */
  public GetUserResourceLimitsRequestControl()
  {
    this(false);
  }



  /**
   * Creates a new get user resource limits request control with the specified
   * criticality.
   *
   * @param  isCritical  Indicates whether this control should be marked
   *                     critical.
   */
  public GetUserResourceLimitsRequestControl(final boolean isCritical)
  {
    super(GET_USER_RESOURCE_LIMITS_REQUEST_OID, isCritical,  null);
  }



  /**
   * Creates a new get user resource limits request control which is decoded
   * from the provided generic control.
   *
   * @param  control  The generic control to be decoded as a get user resource
   *                  limits request control.
   *
   * @throws  LDAPException  If the provided control cannot be decoded as a get
   *                         user resource limits request control.
   */
  public GetUserResourceLimitsRequestControl(final Control control)
         throws LDAPException
  {
    super(control);

    if (control.hasValue())
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_GET_USER_RESOURCE_LIMITS_REQUEST_HAS_VALUE.get());
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getControlName()
  {
    return INFO_CONTROL_NAME_GET_USER_RESOURCE_LIMITS_REQUEST.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("GetUserResourceLimitsRequestControl(isCritical=");
    buffer.append(isCritical());
    buffer.append(')');
  }
}
