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
package com.helger.peppol.ui.types.smp;

import java.net.URI;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.peppol.servicedomain.EPeppolNetwork;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppol.ui.types.smlconfig.ISMLConfiguration;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.smpclient.url.BDXLURLProvider;
import com.helger.smpclient.url.ISMPURLProvider;
import com.helger.smpclient.url.PeppolNaptrURLProvider;
import com.helger.smpclient.url.SMPDNSResolutionException;

/**
 * The collection of needed SMP query parameters
 *
 * @author Philip Helger
 */
public final class SMPQueryParams
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SMPQueryParams.class);

  private ISMLInfo m_aSMLInfo;
  private ESMPAPIType m_eSMPAPIType;
  private ISMPURLProvider m_aSMPURLProvider;
  private EPeppolNetwork m_ePeppolNetwork;
  private IIdentifierFactory m_aIF;
  private IParticipantIdentifier m_aParticipantID;
  private URI m_aSMPHostURI;
  private boolean m_bTrustAllCerts = false;

  private SMPQueryParams ()
  {}

  @NonNull
  public ISMLInfo getSMLInfo ()
  {
    return m_aSMLInfo;
  }

  @NonNull
  public ESMPAPIType getSMPAPIType ()
  {
    return m_eSMPAPIType;
  }

  @NonNull
  public ISMPURLProvider getSMPURLProvider ()
  {
    return m_aSMPURLProvider;
  }

  @Nullable
  public EPeppolNetwork getPeppolNetwork ()
  {
    return m_ePeppolNetwork;
  }

  public boolean isPeppolNetworkProduction ()
  {
    return m_ePeppolNetwork != null && m_ePeppolNetwork.isProduction ();
  }

  @NonNull
  public IIdentifierFactory getIF ()
  {
    return m_aIF;
  }

  @NonNull
  public IParticipantIdentifier getParticipantID ()
  {
    return m_aParticipantID;
  }

  @NonNull
  public URI getSMPHostURI ()
  {
    return m_aSMPHostURI;
  }

  public boolean isTrustAllCertificates ()
  {
    return m_bTrustAllCerts;
  }

  @Nullable
  public static URI getSMURIViaNaptr (@NonNull final ISMPURLProvider aSMPURLProvider,
                                      @NonNull final IParticipantIdentifier aParticipantID,
                                      @NonNull final String sSMLZoneName)
  {
    try
    {
      return aSMPURLProvider.getSMPURIOfParticipant (aParticipantID, sSMLZoneName);
    }
    catch (final SMPDNSResolutionException ex)
    {
      return null;
    }
  }

  public static boolean isSMPRegisteredInDNSViaNaptr (@NonNull final ISMPURLProvider aSMPURLProvider,
                                                      @NonNull final IParticipantIdentifier aParticipantID,
                                                      @NonNull final String sSMLZoneName)
  {
    return getSMURIViaNaptr (aSMPURLProvider, aParticipantID, sSMLZoneName) != null;
  }

  public boolean isSMPRegisteredInDNS ()
  {
    return isSMPRegisteredInDNSViaNaptr (m_aSMPURLProvider, m_aParticipantID, m_aSMLInfo.getDNSZone ());
  }

  @Nullable
  public static SMPQueryParams createForSMLOrNull (@NonNull final ISMLConfiguration aSMLConfig,
                                                   @Nullable final String sParticipantIDScheme,
                                                   @Nullable final String sParticipantIDValue,
                                                   final boolean bLogOnError)
  {
    ValueEnforcer.notNull (aSMLConfig, "CurSML");

    final SMPQueryParams ret = new SMPQueryParams ();
    ret.m_aSMLInfo = aSMLConfig.getSMLInfo ();
    ret.m_eSMPAPIType = aSMLConfig.getSMPAPIType ();
    ret.m_aSMPURLProvider = ret.m_eSMPAPIType == ESMPAPIType.PEPPOL ? PeppolNaptrURLProvider.INSTANCE
                                                                    : BDXLURLProvider.INSTANCE;
    if (ret.m_eSMPAPIType == ESMPAPIType.PEPPOL)
    {
      ret.m_ePeppolNetwork = ESML.DIGIT_PRODUCTION.getID ().equals (aSMLConfig.getID ()) ? EPeppolNetwork.PRODUCTION
                                                                                         : EPeppolNetwork.TEST;
    }
    ret.m_aIF = aSMLConfig.getSMPIdentifierType ().getIdentifierFactory ();
    ret.m_aParticipantID = ret.m_aIF.createParticipantIdentifier (sParticipantIDScheme, sParticipantIDValue);
    if (ret.m_aParticipantID == null)
    {
      // Participant ID is invalid for this scheme
      if (bLogOnError)
        LOGGER.warn ("Failed to parse participant ID '" + sParticipantIDScheme + "' and '" + sParticipantIDValue + "'");
      return null;
    }

    try
    {
      ret.m_aSMPHostURI = ret.m_aSMPURLProvider.getSMPURIOfParticipant (ret.m_aParticipantID,
                                                                        aSMLConfig.getSMLInfo ().getDNSZone ());
      if ("https".equals (ret.m_aSMPHostURI.getScheme ()))
        ret.m_bTrustAllCerts = true;
    }
    catch (final SMPDNSResolutionException ex)
    {
      // For BDXL lookup -> no such participant
      if (bLogOnError)
        LOGGER.warn ("Failed to resolve participant " + ret.m_aParticipantID + " in DNS");
      return null;
    }
    return ret;
  }
}
