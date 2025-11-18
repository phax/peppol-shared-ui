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
import com.helger.peppol.ui.types.minicallback.IMiniCallback;
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
  public static byte [] retrieveBusinessCard (@NonNull final String sLogPrefix,
                                              @NonNull final SMPQueryParams aSMPQueryParams,
                                              @Nullable final Consumer <? super SMPHttpClientSettings> aHCSModifier,
                                              @NonNull final IMiniCallback aMiniCallback)
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
                                                           @NonNull final IMiniCallback aMiniCallback)
  {
    final byte [] aBCBytes = retrieveBusinessCard (sLogPrefix, aSMPQueryParams, aHCSModifier, aMiniCallback);
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

  @Nullable
  public static ICommonsOrderedMap <String, String> retrieveAllDocumentTypes (@NonNull final String sLogPrefix,
                                                                              @NonNull final SMPQueryParams aSMPQueryParams,
                                                                              @Nullable final Consumer <? super SMPHttpClientSettings> aHCSModifier,
                                                                              final boolean bXMLSchemaValidation,
                                                                              final boolean bVerifySignature,
                                                                              @NonNull final ISMPClientCreationCallback aSMPClientCallback,
                                                                              @NonNull final Consumer <String> aDuplicateURLCallback,
                                                                              @NonNull final Consumer <GenericJAXBMarshaller <?>> aSMPMarshallerCustomizer,
                                                                              @NonNull final ISMPExtensionsCallback aExtensionCallback)
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
              // Decoded href is important for unification
              final String sHref = CIdentifier.createPercentDecoded (aSMR.getHref ());
              if (ret.put (sHref, aSMR.getHref ()) != null)
                aDuplicateURLCallback.accept (sHref);
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
              // Decoded href is important for unification
              final String sHref = CIdentifier.createPercentDecoded (aSMR.getHref ());
              if (ret.put (sHref, aSMR.getHref ()) != null)
                aDuplicateURLCallback.accept (sHref);
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
                final String sHref = aSMPQueryParams.getSMPHostURI () +
                                     "/" +
                                     BDXR2ClientReadOnly.PATH_OASIS_BDXR_SMP_2 +
                                     aParticipantID.getURIPercentEncoded () +
                                     "/" +
                                     BDXR2ClientReadOnly.URL_PART_SERVICES +
                                     "/" +
                                     CIdentifier.getURIPercentEncoded (aDocTypeID);
                // Decoded href is important for unification
                if (ret.put (sHref, sHref) != null)
                  aDuplicateURLCallback.accept (sHref);
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
