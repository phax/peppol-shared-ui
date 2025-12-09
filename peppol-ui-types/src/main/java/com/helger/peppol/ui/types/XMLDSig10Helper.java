/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.peppol.ui.types;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.security.certificate.CertificateHelper;
import com.helger.xsds.xmldsig.KeyInfoType;
import com.helger.xsds.xmldsig.SignatureType;
import com.helger.xsds.xmldsig.X509DataType;

import jakarta.xml.bind.JAXBElement;

/**
 * Utility class to easily extract specific data elements from XMLDSig 1.0 objects.
 *
 * @author Philip Helger
 */
@Immutable
public final class XMLDSig10Helper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (XMLDSig10Helper.class);

  private XMLDSig10Helper ()
  {}

  @Nullable
  public static String getSigningAlgorithmString (@NonNull final SignatureType aSignature)
  {
    ValueEnforcer.notNull (aSignature, "Signature");

    final var aSignedInfo = aSignature.getSignedInfo ();
    if (aSignedInfo != null)
    {
      final var aSM = aSignedInfo.getSignatureMethod ();
      if (aSM != null)
        return aSM.getAlgorithm ();
    }
    return null;
  }

  @Nullable
  public static byte [] getSigningCertificateBytes (@NonNull final SignatureType aSignature)
  {
    ValueEnforcer.notNull (aSignature, "Signature");

    final KeyInfoType aKeyInfo = aSignature.getKeyInfo ();
    if (aKeyInfo != null)
    {
      // E.g. 0007:5591793939 usses multiple X509Data elements
      return aKeyInfo.getContent ()
                     .stream ()
                     .filter (JAXBElement.class::isInstance)
                     .map (JAXBElement.class::cast)
                     .filter (x -> x.getValue () instanceof X509DataType)
                     .map (x -> (X509DataType) x.getValue ())
                     .flatMap (x -> x.getX509IssuerSerialOrX509SKIOrX509SubjectName ().stream ())
                     .filter (JAXBElement.class::isInstance)
                     .map (JAXBElement.class::cast)
                     .filter (x -> x.getName ().getLocalPart ().equals ("X509Certificate"))
                     .map (JAXBElement::getValue)
                     .map (byte [].class::cast)
                     .findFirst ()
                     .orElse (null);
    }

    return null;
  }

  @Nullable
  public static X509Certificate getSigningCertificate (@NonNull final SignatureType aSignature)
  {
    ValueEnforcer.notNull (aSignature, "Signature");

    final byte [] aCertBytes = getSigningCertificateBytes (aSignature);
    if (aCertBytes == null)
      return null;

    try
    {
      // Parse certificate
      return CertificateHelper.convertByteArrayToCertficateDirect (aCertBytes);
    }
    catch (final CertificateException ex)
    {
      // Fall through
      LOGGER.warn ("Failed to parse Signature-provided X509 Certificate. Technical Details: " +
                   ex.getClass ().getName () +
                   " - " +
                   ex.getMessage ());
    }
    return null;
  }
}
