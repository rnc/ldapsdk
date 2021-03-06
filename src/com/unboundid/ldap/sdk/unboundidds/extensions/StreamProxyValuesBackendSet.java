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
package com.unboundid.ldap.sdk.unboundidds.extensions;



import java.io.Serializable;

import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1Integer;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.extensions.ExtOpMessages.*;
import static com.unboundid.util.Debug.*;
import static com.unboundid.util.StaticUtils.*;
import static com.unboundid.util.Validator.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides a data structure for holding information about the
 * configuration of backend sets as used by the stream proxy values extended
 * request.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class StreamProxyValuesBackendSet
       implements Serializable
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -5437145469462592611L;



  // The backend set ID for this backend set.
  private final ASN1OctetString backendSetID;

  // The ports of the directory servers in this backend set.
  private final int[] ports;

  // The addresses of the directory servers in this backend set.
  private final String[] hosts;



  /**
   * Creates a new backend set with the provided information.
   *
   * @param  backendSetID  The backend set ID for this backend set.  It must not
   *                       be {@code null}.
   * @param  hosts         The addresses of the servers for this backend set.
   *                       It must not be {@code null} or empty, and it must
   *                       have the same number of elements as the {@code ports}
   *                       array.
   * @param  ports         The ports of the servers for this backend set.  It
   *                       must not be {@code null} or empty, and it must have
   *                       the same number of elements as the {@code hosts}
   *                       array.
   */
  public StreamProxyValuesBackendSet(final ASN1OctetString backendSetID,
                                     final String[] hosts, final int[] ports)
  {
    ensureNotNull(backendSetID, hosts, ports);
    ensureTrue(hosts.length > 0);
    ensureTrue(hosts.length == ports.length);

    this.backendSetID = backendSetID;
    this.hosts        = hosts;
    this.ports        = ports;
  }



  /**
   * Retrieves the backend set ID for this backend set.
   *
   * @return  The backend set ID for this backend set.
   */
  public ASN1OctetString getBackendSetID()
  {
    return backendSetID;
  }



  /**
   * Retrieves the addresses of the servers for this backend set.
   *
   * @return  The addresses of the servers for this backend set.
   */
  public String[] getHosts()
  {
    return hosts;
  }



  /**
   * Retrieves the ports of the servers for this backend set.
   *
   * @return  The ports of the servers for this backend set.
   */
  public int[] getPorts()
  {
    return ports;
  }



  /**
   * Encodes this backend set object in a form suitable for inclusion in the
   * value of the stream proxy values extended request.
   *
   * @return  The encoded representation of this backend set.
   */
  public ASN1Element encode()
  {
    final ASN1Element[] hostPortElements = new ASN1Element[hosts.length];
    for (int i=0; i < hosts.length; i++)
    {
      hostPortElements[i] = new ASN1Sequence(
           new ASN1OctetString(hosts[i]),
           new ASN1Integer(ports[i]));
    }

    return new ASN1Sequence(
         backendSetID,
         new ASN1Sequence(hostPortElements));
  }



  /**
   * Decodes the provided ASN.1 element as a backend set.
   *
   * @param  element  The element to be decoded as a backend set.
   *
   * @return  The decoded backend set.
   *
   * @throws  LDAPException  If the provided ASN.1 element cannot be decoded as
   *                         a backend set.
   */
  public static StreamProxyValuesBackendSet decode(final ASN1Element element)
         throws LDAPException
  {
    try
    {
      final ASN1Element[] elements =
           ASN1Sequence.decodeAsSequence(element).elements();
      final ASN1OctetString backendSetID =
           ASN1OctetString.decodeAsOctetString(elements[0]);

      final ASN1Element[] hostPortElements =
           ASN1Sequence.decodeAsSequence(elements[1]).elements();
      final String[] hosts = new String[hostPortElements.length];
      final int[]    ports = new int[hostPortElements.length];
      for (int i=0; i < hostPortElements.length; i++)
      {
        final ASN1Element[] hpElements =
             ASN1Sequence.decodeAsSequence(hostPortElements[i]).elements();
        hosts[i] =
             ASN1OctetString.decodeAsOctetString(hpElements[0]).stringValue();
        ports[i] = ASN1Integer.decodeAsInteger(hpElements[1]).intValue();
      }

      return new StreamProxyValuesBackendSet(backendSetID, hosts, ports);
    }
    catch (Exception e)
    {
      debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_STREAM_PROXY_VALUES_BACKEND_SET_CANNOT_DECODE.get(
                getExceptionMessage(e)), e);
    }
  }



  /**
   * Retrieves a string representation of this stream proxy values backend set.
   *
   * @return  A string representation of this stream proxy values backend set.
   */
  @Override()
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    toString(buffer);
    return buffer.toString();
  }



  /**
   * Appends a string representation of this stream proxy values backend set to
   * the provided buffer.
   *
   * @param  buffer  The buffer to which the stream representation should be
   *                 appended.
   */
  public void toString(final StringBuilder buffer)
  {
    buffer.append("StreamProxyValuesBackendSet(id=");
    backendSetID.toString(buffer);
    buffer.append(", servers={");

    for (int i=0; i < hosts.length; i++)
    {
      if (i > 0)
      {
        buffer.append(", ");
      }
      buffer.append(hosts[i]);
      buffer.append(':');
      buffer.append(ports[i]);
    }
    buffer.append("})");
  }
}
