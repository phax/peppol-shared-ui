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
package com.helger.peppol.ui.types.smp;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppol.ui.types.smlconfig.ISMLConfiguration;
import com.helger.peppolid.factory.IIdentifierFactory;

/**
 * Special SMP query parameters with UI related information as well
 *
 * @author Philip Helger
 */
public class SMPQueryParamsUI extends SMPQueryParams
{
  private final boolean m_bIsProductionSML;

  protected SMPQueryParamsUI (@NonNull final ISMLInfo aSMLInfo,
                              @NonNull final ESMPAPIType eSMPAPIType,
                              @NonNull final IIdentifierFactory aIF,
                              final boolean bIsProductionSML)
  {
    super (aSMLInfo, eSMPAPIType, aIF);
    m_bIsProductionSML = bIsProductionSML;
  }

  public final boolean isProductionSML ()
  {
    return m_bIsProductionSML;
  }

  @Nullable
  public static SMPQueryParamsUI createForSMLOrNull (@NonNull final ISMLConfiguration aSMLConfig,
                                                     @Nullable final String sParticipantIDScheme,
                                                     @Nullable final String sParticipantIDValue,
                                                     final boolean bLogOnError)
  {
    ValueEnforcer.notNull (aSMLConfig, "SMLConfig");
    final SMPQueryParamsUI ret = new SMPQueryParamsUI (aSMLConfig.getSMLInfo (),
                                                       aSMLConfig.getSMPAPIType (),
                                                       aSMLConfig.getSMPIdentifierType ().getIdentifierFactory (),
                                                       aSMLConfig.isProduction ());
    if (fillSMPQueryParams (ret, sParticipantIDScheme, sParticipantIDValue, bLogOnError).isSuccess ())
      return ret;
    return null;
  }
}
