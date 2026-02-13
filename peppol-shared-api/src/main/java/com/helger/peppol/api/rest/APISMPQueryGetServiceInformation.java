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

import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.base.CGlobal;
import com.helger.base.numeric.mutable.MutableBoolean;
import com.helger.json.IJsonObject;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.app.PhotonUnifiedResponse;
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
    final String sSMLID = aPathVariables.get (PeppolSharedRestAPI.PARAM_SML_ID);
    final String sParticipantID = aPathVariables.get (PeppolSharedRestAPI.PARAM_PARTICIPANT_ID);
    final String sDocTypeID = aPathVariables.get (PeppolSharedRestAPI.PARAM_DOCTYPE_ID);
    final boolean bXMLSchemaValidation = aRequestScope.params ().getAsBoolean (PARAM_XML_SCHEMA_VALIDATION, true);
    final boolean bVerifySignature = aRequestScope.params ().getAsBoolean (PARAM_VERIFY_SIGNATURE, true);

    final MutableBoolean aResponseSet = new MutableBoolean (false);
    final IJsonObject aJson = PeppolAPIHelper.getServiceInformationAsJson (sSMLID,
                                                                           sParticipantID,
                                                                           sDocTypeID,
                                                                           bXMLSchemaValidation,
                                                                           bVerifySignature,
                                                                           sLogPrefix,
                                                                           m_aHCSModifier,
                                                                           null,
                                                                           sMsg -> {
                                                                             LOGGER.warn (sLogPrefix + sMsg);
                                                                             aUnifiedResponse.createNotFound ()
                                                                                             .text (sMsg);
                                                                             aResponseSet.set (true);
                                                                           });

    if (aJson == null)
    {
      // Response object is filled
      return;
    }

    aUnifiedResponse.json (aJson).enableCaching (3 * CGlobal.SECONDS_PER_HOUR);
  }
}
