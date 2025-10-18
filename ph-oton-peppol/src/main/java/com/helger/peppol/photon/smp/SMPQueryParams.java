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
package com.helger.peppol.photon.smp;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.peppol.photon.smlconfig.ISMLConfiguration;
import com.helger.peppol.servicedomain.EPeppolNetwork;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.smpclient.url.BDXLURLProvider;
import com.helger.smpclient.url.ISMPURLProvider;
import com.helger.smpclient.url.PeppolNaptrURLProvider;
import com.helger.smpclient.url.PeppolURLProvider;
import com.helger.smpclient.url.SMPDNSResolutionException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
  private EPeppolNetwork m_ePeppolNetwork;
  private IIdentifierFactory m_aIF;
  private IParticipantIdentifier m_aParticipantID;
  private URI m_aSMPHostURI;
  private boolean m_bTrustAllCerts = false;

  private SMPQueryParams ()
  {}

  @Nonnull
  public ISMLInfo getSMLInfo ()
  {
    return m_aSMLInfo;
  }

  @Nonnull
  public ESMPAPIType getSMPAPIType ()
  {
    return m_eSMPAPIType;
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

  @Nonnull
  public IIdentifierFactory getIF ()
  {
    return m_aIF;
  }

  @Nonnull
  public IParticipantIdentifier getParticipantID ()
  {
    return m_aParticipantID;
  }

  @Nonnull
  public URI getSMPHostURI ()
  {
    return m_aSMPHostURI;
  }

  public boolean isTrustAllCertificates ()
  {
    return m_bTrustAllCerts;
  }

  public static boolean isSMPRegisteredInDNSViaNaptr (@Nonnull final IParticipantIdentifier aParticipantID,
                                                      @Nonnull final String sSMLZoneName)
  {
    try
    {
      // Perform NAPTR resolution
      PeppolNaptrURLProvider.INSTANCE.getSMPURIOfParticipant (aParticipantID, sSMLZoneName);

      // Found it
      return true;
    }
    catch (final SMPDNSResolutionException ex)
    {
      return false;
    }
  }

  public boolean isSMPRegisteredInDNS ()
  {
    return isSMPRegisteredInDNSViaNaptr (m_aParticipantID, m_aSMLInfo.getDNSZone ());
  }

  @SuppressWarnings ({ "removal", "deprecation" })
  @Nonnull
  private static ISMPURLProvider _getURLProvider (@Nonnull final ESMPAPIType eAPIType, final boolean bUseCNAMELookup)
  {
    return eAPIType == ESMPAPIType.PEPPOL ? bUseCNAMELookup ? PeppolURLProvider.INSTANCE
                                                            : PeppolNaptrURLProvider.INSTANCE
                                          : BDXLURLProvider.INSTANCE;
  }

  @Nullable
  public static SMPQueryParams createForSMLOrNull (@Nonnull final ISMLConfiguration aSMLConfig,
                                                   @Nullable final String sParticipantIDScheme,
                                                   @Nullable final String sParticipantIDValue,
                                                   final boolean bUseCNAMELookup,
                                                   final boolean bLogOnError)
  {
    ValueEnforcer.notNull (aSMLConfig, "CurSML");

    final SMPQueryParams ret = new SMPQueryParams ();
    ret.m_aSMLInfo = aSMLConfig.getSMLInfo ();
    ret.m_eSMPAPIType = aSMLConfig.getSMPAPIType ();
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
      ret.m_aSMPHostURI = _getURLProvider (ret.m_eSMPAPIType, bUseCNAMELookup).getSMPURIOfParticipant (
                                                                                                       ret.m_aParticipantID,
                                                                                                       aSMLConfig.getSMLInfo ()
                                                                                                                 .getDNSZone ());
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
