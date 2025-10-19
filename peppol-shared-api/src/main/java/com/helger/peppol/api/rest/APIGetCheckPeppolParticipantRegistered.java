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

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.base.timing.StopWatch;
import com.helger.datetime.helper.PDTFactory;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.ui.types.mgr.PhotonPeppolMetaManager;
import com.helger.peppol.ui.types.smlconfig.ISMLConfiguration;
import com.helger.peppol.ui.types.smlconfig.ISMLConfigurationManager;
import com.helger.peppol.ui.types.smp.SMPQueryParams;
import com.helger.peppolid.CIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.PeppolIdentifierFactory;
import com.helger.peppolid.peppol.PeppolIdentifierHelper;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.app.PhotonUnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import jakarta.annotation.Nonnull;

/**
 * Determine if a participant ID is registered in the Peppol Network or not.
 *
 * @author Philip Helger
 */
public final class APIGetCheckPeppolParticipantRegistered extends AbstractAPIExecutor
{
  private static final Logger LOGGER = LoggerFactory.getLogger (APIGetCheckPeppolParticipantRegistered.class);

  public APIGetCheckPeppolParticipantRegistered (@Nonnull @Nonempty final String sUserAgent)
  {
    super (sUserAgent);
  }

  @Override
  public void invokeAPI (@Nonnull @Nonempty final String sLogPrefix,
                         @Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final PhotonUnifiedResponse aUnifiedResponse) throws IOException
  {
    final ISMLConfigurationManager aSMLConfigurationMgr = PhotonPeppolMetaManager.getSMLConfigurationMgr ();

    final String sSMLID = aPathVariables.get (PeppolSharedRestAPI.PARAM_SML_ID);
    final boolean bSMLAutoDetect = ISMLConfigurationManager.ID_AUTO_DETECT.equals (sSMLID);
    final ISMLConfiguration aSMLConf = aSMLConfigurationMgr.getSMLInfoOfID (sSMLID);
    if (aSMLConf == null && !bSMLAutoDetect)
      throw new APIParamException ("Unsupported SML ID '" + sSMLID + "' provided.");

    String sParticipantID = aPathVariables.get (PeppolSharedRestAPI.PARAM_PARTICIPANT_ID);
    if (sParticipantID != null)
    {
      // Add prefix on demand
      if (!sParticipantID.startsWith (PeppolIdentifierHelper.PARTICIPANT_SCHEME_ISO6523_ACTORID_UPIS))
        sParticipantID = CIdentifier.getURIEncoded (PeppolIdentifierHelper.PARTICIPANT_SCHEME_ISO6523_ACTORID_UPIS,
                                                    sParticipantID);
    }

    final IParticipantIdentifier aParticipantID = PeppolIdentifierFactory.INSTANCE.parseParticipantIdentifier (sParticipantID);
    if (aParticipantID == null)
    {
      LOGGER.error (sLogPrefix + "The provided Peppol Participant ID '" + sParticipantID + "' is invalid");
      aUnifiedResponse.createBadRequest ();
      return;
    }
    final ZonedDateTime aQueryDT = PDTFactory.getCurrentZonedDateTimeUTC ();
    final StopWatch aSW = StopWatch.createdStarted ();

    ISMLConfiguration aEffectiveSMLConf = null;
    boolean bRegistered;
    if (bSMLAutoDetect)
    {
      bRegistered = false;
      for (final ISMLConfiguration aCurSMLConf : aSMLConfigurationMgr.getAllSorted ())
      {
        bRegistered = SMPQueryParams.isSMPRegisteredInDNSViaNaptr (ESMPAPIType.PEPPOL,
                                                                   aParticipantID,
                                                                   aCurSMLConf.getSMLInfo ().getDNSZone ());
        if (bRegistered)
        {
          aEffectiveSMLConf = aCurSMLConf;
          break;
        }
      }
    }
    else
    {
      bRegistered = SMPQueryParams.isSMPRegisteredInDNSViaNaptr (ESMPAPIType.PEPPOL,
                                                                 aParticipantID,
                                                                 aSMLConf.getSMLInfo ().getDNSZone ());
      if (bRegistered)
        aEffectiveSMLConf = aSMLConf;
    }

    final IJsonObject aJson = new JsonObject ();
    aJson.add ("participantID", aParticipantID.getURIEncoded ());
    aJson.add ("sml", sSMLID);
    if (aEffectiveSMLConf != null)
    {
      aJson.add ("smpHostURI",
                 SMPQueryParams.getSMURIViaNaptr (ESMPAPIType.PEPPOL,
                                                  aParticipantID,
                                                  aEffectiveSMLConf.getSMLInfo ().getDNSZone ()));
    }
    // This is the main check result
    aJson.add ("exists", bRegistered);
    aSW.stop ();

    aJson.add ("queryDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format (aQueryDT));
    aJson.add ("queryDurationMillis", aSW.getMillis ());

    if (bRegistered)
    {
      LOGGER.info (sLogPrefix +
                   "Peppol Participant ID '" +
                   sParticipantID +
                   "' is registered in DNS. Took " +
                   aSW.getMillis () +
                   " milliseconds");
      aUnifiedResponse.json (aJson);
    }
    else
    {
      LOGGER.warn (sLogPrefix +
                   "Peppol Participant ID '" +
                   sParticipantID +
                   "' is NOT registered in DNS. Took " +
                   aSW.getMillis () +
                   " milliseconds");
      aUnifiedResponse.createNotFound ().json (aJson);
    }
  }
}
