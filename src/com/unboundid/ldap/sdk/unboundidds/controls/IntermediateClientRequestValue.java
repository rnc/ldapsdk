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



import java.io.Serializable;
import java.util.ArrayList;

import com.unboundid.asn1.ASN1Boolean;
import com.unboundid.asn1.ASN1Constants;
import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.controls.ControlMessages.*;
import static com.unboundid.util.Debug.*;
import static com.unboundid.util.StaticUtils.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class implements a data structure which encapsulates the value of an
 * intermediate client request value.  It may recursively embed intermediate
 * client request values from downstream clients.
 * <BR><BR>
 * See the documentation in the {@link IntermediateClientRequestControl} class
 * for an example of using the intermediate client request and response
 * controls.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class IntermediateClientRequestValue
       implements Serializable
{
  /**
   * The BER type for the downstreamRequest element.
   */
  private static final byte TYPE_DOWNSTREAM_REQUEST = (byte) 0xA0;



  /**
   * The BER type for the downstreamClientAddress element.
   */
  private static final byte TYPE_DOWNSTREAM_CLIENT_ADDRESS = (byte) 0x81;



  /**
   * The BER type for the downstreamClientSecure element.
   */
  private static final byte TYPE_DOWNSTREAM_CLIENT_SECURE = (byte) 0x82;



  /**
   * The BER type for the clientIdentity element.
   */
  private static final byte TYPE_CLIENT_IDENTITY = (byte) 0x83;



  /**
   * The BER type for the clientName element.
   */
  private static final byte TYPE_CLIENT_NAME = (byte) 0x84;



  /**
   * The BER type for the clientSessionID element.
   */
  private static final byte TYPE_CLIENT_SESSION_ID = (byte) 0x85;



  /**
   * The BER type for the clientRequestID element.
   */
  private static final byte TYPE_CLIENT_REQUEST_ID = (byte) 0x86;



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -794887520013838259L;



  // Indicates whether the communication with the downstream client is secure.
  private final Boolean downstreamClientSecure;

  // The downstream request value, if present.
  private final IntermediateClientRequestValue downstreamRequest;

  // The requested client authorization identity, if present.
  private final String clientIdentity;

  // The downstream client address, if present.
  private final String downstreamClientAddress;

  // The client name, which describes the client application, if present.
  private final String clientName;

  // The client request ID, if present.
  private final String clientRequestID;

  // The client session ID, if present.
  private final String clientSessionID;



  /**
   * Creates a new intermediate client request value with the provided
   * information.
   *
   * @param  downstreamRequest        A wrapped intermediate client request from
   *                                  a downstream client.  It may be
   *                                  {@code null} if there is no downstream
   *                                  request.
   * @param  downstreamClientAddress  The IP address or resolvable name of the
   *                                  downstream client system.  It may be
   *                                  {@code null} if there is no downstream
   *                                  client or its address is not available.
   * @param  downstreamClientSecure   Indicates whether communication with the
   *                                  downstream client is secure.  It may be
   *                                  {@code null} if there is no downstream
   *                                  client or it is not known whether the
   *                                  communication is secure.
   * @param  clientIdentity           The requested client authorization
   *                                  identity.  It may be {@code null} if there
   *                                  is no requested authorization identity.
   * @param  clientName               An identifier string that summarizes the
   *                                  client application that created this
   *                                  intermediate client request.  It may be
   *                                  {@code null} if that information is not
   *                                  available.
   * @param  clientSessionID          A string that may be used to identify the
   *                                  session in the client application.  It may
   *                                  be {@code null} if there is no available
   *                                  session identifier.
   * @param  clientRequestID          A string that may be used to identify the
   *                                  request in the client application.  It may
   *                                  be {@code null} if there is no available
   *                                  request identifier.
   */
  public IntermediateClientRequestValue(
              final IntermediateClientRequestValue downstreamRequest,
              final String downstreamClientAddress,
              final Boolean downstreamClientSecure, final String clientIdentity,
              final String clientName, final String clientSessionID,
              final String clientRequestID)
  {
    this.downstreamRequest       = downstreamRequest;
    this.downstreamClientAddress = downstreamClientAddress;
    this.downstreamClientSecure  = downstreamClientSecure;
    this.clientIdentity          = clientIdentity;
    this.clientName              = clientName;
    this.clientSessionID         = clientSessionID;
    this.clientRequestID         = clientRequestID;
  }



  /**
   * Retrieves the wrapped request from a downstream client, if available.
   *
   * @return  The wrapped request from a downstream client, or {@code null} if
   *          there is none.
   */
  public IntermediateClientRequestValue getDownstreamRequest()
  {
    return downstreamRequest;
  }



  /**
   * Retrieves the requested client authorization identity, if available.
   *
   * @return  The requested client authorization identity, or {@code null} if
   *          there is none.
   */
  public String getClientIdentity()
  {
    return clientIdentity;
  }



  /**
   * Retrieves the IP address or resolvable name of the downstream client
   * system, if available.
   *
   * @return  The IP address or resolvable name of the downstream client system,
   *          or {@code null} if there is no downstream client or its address is
   *          not available.
   */
  public String getDownstreamClientAddress()
  {
    return downstreamClientAddress;
  }



  /**
   * Indicates whether the communication with the communication with the
   * downstream client is secure (i.e., whether communication between the
   * client application and the downstream client is safe from interpretation or
   * undetectable alteration by a third party observer or interceptor).
   *
   *
   * @return  {@code Boolean.TRUE} if communication with the downstream client
   *          is secure, {@code Boolean.FALSE} if it is not secure, or
   *          {@code null} if there is no downstream client or it is not known
   *          whether the communication is secure.
   */
  public Boolean downstreamClientSecure()
  {
    return downstreamClientSecure;
  }



  /**
   * Retrieves a string that identifies the client application that created this
   * intermediate client request value.
   *
   * @return  A string that may be used to identify the client application that
   *          created this intermediate client request value.
   */
  public String getClientName()
  {
    return clientName;
  }



  /**
   * Retrieves a string that may be used to identify the session in the client
   * application.
   *
   * @return  A string that may be used to identify the session in the client
   *          application, or {@code null} if there is none.
   */
  public String getClientSessionID()
  {
    return clientSessionID;
  }



  /**
   * Retrieves a string that may be used to identify the request in the client
   * application.
   *
   * @return  A string that may be used to identify the request in the client
   *          application, or {@code null} if there is none.
   */
  public String getClientRequestID()
  {
    return clientRequestID;
  }



  /**
   * Encodes this intermediate client request value to a form that may be
   * included in the request control.
   *
   * @return  An ASN.1 octet string containing the encoded client request value.
   */
  public ASN1Sequence encode()
  {
    return encode(ASN1Constants.UNIVERSAL_SEQUENCE_TYPE);
  }



  /**
   * Encodes this intermediate client request value to a form that may be
   * included in the request control.
   *
   * @param  type  The BER type to use for this element.
   *
   * @return  An ASN.1 octet string containing the encoded client request value.
   */
  private ASN1Sequence encode(final byte type)
  {
    final ArrayList<ASN1Element> elements = new ArrayList<ASN1Element>(7);

    if (downstreamRequest != null)
    {
      elements.add(downstreamRequest.encode(TYPE_DOWNSTREAM_REQUEST));
    }

    if (downstreamClientAddress != null)
    {
      elements.add(new ASN1OctetString(TYPE_DOWNSTREAM_CLIENT_ADDRESS,
                                       downstreamClientAddress));
    }

    if (downstreamClientSecure != null)
    {
      elements.add(new ASN1Boolean(TYPE_DOWNSTREAM_CLIENT_SECURE,
                                   downstreamClientSecure));
    }

    if (clientIdentity != null)
    {
      elements.add(new ASN1OctetString(TYPE_CLIENT_IDENTITY, clientIdentity));
    }

    if (clientName != null)
    {
      elements.add(new ASN1OctetString(TYPE_CLIENT_NAME,  clientName));
    }

    if (clientSessionID != null)
    {
      elements.add(new ASN1OctetString(TYPE_CLIENT_SESSION_ID,
                                       clientSessionID));
    }

    if (clientRequestID != null)
    {
      elements.add(new ASN1OctetString(TYPE_CLIENT_REQUEST_ID,
                                       clientRequestID));
    }

    return new ASN1Sequence(type, elements);
  }



  /**
   * Decodes the provided ASN.1 sequence as an intermediate client request
   * value.
   *
   * @param  sequence  The sequence to be decoded as an intermediate client
   *                   request value.
   *
   * @return  The decoded intermediate client request value.
   *
   * @throws  LDAPException  If the provided sequence cannot be decoded as an
   *                         intermediate client request value.
   */
  public static IntermediateClientRequestValue
                     decode(final ASN1Sequence sequence)
         throws LDAPException
  {
    Boolean                        downstreamClientSecure  = null;
    IntermediateClientRequestValue downstreamRequest       = null;
    String                         clientIdentity          = null;
    String                         downstreamClientAddress = null;
    String                         clientName              = null;
    String                         clientRequestID         = null;
    String                         clientSessionID         = null;

    for (final ASN1Element element : sequence.elements())
    {
      switch (element.getType())
      {
        case TYPE_DOWNSTREAM_REQUEST:
          try
          {
            final ASN1Sequence s = ASN1Sequence.decodeAsSequence(element);
            downstreamRequest = decode(s);
          }
          catch (LDAPException le)
          {
            debugException(le);
            throw new LDAPException(ResultCode.DECODING_ERROR,
                 ERR_ICREQ_CANNOT_DECODE_DOWNSTREAM_REQUEST.get(
                      le.getMessage()), le);
          }
          catch (Exception e)
          {
            debugException(e);
            throw new LDAPException(ResultCode.DECODING_ERROR,
                 ERR_ICREQ_CANNOT_DECODE_DOWNSTREAM_REQUEST.get(
                      String.valueOf(e)), e);
          }
          break;

        case TYPE_DOWNSTREAM_CLIENT_ADDRESS:
          downstreamClientAddress =
               ASN1OctetString.decodeAsOctetString(element).stringValue();
          break;

        case TYPE_DOWNSTREAM_CLIENT_SECURE:
          try
          {
            downstreamClientSecure =
                 ASN1Boolean.decodeAsBoolean(element).booleanValue();
          }
          catch (Exception e)
          {
            debugException(e);
            throw new LDAPException(ResultCode.DECODING_ERROR,
                 ERR_ICREQ_CANNOT_DECODE_DOWNSTREAM_SECURE.get(
                      String.valueOf(e)), e);
          }
          break;

        case TYPE_CLIENT_IDENTITY:
          clientIdentity =
               ASN1OctetString.decodeAsOctetString(element).stringValue();
          break;

        case TYPE_CLIENT_NAME:
          clientName =
               ASN1OctetString.decodeAsOctetString(element).stringValue();
          break;

        case TYPE_CLIENT_SESSION_ID:
          clientSessionID =
               ASN1OctetString.decodeAsOctetString(element).stringValue();
          break;

        case TYPE_CLIENT_REQUEST_ID:
          clientRequestID =
               ASN1OctetString.decodeAsOctetString(element).stringValue();
          break;

        default:
          throw new LDAPException(ResultCode.DECODING_ERROR,
               ERR_ICREQ_INVALID_ELEMENT_TYPE.get(toHex(element.getType())));
      }
    }

    return new IntermediateClientRequestValue(downstreamRequest,
                                              downstreamClientAddress,
                                              downstreamClientSecure,
                                              clientIdentity, clientName,
                                              clientSessionID, clientRequestID);
  }



  /**
   * Generates a hash code for this intermediate client request value.
   *
   * @return  A hash code for this intermediate client request value.
   */
  @Override()
  public int hashCode()
  {
    int hashCode = 0;

    if (downstreamRequest != null)
    {
      hashCode += downstreamRequest.hashCode();
    }

    if (downstreamClientAddress != null)
    {
      hashCode += downstreamClientAddress.hashCode();
    }

    if (downstreamClientSecure != null)
    {
      hashCode += downstreamClientSecure.hashCode();
    }

    if (clientIdentity != null)
    {
      hashCode += clientIdentity.hashCode();
    }

    if (clientName != null)
    {
      hashCode += clientName.hashCode();
    }

    if (clientSessionID != null)
    {
      hashCode += clientSessionID.hashCode();
    }

    if (clientRequestID != null)
    {
      hashCode += clientRequestID.hashCode();
    }

    return hashCode;
  }



  /**
   * Indicates whether the provided object is equal to this intermediate client
   * request value.  It will only be considered equal if the provided object is
   * also an intermediate client request value with all the same fields.
   *
   * @param  o  The object for which to make the determination.
   *
   * @return  {@code true} if the provided object is considered equal to this
   *          intermediate client request value, or {@code false} if not.
   */
  @Override()
  public boolean equals(final Object o)
  {
    if (o == this)
    {
      return true;
    }
    else if (o == null)
    {
      return false;
    }
    else if (! (o instanceof IntermediateClientRequestValue))
    {
      return false;
    }

    final IntermediateClientRequestValue v = (IntermediateClientRequestValue) o;

    if (downstreamRequest == null)
    {
      if (v.downstreamRequest != null)
      {
        return false;
      }
    }
    else
    {
      if (! downstreamRequest.equals(v.downstreamRequest))
      {
        return false;
      }
    }

    if (downstreamClientAddress == null)
    {
      if (v.downstreamClientAddress != null)
      {
        return false;
      }
    }
    else
    {
      if (! downstreamClientAddress.equals(v.downstreamClientAddress))
      {
        return false;
      }
    }

    if (downstreamClientSecure == null)
    {
      if (v.downstreamClientSecure != null)
      {
        return false;
      }
    }
    else
    {
      if (! downstreamClientSecure.equals(v.downstreamClientSecure))
      {
        return false;
      }
    }

    if (clientIdentity == null)
    {
      if (v.clientIdentity != null)
      {
        return false;
      }
    }
    else
    {
      if (! clientIdentity.equals(v.clientIdentity))
      {
        return false;
      }
    }

    if (clientName == null)
    {
      if (v.clientName != null)
      {
        return false;
      }
    }
    else
    {
      if (! clientName.equals(v.clientName))
      {
        return false;
      }
    }

    if (clientSessionID == null)
    {
      if (v.clientSessionID != null)
      {
        return false;
      }
    }
    else
    {
      if (! clientSessionID.equals(v.clientSessionID))
      {
        return false;
      }
    }

    if (clientRequestID == null)
    {
      if (v.clientRequestID != null)
      {
        return false;
      }
    }
    else
    {
      if (! clientRequestID.equals(v.clientRequestID))
      {
        return false;
      }
    }

    return true;
  }



  /**
   * Retrieves a string representation of this intermediate client request
   * value.
   *
   * @return  A string representation of this intermediate client request value.
   */
  @Override()
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    toString(buffer);
    return buffer.toString();
  }



  /**
   * Appends a string representation of this intermediate client request value
   * to the provided buffer.
   *
   * @param  buffer  The buffer to which the information is to be appended.
   */
  public void toString(final StringBuilder buffer)
  {
    buffer.append("IntermediateClientRequestValue(");

    boolean added = false;
    if (downstreamRequest != null)
    {
      buffer.append("downstreamRequest=");
      downstreamRequest.toString(buffer);
      added = true;
    }

    if (clientIdentity != null)
    {
      if (added)
      {
        buffer.append(", ");
      }
      else
      {
        added = true;
      }

      buffer.append("clientIdentity='");
      buffer.append(clientIdentity);
      buffer.append('\'');
    }

    if (downstreamClientAddress != null)
    {
      if (added)
      {
        buffer.append(", ");
      }
      else
      {
        added = true;
      }

      buffer.append("downstreamClientAddress='");
      buffer.append(downstreamClientAddress);
      buffer.append('\'');
    }

    if (downstreamClientSecure != null)
    {
      if (added)
      {
        buffer.append(", ");
      }
      else
      {
        added = true;
      }

      buffer.append("downstreamClientSecure='");
      buffer.append(downstreamClientSecure);
      buffer.append('\'');
    }

    if (clientName != null)
    {
      if (added)
      {
        buffer.append(", ");
      }
      else
      {
        added = true;
      }

      buffer.append("clientName='");
      buffer.append(clientName);
      buffer.append('\'');
    }

    if (clientSessionID != null)
    {
      if (added)
      {
        buffer.append(", ");
      }
      else
      {
        added = true;
      }

      buffer.append("clientSessionID='");
      buffer.append(clientSessionID);
      buffer.append('\'');
    }

    if (clientRequestID != null)
    {
      if (added)
      {
        buffer.append(", ");
      }
      else
      {
        added = true;
      }

      buffer.append("clientRequestID='");
      buffer.append(clientRequestID);
      buffer.append('\'');
    }

    buffer.append(')');
  }
}
