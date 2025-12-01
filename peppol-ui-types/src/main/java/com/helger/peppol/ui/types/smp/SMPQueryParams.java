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

import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.ESuccess;
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
@NotThreadSafe
public class SMPQueryParams
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SMPQueryParams.class);

  private @NonNull final ISMLInfo m_aSMLInfo;
  private @NonNull final ESMPAPIType m_eSMPAPIType;
  private @NonNull final IIdentifierFactory m_aIF;
  private @NonNull ISMPURLProvider m_aSMPURLProvider;
  private @Nullable final EPeppolNetwork m_ePeppolNetwork;
  private IParticipantIdentifier m_aParticipantID;
  private URI m_aSMPHostURI;
  private boolean m_bTrustAllCerts = false;

  protected SMPQueryParams (@NonNull final ISMLInfo aSMLInfo,
                            @NonNull final ESMPAPIType eSMPAPIType,
                            @NonNull final IIdentifierFactory aIF)
  {
    m_aSMLInfo = aSMLInfo;
    m_eSMPAPIType = eSMPAPIType;
    m_aIF = aIF;
    if (eSMPAPIType == ESMPAPIType.PEPPOL)
    {
      m_aSMPURLProvider = PeppolNaptrURLProvider.INSTANCE;
      m_ePeppolNetwork = ESML.DIGIT_PRODUCTION.getID ().equals (aSMLInfo.getID ()) ? EPeppolNetwork.PRODUCTION
                                                                                   : EPeppolNetwork.TEST;
    }
    else
    {
      m_aSMPURLProvider = BDXLURLProvider.INSTANCE;
      m_ePeppolNetwork = null;
    }
  }

  @NonNull
  public final ISMLInfo getSMLInfo ()
  {
    return m_aSMLInfo;
  }

  @NonNull
  public final ESMPAPIType getSMPAPIType ()
  {
    return m_eSMPAPIType;
  }

  @NonNull
  public final ISMPURLProvider getSMPURLProvider ()
  {
    return m_aSMPURLProvider;
  }

  /**
   * Set a custom URL provider. This is e.g. needed for HR eDelivery to pick the right resolver.
   *
   * @param aURLProvider
   *        The URL provider to use. May not be <code>null</code>.
   */
  public void setSMPURLProvider (@NonNull final ISMPURLProvider aURLProvider)
  {
    ValueEnforcer.notNull (aURLProvider, "URLProvider");
    m_aSMPURLProvider = aURLProvider;
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

  /**
   * @return The SMP host URI. Should never end with a slash.
   */
  @NonNull
  public URI getSMPHostURI ()
  {
    return m_aSMPHostURI;
  }

  public boolean isTrustAllCertificates ()
  {
    return m_bTrustAllCerts;
  }

  public boolean isSMPRegisteredInDNS ()
  {
    return PeppolExistenceCheck.isSMPRegisteredInDNSViaNaptr (m_aSMPURLProvider,
                                                              m_aParticipantID,
                                                              getSMLInfo ().getDNSZone ());
  }

  @NonNull
  protected static ESuccess fillSMPQueryParams (@NonNull final SMPQueryParams aSMPQueryParams,
                                                @Nullable final String sParticipantIDScheme,
                                                @Nullable final String sParticipantIDValue,
                                                final boolean bLogOnError)
  {
    ValueEnforcer.notNull (aSMPQueryParams, "SMPQueryParams");
    aSMPQueryParams.m_aParticipantID = aSMPQueryParams.getIF ()
                                                      .createParticipantIdentifier (sParticipantIDScheme,
                                                                                    sParticipantIDValue);
    if (aSMPQueryParams.m_aParticipantID == null)
    {
      // Participant ID is invalid for this scheme
      if (bLogOnError)
        LOGGER.error ("Failed to parse participant ID '" +
                      sParticipantIDScheme +
                      "' and '" +
                      sParticipantIDValue +
                      "'");
      return ESuccess.FAILURE;
    }

    // Do this as the last action - it's the DNS lookup
    try
    {
      aSMPQueryParams.m_aSMPHostURI = aSMPQueryParams.m_aSMPURLProvider.getSMPURIOfParticipant (aSMPQueryParams.m_aParticipantID,
                                                                                                aSMPQueryParams.getSMLInfo ()
                                                                                                               .getDNSZone ());
      if ("https".equals (aSMPQueryParams.m_aSMPHostURI.getScheme ()))
        aSMPQueryParams.m_bTrustAllCerts = true;
      return ESuccess.SUCCESS;
    }
    catch (final SMPDNSResolutionException ex)
    {
      // For NAPTR lookup -> no such participant
      if (bLogOnError)
        LOGGER.error ("Failed to resolve participant " +
                      aSMPQueryParams.m_aParticipantID +
                      " in DNS: " +
                      ex.getMessage ());
      return ESuccess.FAILURE;
    }
  }

  @Nullable
  public static SMPQueryParams createForSMLOrNull (@NonNull final ISMLConfiguration aSMLConfig,
                                                   @Nullable final String sParticipantIDScheme,
                                                   @Nullable final String sParticipantIDValue,
                                                   final boolean bLogOnError)
  {
    ValueEnforcer.notNull (aSMLConfig, "SMLConfig");
    return createForSMLOrNull (aSMLConfig.getSMLInfo (),
                               aSMLConfig.getSMPAPIType (),
                               aSMLConfig.getSMPIdentifierType ().getIdentifierFactory (),
                               sParticipantIDScheme,
                               sParticipantIDValue,
                               bLogOnError);
  }

  @Nullable
  public static SMPQueryParams createForSMLOrNull (@NonNull final ISMLInfo aSMLInfo,
                                                   @NonNull final ESMPAPIType eSMPAPIType,
                                                   @NonNull final IIdentifierFactory aIF,
                                                   @Nullable final String sParticipantIDScheme,
                                                   @Nullable final String sParticipantIDValue,
                                                   final boolean bLogOnError)
  {
    ValueEnforcer.notNull (aSMLInfo, "SMLInfo");
    final SMPQueryParams ret = new SMPQueryParams (aSMLInfo, eSMPAPIType, aIF);
    if (fillSMPQueryParams (ret, sParticipantIDScheme, sParticipantIDValue, bLogOnError).isSuccess ())
      return ret;
    return null;
  }
}
