/*
 * Copyright 2015-2016 UnboundID Corp.
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
package com.unboundid.ldap.sdk.unboundidds.extensions;



import java.io.Serializable;
import java.util.StringTokenizer;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.Validator;

import static com.unboundid.ldap.sdk.unboundidds.extensions.ExtOpMessages.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class defines a data structure that will provide information about
 * notices pertaining to a user's password policy state (items that might be
 * of interest, but do not necessarily represent a current or imminent problem
 * with the account).  It includes a number of predefined notice types, but also
 * allows for the possibility of additional notice types that have not been
 * defined.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class PasswordPolicyStateAccountUsabilityNotice
       implements Serializable
{
  /**
   * The numeric value for the notice type that indicates the user has a valid
   * outstanding retired password.
   */
  public static final int NOTICE_TYPE_OUTSTANDING_RETIRED_PASSWORD = 1;



  /**
   * The name for the notice type that indicates the user user has a valid
   * outstanding retired password.
   */
  public static final String NOTICE_NAME_OUTSTANDING_RETIRED_PASSWORD =
       "outstanding-retired-password";



  /**
   * The numeric value for the notice type that indicates the user has a valid
   * outstanding one-time password.
   */
  public static final int NOTICE_TYPE_OUTSTANDING_ONE_TIME_PASSWORD = 2;



  /**
   * The name for the notice type that indicates the user has a valid
   * outstanding one-time password.
   */
  public static final String NOTICE_NAME_OUTSTANDING_ONE_TIME_PASSWORD =
       "outstanding-one-time-password";



  /**
   * The numeric value for the notice type that indicates the user has a valid
   * outstanding password reset token.
   */
  public static final int NOTICE_TYPE_OUTSTANDING_PASSWORD_RESET_TOKEN = 3;



  /**
   * The name for the notice type that indicates the user has a valid
   * outstanding password reset token that will expire in the near future.
   */
  public static final String NOTICE_NAME_OUTSTANDING_PASSWORD_RESET_TOKEN =
       "outstanding-password-reset-token";



  /**
   * The numeric value for the notice type that indicates the user is not
   * currently allowed to change his/her password because they are within the
   * minimum password age.
   */
  public static final int NOTICE_TYPE_IN_MINIMUM_PASSWORD_AGE = 4;



  /**
   * The name for the notice type that indicates the user is not currently
   * allowed to change his/her password because they are within the minimum
   * password age.
   */
  public static final  String NOTICE_NAME_IN_MINIMUM_PASSWORD_AGE =
       "in-minimum-password-age";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 3676573634197714542L;



  // The integer value for this account usability notice.
  private final int intValue;

  // A human-readable message that provides specific details about this account
  // usability notice.
  private final String message;

  // The name for this account usability notice.
  private final String name;

  // The encoded string representation for this account usability notice.
  private final String stringRepresentation;



  /**
   * Creates a new account usability notice with the provided information.
   *
   * @param  intValue  The integer value for this account usability notice.
   * @param  name      The name for this account usability notice.  It must not
   *                   be {@code null}.
   * @param  message   A human-readable message that provides specific details
   *                   about this account usability notice.  It may be
   *                   {@code null} if no message is available.
   */
  public PasswordPolicyStateAccountUsabilityNotice(final int intValue,
                                                   final String name,
                                                   final String message)
  {
    Validator.ensureNotNull(name);

    this.intValue = intValue;
    this.name = name;
    this.message = message;

    final StringBuilder buffer = new StringBuilder();
    buffer.append("code=");
    buffer.append(intValue);
    buffer.append("\tname=");
    buffer.append(name);

    if (message != null)
    {
      buffer.append("\tmessage=");
      buffer.append(message);
    }

    stringRepresentation = buffer.toString();
  }



  /**
   * Creates a new account usability notice that is decoded from the provided
   * string representation.
   *
   * @param  stringRepresentation  The string representation of the account
   *                               usability notice to decode.  It must not be
   *                               {@code null}.
   *
   * @throws LDAPException  If the provided string cannot be decoded as a valid
   *                         account usability notice.
   */
  public PasswordPolicyStateAccountUsabilityNotice(
       final String stringRepresentation)
       throws LDAPException
  {
    this.stringRepresentation = stringRepresentation;

    try
    {
      Integer i = null;
      String n = null;
      String m = null;

      final StringTokenizer tokenizer =
           new StringTokenizer(stringRepresentation, "\t");
      while (tokenizer.hasMoreTokens())
      {
        final String token = tokenizer.nextToken();
        final int equalPos = token.indexOf('=');
        final String fieldName = token.substring(0, equalPos);
        final String fieldValue = token.substring(equalPos+1);
        if (fieldName.equals("code"))
        {
          i = Integer.valueOf(fieldValue);
        }
        else if (fieldName.equals("name"))
        {
          n = fieldValue;
        }
        else if (fieldName.equals("message"))
        {
          m = fieldValue;
        }
      }

      if (i == null)
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_PWP_STATE_ACCOUNT_USABILITY_NOTICE_CANNOT_DECODE.get(
                  stringRepresentation,
                  ERR_PWP_STATE_ACCOUNT_USABILITY_NOTICE_NO_CODE.get()));
      }

      if (n == null)
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_PWP_STATE_ACCOUNT_USABILITY_NOTICE_CANNOT_DECODE.get(
                  stringRepresentation,
                  ERR_PWP_STATE_ACCOUNT_USABILITY_NOTICE_NO_NAME.get()));
      }

      intValue = i;
      name     = n;
      message  = m;
    }
    catch (final LDAPException le)
    {
      Debug.debugException(le);

      throw le;
    }
    catch (final Exception e)
    {
      Debug.debugException(e);

      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_PWP_STATE_ACCOUNT_USABILITY_NOTICE_CANNOT_DECODE.get(
                stringRepresentation, StaticUtils.getExceptionMessage(e)),
           e);
    }
  }



  /**
   * Retrieves the integer value for this account usability notice.
   *
   * @return  The integer value for this account usability notice.
   */
  public int getIntValue()
  {
    return intValue;
  }



  /**
   * Retrieves the name for this account usability notice.
   *
   * @return  The name for this account usability notice.
   */
  public String getName()
  {
    return name;
  }



  /**
   * Retrieves a human-readable message that provides specific details about
   * this account usability notice.
   *
   * @return  A human-readable message that provides specific details about this
   *          account usability notice, or {@code null} if no message is
   *          available.
   */
  public String getMessage()
  {
    return message;
  }



  /**
   * Retrieves a string representation of this account usability notice.
   *
   * @return  A string representation of this account usability notice.
   */
  @Override()
  public String toString()
  {
    return stringRepresentation;
  }
}
