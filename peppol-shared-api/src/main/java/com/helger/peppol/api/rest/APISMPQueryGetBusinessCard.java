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
package com.helger.peppol.api.rest;

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
import com.helger.peppol.businesscard.generic.PDBusinessCard;
import com.helger.peppol.ui.types.feedbackcb.FeedbackCallbackLog;
import com.helger.peppol.ui.types.mgr.PhotonPeppolMetaManager;
import com.helger.peppol.ui.types.smlconfig.ISMLConfiguration;
import com.helger.peppol.ui.types.smlconfig.ISMLConfigurationManager;
import com.helger.peppol.ui.types.smp.SMPQueryParams;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.SimpleIdentifierFactory;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.app.PhotonUnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

public final class APISMPQueryGetBusinessCard extends AbstractAPIExecutor
{
  private static final Logger LOGGER = LoggerFactory.getLogger (APISMPQueryGetBusinessCard.class);

  public APISMPQueryGetBusinessCard (@NonNull @Nonempty final String sUserAgent)
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
    ISMLConfiguration aSMLConfig = aSMLConfigurationMgr.getSMLInfoOfID (sSMLID);
    if (aSMLConfig == null && !bSMLAutoDetect)
      throw new APIParamException ("Unsupported SML ID '" + sSMLID + "' provided.");

    final String sParticipantID = aPathVariables.get (PeppolSharedRestAPI.PARAM_PARTICIPANT_ID);
    final IParticipantIdentifier aPID = SimpleIdentifierFactory.INSTANCE.parseParticipantIdentifier (sParticipantID);
    if (aPID == null)
      throw new APIParamException ("Invalid participant ID '" + sParticipantID + "' provided.");

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

    // Main SMP query inside
    final PDBusinessCard aBC = PeppolAPIHelper.retrieveBusinessCardParsed (sLogPrefix,
                                                                           aSMPQueryParams,
                                                                           m_aHCSModifier,
                                                                           new FeedbackCallbackLog (LOGGER, sLogPrefix),
                                                                           ex -> {});
    final IJsonObject aBCJson = aBC == null ? null : aBC.getAsJson ();

    aSW.stop ();

    if (aBCJson == null)
    {
      final String sMsg = "Failed to resolve BusinessCard for participant ID '" +
                          sParticipantID +
                          "' for the provided SML '" +
                          aSMPQueryParams.getSMLInfo ().getID () +
                          "'";
      LOGGER.warn (sLogPrefix + sMsg);
      aUnifiedResponse.createNotFound ().text (sMsg);
    }
    else
    {
      LOGGER.info (sLogPrefix +
                   "Succesfully finished BusinessCard lookup lookup after " +
                   aSW.getMillis () +
                   " milliseconds");

      aBCJson.add ("queryDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format (aQueryDT));
      aBCJson.add ("queryDurationMillis", aSW.getMillis ());

      aUnifiedResponse.json (aBCJson).enableCaching (3 * CGlobal.SECONDS_PER_HOUR);
    }
  }
}
