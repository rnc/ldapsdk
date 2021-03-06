/*
 * Copyright 2011-2016 UnboundID Corp.
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



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
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
 * This class provides an implementation of a get changelog batch change
 * selection criteria value that indicates that the server should only return
 * changes which target all or more of the specified attributes.  The changes
 * may target other attributes as well, but all of the associated attributes
 * must be included in the change.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class AllAttributesChangeSelectionCriteria
       extends ChangelogBatchChangeSelectionCriteria
{
  /**
   * The inner BER type that should be used for encoded elements that represent
   * an all attributes get changelog batch selection criteria value.
   */
  static final byte TYPE_SELECTION_CRITERIA_ALL_ATTRIBUTES = (byte) 0xA2;



  // The names of the target attributes.
  private final List<String> attributeNames;



  /**
   * Creates a new all attributes change selection criteria value with the
   * provided set of attribute names.
   *
   * @param  attributeNames  The names of the target attributes for changes that
   *                         should be retrieved.  It must not be {@code null}
   *                         or empty.
   */
  public AllAttributesChangeSelectionCriteria(final String... attributeNames)
  {
    this(StaticUtils.toList(attributeNames));
  }



  /**
   * Creates a new all attributes change selection criteria value with the
   * provided set of attribute names.
   *
   * @param  attributeNames  The names of the target attributes for changes that
   *                         should be retrieved.  It must not be {@code null}
   *                         or empty.
   */
  public AllAttributesChangeSelectionCriteria(
              final Collection<String> attributeNames)
  {
    Validator.ensureNotNull(attributeNames);
    Validator.ensureFalse(attributeNames.isEmpty());

    this.attributeNames =
         Collections.unmodifiableList(new ArrayList<String>(attributeNames));
  }



  /**
   * Decodes the provided ASN.1 element, which is the inner element of a
   * changelog batch change selection criteria element, as an all attributes
   * change selection criteria value.
   *
   * @param  innerElement  The inner element of a changelog batch change
   *                       selection criteria element to be decoded.
   *
   * @return  The decoded all attributes change selection criteria value.
   *
   * @throws  LDAPException  If a problem is encountered while trying to decode
   *                         the provided element as the inner element of an all
   *                         attributes change selection criteria value.
   */
  static AllAttributesChangeSelectionCriteria decodeInnerElement(
              final ASN1Element innerElement)
         throws LDAPException
  {
    try
    {
      final ASN1Element[] attrElements =
           ASN1Sequence.decodeAsSequence(innerElement).elements();
      final ArrayList<String> attrNames =
           new ArrayList<String>(attrElements.length);
      for (final ASN1Element e : attrElements)
      {
        attrNames.add(ASN1OctetString.decodeAsOctetString(e).stringValue());
      }

      return new AllAttributesChangeSelectionCriteria(attrNames);
    }
    catch (final Exception e)
    {
      Debug.debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_ALL_ATTRS_CHANGE_SELECTION_CRITERIA_DECODE_ERROR.get(
                StaticUtils.getExceptionMessage(e)),
           e);
    }
  }



  /**
   * Retrieves the names of the target attributes for changes that should be
   * retrieved.
   *
   * @return  The names of the target attributes for changes that should be
   *          retrieved.
   */
  public List<String> getAttributeNames()
  {
    return attributeNames;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public ASN1Element encodeInnerElement()
  {
    final ArrayList<ASN1Element> elements =
         new ArrayList<ASN1Element>(attributeNames.size());
    for (final String s : attributeNames)
    {
      elements.add(new ASN1OctetString(s));
    }

    return new ASN1Sequence(TYPE_SELECTION_CRITERIA_ALL_ATTRIBUTES, elements);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("AllAttributesChangeSelectionCriteria(attributeNames={");

    final Iterator<String> iterator = attributeNames.iterator();
    while (iterator.hasNext())
    {
      buffer.append(iterator.next());
      if (iterator.hasNext())
      {
        buffer.append(',');
      }
    }

    buffer.append("})");
  }
}
