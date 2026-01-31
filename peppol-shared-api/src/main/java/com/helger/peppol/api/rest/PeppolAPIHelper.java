/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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
package com.helger.peppol.api.rest;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.callback.exception.IExceptionCallback;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.jaxb.GenericJAXBMarshaller;
import com.helger.peppol.businesscard.generic.PDBusinessCard;
import com.helger.peppol.businesscard.helper.PDBusinessCardHelper;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.ui.types.PeppolUITypes;
import com.helger.peppol.ui.types.feedbackcb.IFeedbackCallback;
import com.helger.peppol.ui.types.smp.ISMPClientCreationCallback;
import com.helger.peppol.ui.types.smp.ISMPExtensionsCallback;
import com.helger.peppol.ui.types.smp.SMPQueryParams;
import com.helger.peppolid.CIdentifier;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.smpclient.bdxr1.BDXRClientReadOnly;
import com.helger.smpclient.bdxr2.BDXR2ClientReadOnly;
import com.helger.smpclient.exception.SMPClientException;
import com.helger.smpclient.httpclient.SMPHttpClientSettings;
import com.helger.smpclient.peppol.SMPClientReadOnly;
import com.helger.xsds.bdxr.smp2.bc.IDType;

@Immutable
public final class PeppolAPIHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PeppolAPIHelper.class);

  private PeppolAPIHelper ()
  {}

  @NonNull
  public static String getBusinessCardURL (@NonNull final SMPQueryParams aSMPQueryParams)
  {
    final IParticipantIdentifier aParticipantID = aSMPQueryParams.getParticipantID ();
    final URI aSMPHostURI = aSMPQueryParams.getSMPHostURI ();
    return StringHelper.trimEnd (aSMPHostURI.toString (), '/') + "/businesscard/" + aParticipantID.getURIEncoded ();
  }

  @Nullable
  public static byte [] retrieveBusinessCardBytes (@NonNull final String sLogPrefix,
                                                   @NonNull final SMPQueryParams aSMPQueryParams,
                                                   @Nullable final Consumer <? super SMPHttpClientSettings> aHCSModifier,
                                                   @NonNull final IFeedbackCallback aMiniCallback,
                                                   @NonNull final IExceptionCallback <? super Exception> aExceptionCallback)
  {
    LOGGER.info (sLogPrefix +
                 "BusinessCard of '" +
                 aSMPQueryParams.getParticipantID ().getURIEncoded () +
                 "' is queried using SMP API '" +
                 aSMPQueryParams.getSMPAPIType () +
                 "' from '" +
                 aSMPQueryParams.getSMPHostURI () +
                 "' using SML '" +
                 aSMPQueryParams.getSMLInfo ().getID () +
                 "'");

    final String sBCURL = getBusinessCardURL (aSMPQueryParams);
    LOGGER.info (sLogPrefix + "  Querying BC from '" + sBCURL + "'");

    // Use SMP settings - e.g. don't follow redirect
    final SMPHttpClientSettings aHCS = new SMPHttpClientSettings ();
    if (aHCSModifier != null)
      aHCSModifier.accept (aHCS);

    byte [] aBCBytes;
    try (final HttpClientManager aHttpClientMgr = HttpClientManager.create (aHCS))
    {
      final HttpGet aGet = new HttpGet (sBCURL);
      aBCBytes = aHttpClientMgr.execute (aGet, new ResponseHandlerByteArray ());
    }
    catch (final Exception ex)
    {
      aExceptionCallback.onException (ex);
      aBCBytes = null;
    }

    if (aBCBytes == null)
      aMiniCallback.warn ("No Business Card is available for that participant.");

    return aBCBytes;
  }

  @Nullable
  public static PDBusinessCard retrieveBusinessCardParsed (@NonNull final String sLogPrefix,
                                                           @NonNull final SMPQueryParams aSMPQueryParams,
                                                           @Nullable final Consumer <? super HttpClientSettings> aHCSModifier,
                                                           @NonNull final IFeedbackCallback aMiniCallback,
                                                           @NonNull final IExceptionCallback <? super Exception> aExceptionCallback)
  {
    final byte [] aBCBytes = retrieveBusinessCardBytes (sLogPrefix,
                                                        aSMPQueryParams,
                                                        aHCSModifier,
                                                        aMiniCallback,
                                                        aExceptionCallback);
    if (aBCBytes != null)
    {
      final PDBusinessCard aBC = PDBusinessCardHelper.parseBusinessCard (aBCBytes, StandardCharsets.UTF_8);
      if (aBC != null)
      {
        // Business Card found
        return aBC;
      }
      aMiniCallback.error ("Failed to parse BC:\n" + new String (aBCBytes, StandardCharsets.UTF_8));
    }
    return null;
  }

  /**
   * Get all document types of a participant
   *
   * @param sLogPrefix
   *        Log prefix
   * @param aSMPQueryParams
   *        SMP query parameters
   * @param aHCSModifier
   *        Optional HTTP Client settings modifier callback
   * @param bXMLSchemaValidation
   *        <code>true</code> to enable XML Schema validation (recommended)
   * @param bVerifySignature
   *        <code>true</code> to perform signature validation (recommended)
   * @param aSMPClientCallback
   *        The SMP client modification callback
   * @param aDuplicateURLCallback
   *        The Duplicate URL collector callback. Will be called with URL decoded URLs only
   * @param aSMPMarshallerCustomizer
   *        Optional SMP marshaller customizing callback
   * @param aExtensionCallback
   *        Callback to be invoked on SMP extensions.
   * @param aExceptionCallback
   *        Callback to be invoked on SMP exceptions
   * @return A map from clean (URL unescaped) URL to the original URL as found in the data
   */
  @Nullable
  public static ICommonsOrderedMap <String, String> retrieveAllDocumentTypes (@NonNull final String sLogPrefix,
                                                                              @NonNull final SMPQueryParams aSMPQueryParams,
                                                                              @Nullable final Consumer <? super SMPHttpClientSettings> aHCSModifier,
                                                                              final boolean bXMLSchemaValidation,
                                                                              final boolean bVerifySignature,
                                                                              @NonNull final ISMPClientCreationCallback aSMPClientCallback,
                                                                              @NonNull final Consumer <? super String> aDuplicateURLCallback,
                                                                              @NonNull final Consumer <? super GenericJAXBMarshaller <?>> aSMPMarshallerCustomizer,
                                                                              @NonNull final ISMPExtensionsCallback aExtensionCallback,
                                                                              @NonNull final IExceptionCallback <? super SMPClientException> aExceptionCallback)
  {
    final IParticipantIdentifier aParticipantID = aSMPQueryParams.getParticipantID ();
    final ESMPAPIType eAPIType = aSMPQueryParams.getSMPAPIType ();
    final IIdentifierFactory aIF = aSMPQueryParams.getIF ();

    LOGGER.info (sLogPrefix +
                 "Document types of '" +
                 aParticipantID.getURIEncoded () +
                 "' are queried using SMP API '" +
                 eAPIType +
                 "' from '" +
                 aSMPQueryParams.getSMPHostURI () +
                 "' using SML '" +
                 aSMPQueryParams.getSMLInfo ().getID () +
                 "'; XSD validation=" +
                 bXMLSchemaValidation +
                 "; signature verification=" +
                 bVerifySignature);

    ICommonsOrderedMap <String, String> ret = null;
    switch (eAPIType)
    {
      case PEPPOL:
      {
        final SMPClientReadOnly aSMPClient = new SMPClientReadOnly (aSMPQueryParams.getSMPHostURI ());
        aSMPClient.setSecureValidation (PeppolUITypes.DEFAULT_SMP_USE_SECURE_VALIDATION);
        aSMPClient.withHttpClientSettings (aHCSModifier);
        aSMPClient.setXMLSchemaValidation (bXMLSchemaValidation);
        aSMPClient.setVerifySignature (bVerifySignature);
        aSMPClient.setMarshallerCustomizer (aSMPMarshallerCustomizer);
        if (aSMPQueryParams.isTrustAllCertificates ())
          try
          {
            aSMPClient.httpClientSettings ().setSSLContextTrustAll ();
          }
          catch (final GeneralSecurityException ex)
          {
            // Ignore
          }

        aSMPClientCallback.onPeppolSMPClient (aSMPClient);

        // Get all HRefs
        com.helger.xsds.peppol.smp1.ServiceGroupType aSG;
        try
        {
          aSG = aSMPClient.getServiceGroupOrNull (aParticipantID);
        }
        catch (final SMPClientException ex)
        {
          aExceptionCallback.onException (ex);
          aSG = null;
        }

        // Map from cleaned URL to original URL
        if (aSG != null)
        {
          if (aSG.getServiceMetadataReferenceCollection () != null)
          {
            ret = new CommonsLinkedHashMap <> ();
            for (final var aSMR : aSG.getServiceMetadataReferenceCollection ().getServiceMetadataReference ())
            {
              final String sOriginalHref = aSMR.getHref ();
              // Decoded href is important for unification
              final String sCleanHref = CIdentifier.createPercentDecoded (sOriginalHref);
              if (ret.put (sCleanHref, sOriginalHref) != null)
                aDuplicateURLCallback.accept (sCleanHref);
            }
          }

          if (aSG.getExtension () != null && aSG.getExtension ().getAny () != null)
          {
            aExtensionCallback.onPeppolSMPExtension (aSG.getExtension ());
          }
        }
        break;
      }
      case OASIS_BDXR_V1:
      {
        final BDXRClientReadOnly aBDXR1Client = new BDXRClientReadOnly (aSMPQueryParams.getSMPHostURI ());
        aBDXR1Client.setSecureValidation (PeppolUITypes.DEFAULT_SMP_USE_SECURE_VALIDATION);
        aBDXR1Client.withHttpClientSettings (aHCSModifier);
        aBDXR1Client.setXMLSchemaValidation (bXMLSchemaValidation);
        aBDXR1Client.setVerifySignature (bVerifySignature);
        aBDXR1Client.setMarshallerCustomizer (aSMPMarshallerCustomizer);
        if (aSMPQueryParams.isTrustAllCertificates ())
          try
          {
            aBDXR1Client.httpClientSettings ().setSSLContextTrustAll ();
          }
          catch (final GeneralSecurityException ex)
          {
            // Ignore
          }

        aSMPClientCallback.onBDXR1Client (aBDXR1Client);

        // Get all HRefs and sort them by decoded URL
        com.helger.xsds.bdxr.smp1.ServiceGroupType aSG;
        try
        {
          aSG = aBDXR1Client.getServiceGroupOrNull (aParticipantID);
        }
        catch (final SMPClientException ex)
        {
          aExceptionCallback.onException (ex);
          aSG = null;
        }

        // Map from cleaned URL to original URL
        if (aSG != null)
        {
          if (aSG.getServiceMetadataReferenceCollection () != null)
          {
            ret = new CommonsLinkedHashMap <> ();
            for (final var aSMR : aSG.getServiceMetadataReferenceCollection ().getServiceMetadataReference ())
            {
              final String sOriginalHref = aSMR.getHref ();
              // Decoded href is important for unification
              final String sCleanHref = CIdentifier.createPercentDecoded (sOriginalHref);
              if (ret.put (sCleanHref, sOriginalHref) != null)
                aDuplicateURLCallback.accept (sCleanHref);
            }
          }

          if (aSG.getExtensionCount () > 0)
          {
            aExtensionCallback.onBDXR1Extension (aSG.getExtension ());
          }
        }
        break;
      }
      case OASIS_BDXR_V2:
      {
        final BDXR2ClientReadOnly aBDXR2Client = new BDXR2ClientReadOnly (aSMPQueryParams.getSMPHostURI ());
        aBDXR2Client.setSecureValidation (PeppolUITypes.DEFAULT_SMP_USE_SECURE_VALIDATION);
        aBDXR2Client.withHttpClientSettings (aHCSModifier);
        aBDXR2Client.setXMLSchemaValidation (bXMLSchemaValidation);
        aBDXR2Client.setVerifySignature (bVerifySignature);
        aBDXR2Client.setMarshallerCustomizer (aSMPMarshallerCustomizer);
        if (aSMPQueryParams.isTrustAllCertificates ())
          try
          {
            aBDXR2Client.httpClientSettings ().setSSLContextTrustAll ();
          }
          catch (final GeneralSecurityException ex)
          {
            // Ignore
          }

        aSMPClientCallback.onBDXR2Client (aBDXR2Client);

        // Get all HRefs and sort them by decoded URL
        com.helger.xsds.bdxr.smp2.ServiceGroupType aSG;
        try
        {
          aSG = aBDXR2Client.getServiceGroupOrNull (aParticipantID);
        }
        catch (final SMPClientException ex)
        {
          aExceptionCallback.onException (ex);
          aSG = null;
        }

        if (aSG != null)
        {
          ret = new CommonsLinkedHashMap <> ();
          for (final var aSMR : aSG.getServiceReference ())
          {
            final IDType aID = aSMR.getID ();
            if (aID != null)
            {
              final IDocumentTypeIdentifier aDocTypeID = aIF.createDocumentTypeIdentifier (aID.getSchemeID (),
                                                                                           aID.getValue ());
              if (aDocTypeID != null)
              {
                // Found a document type
                final String sOriginalHref = aSMPQueryParams.getSMPHostURI ().toString () +
                                             '/' +
                                             BDXR2ClientReadOnly.PATH_OASIS_BDXR_SMP_2 +
                                             aParticipantID.getURIPercentEncoded () +
                                             '/' +
                                             BDXR2ClientReadOnly.URL_PART_SERVICES +
                                             '/' +
                                             CIdentifier.getURIPercentEncoded (aDocTypeID);
                // Decoded href is important for unification
                final String sCleanHref = CIdentifier.createPercentDecoded (sOriginalHref);
                if (ret.put (sCleanHref, sOriginalHref) != null)
                  aDuplicateURLCallback.accept (sCleanHref);
              }
            }
          }

          if (aSG.getSMPExtensions () != null && aSG.getSMPExtensions ().hasSMPExtensionEntries ())
          {
            aExtensionCallback.onBDXR2Extension (aSG.getSMPExtensions ().getSMPExtension ());
          }
        }
        break;
      }
    }
    return ret;
  }
}
