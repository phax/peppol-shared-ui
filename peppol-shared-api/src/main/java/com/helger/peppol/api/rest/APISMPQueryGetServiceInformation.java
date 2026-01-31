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

import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.base.CGlobal;
import com.helger.base.timing.StopWatch;
import com.helger.datetime.helper.PDTFactory;
import com.helger.json.IJsonObject;
import com.helger.peppol.ui.types.PeppolUITypes;
import com.helger.peppol.ui.types.mgr.PhotonPeppolMetaManager;
import com.helger.peppol.ui.types.smlconfig.ISMLConfiguration;
import com.helger.peppol.ui.types.smlconfig.ISMLConfigurationManager;
import com.helger.peppol.ui.types.smp.SMPQueryParams;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.SimpleIdentifierFactory;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.app.PhotonUnifiedResponse;
import com.helger.smpclient.bdxr1.BDXRClientReadOnly;
import com.helger.smpclient.bdxr2.BDXR2ClientReadOnly;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.smpclient.peppol.SMPClientReadOnly;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

public final class APISMPQueryGetServiceInformation extends AbstractAPIExecutor
{
  public static final String PARAM_VERIFY_SIGNATURE = "verifySignature";
  public static final String PARAM_XML_SCHEMA_VALIDATION = "xmlSchemaValidation";

  private static final Logger LOGGER = LoggerFactory.getLogger (APISMPQueryGetServiceInformation.class);

  public APISMPQueryGetServiceInformation (@NonNull @Nonempty final String sUserAgent)
  {
    super (sUserAgent);
  }

  @Override
  protected void invokeAPI (@NonNull @Nonempty final String sLogPrefix,
                            @NonNull final IAPIDescriptor aAPIDescriptor,
                            @NonNull @Nonempty final String sPath,
                            @NonNull final Map <String, String> aPathVariables,
                            @NonNull final IRequestWebScopeWithoutResponse aRequestScope,
                            @NonNull final PhotonUnifiedResponse aUnifiedResponse) throws Exception
  {
    final ISMLConfigurationManager aSMLConfigurationMgr = PhotonPeppolMetaManager.getSMLConfigurationMgr ();
    final String sSMLID = aPathVariables.get (PeppolSharedRestAPI.PARAM_SML_ID);
    final boolean bSMLAutoDetect = ISMLConfigurationManager.ID_AUTO_DETECT.equals (sSMLID);
    ISMLConfiguration aSMLConfig = aSMLConfigurationMgr.getSMLConfigurationfID (sSMLID);
    if (aSMLConfig == null && !bSMLAutoDetect)
      throw new APIParamException ("Unsupported SML ID '" + sSMLID + "' provided.");

    final String sParticipantID = aPathVariables.get (PeppolSharedRestAPI.PARAM_PARTICIPANT_ID);
    final IParticipantIdentifier aPID = SimpleIdentifierFactory.INSTANCE.parseParticipantIdentifier (sParticipantID);
    if (aPID == null)
      throw new APIParamException ("Invalid participant ID '" + sParticipantID + "' provided.");

    final String sDocTypeID = aPathVariables.get (PeppolSharedRestAPI.PARAM_DOCTYPE_ID);
    final IDocumentTypeIdentifier aDTID = SimpleIdentifierFactory.INSTANCE.parseDocumentTypeIdentifier (sDocTypeID);
    if (aDTID == null)
      throw new APIParamException ("Invalid document type ID '" + sDocTypeID + "' provided.");

    final boolean bXMLSchemaValidation = aRequestScope.params ().getAsBoolean (PARAM_XML_SCHEMA_VALIDATION, true);
    final boolean bVerifySignature = aRequestScope.params ().getAsBoolean (PARAM_VERIFY_SIGNATURE, true);

    final ZonedDateTime aQueryDT = PDTFactory.getCurrentZonedDateTimeUTC ();
    final StopWatch aSW = StopWatch.createdStarted ();

    SMPQueryParams aSMPQueryParams = null;
    if (bSMLAutoDetect)
    {
      for (final ISMLConfiguration aCurSML : aSMLConfigurationMgr.getAllSorted ())
      {
        aSMPQueryParams = SMPQueryParams.createForSMLOrNull (aCurSML, aPID.getScheme (), aPID.getValue (), false);
        if (aSMPQueryParams != null && aSMPQueryParams.isSMPRegisteredInDNS ())
        {
          // Found it
          aSMLConfig = aCurSML;
          break;
        }
      }

      // Ensure to go into the exception handler
      if (aSMLConfig == null)
      {
        final String sMsg = "The participant identifier '" + sParticipantID + "' could not be found in any SML.";
        LOGGER.warn (sLogPrefix + sMsg);
        aUnifiedResponse.createNotFound ().text (sMsg);
        return;
      }
    }
    else
    {
      aSMPQueryParams = SMPQueryParams.createForSMLOrNull (aSMLConfig, aPID.getScheme (), aPID.getValue (), true);
    }
    if (aSMPQueryParams == null)
    {
      final String sMsg = "Failed to resolve participant ID '" +
                          sParticipantID +
                          "' for the provided SML '" +
                          aSMLConfig.getID () +
                          "'";
      LOGGER.warn (sLogPrefix + sMsg);
      aUnifiedResponse.createNotFound ().text (sMsg);
      return;
    }

    final IParticipantIdentifier aParticipantID = aSMPQueryParams.getParticipantID ();
    final IDocumentTypeIdentifier aDocTypeID = aSMPQueryParams.getIF ()
                                                              .createDocumentTypeIdentifier (aDTID.getScheme (),
                                                                                             aDTID.getValue ());
    if (aDocTypeID == null)
    {
      final String sMsg = "Failed to resolve document type ID '" +
                          sDocTypeID +
                          "' for participant ID '" +
                          sParticipantID +
                          "' for the provided SML '" +
                          aSMLConfig.getID () +
                          "'";
      LOGGER.warn (sLogPrefix + sMsg);
      aUnifiedResponse.createNotFound ().text (sMsg);
      return;
    }

    LOGGER.info (sLogPrefix +
                 "Participant information of '" +
                 aParticipantID.getURIEncoded () +
                 "' is queried using SMP API '" +
                 aSMPQueryParams.getSMPAPIType () +
                 "' from '" +
                 aSMPQueryParams.getSMPHostURI () +
                 "' using SML '" +
                 aSMLConfig.getID () +
                 "' for document type '" +
                 aDocTypeID.getURIEncoded () +
                 "'; XSD validation=" +
                 bXMLSchemaValidation +
                 "; signature verification=" +
                 bVerifySignature);

    IJsonObject aJson = null;
    switch (aSMPQueryParams.getSMPAPIType ())
    {
      case PEPPOL:
      {
        final SMPClientReadOnly aSMPClient = new SMPClientReadOnly (aSMPQueryParams.getSMPHostURI ());
        aSMPClient.setSecureValidation (PeppolUITypes.DEFAULT_SMP_USE_SECURE_VALIDATION);
        aSMPClient.withHttpClientSettings (m_aHCSModifier);
        aSMPClient.setXMLSchemaValidation (bXMLSchemaValidation);
        aSMPClient.setVerifySignature (bVerifySignature);
        if (aSMPQueryParams.isTrustAllCertificates ())
          try
          {
            aSMPClient.httpClientSettings ().setSSLContextTrustAll ();
          }
          catch (final GeneralSecurityException ex)
          {
            // Ignore
          }

        final var aSSM = aSMPClient.getSchemeSpecificServiceMetadataOrNull (aParticipantID, aDocTypeID);
        if (aSSM != null)
        {
          final com.helger.xsds.peppol.smp1.ServiceMetadataType aSM = aSSM.getServiceMetadata ();
          aJson = SMPJsonResponse.convert (aParticipantID, aDocTypeID, aSM);
        }
        break;
      }
      case OASIS_BDXR_V1:
      {
        final BDXRClientReadOnly aBDXR1Client = new BDXRClientReadOnly (aSMPQueryParams.getSMPHostURI ());
        aBDXR1Client.setSecureValidation (PeppolUITypes.DEFAULT_SMP_USE_SECURE_VALIDATION);
        aBDXR1Client.withHttpClientSettings (m_aHCSModifier);
        aBDXR1Client.setXMLSchemaValidation (bXMLSchemaValidation);
        aBDXR1Client.setVerifySignature (bVerifySignature);
        if (aSMPQueryParams.isTrustAllCertificates ())
          try
          {
            aBDXR1Client.httpClientSettings ().setSSLContextTrustAll ();
          }
          catch (final GeneralSecurityException ex)
          {
            // Ignore
          }

        final var aSSM = aBDXR1Client.getServiceMetadataOrNull (aParticipantID, aDocTypeID);
        if (aSSM != null)
        {
          final var aSM = aSSM.getServiceMetadata ();
          aJson = SMPJsonResponse.convert (aParticipantID, aDocTypeID, aSM);
        }
        break;
      }
      case OASIS_BDXR_V2:
      {
        final BDXR2ClientReadOnly aBDXR2Client = new BDXR2ClientReadOnly (aSMPQueryParams.getSMPHostURI ());
        aBDXR2Client.setSecureValidation (PeppolUITypes.DEFAULT_SMP_USE_SECURE_VALIDATION);
        aBDXR2Client.withHttpClientSettings (m_aHCSModifier);
        aBDXR2Client.setXMLSchemaValidation (bXMLSchemaValidation);
        aBDXR2Client.setVerifySignature (bVerifySignature);
        if (aSMPQueryParams.isTrustAllCertificates ())
          try
          {
            aBDXR2Client.httpClientSettings ().setSSLContextTrustAll ();
          }
          catch (final GeneralSecurityException ex)
          {
            // Ignore
          }

        final var aSM = aBDXR2Client.getServiceMetadataOrNull (aParticipantID, aDocTypeID);
        if (aSM != null)
        {
          aJson = SMPJsonResponse.convert (aParticipantID, aDocTypeID, aSM);
        }
        break;
      }
    }

    aSW.stop ();

    if (aJson == null)
    {
      final String sMsg = "Failed to perform the SMP lookup for participant ID '" +
                          sParticipantID +
                          "' for the provided SML '" +
                          aSMLConfig.getID () +
                          "'";
      LOGGER.warn (sLogPrefix + sMsg);
      aUnifiedResponse.createNotFound ().text (sMsg);
    }
    else
    {
      LOGGER.info (sLogPrefix + "Succesfully finished lookup lookup after " + aSW.getMillis () + " milliseconds");

      aJson.add ("queryDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format (aQueryDT));
      aJson.add ("queryDurationMillis", aSW.getMillis ());

      aUnifiedResponse.json (aJson).enableCaching (3 * CGlobal.SECONDS_PER_HOUR);
    }
  }
}
