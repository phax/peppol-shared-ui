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

import com.helger.annotation.concurrent.Immutable;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.smpclient.url.ISMPURLProvider;
import com.helger.smpclient.url.SMPDNSResolutionException;

/**
 * Specific helper classes to check if a Peppol Participant is registered in Peppol or not.
 *
 * @author Philip Helger
 */
@Immutable
public final class PeppolExistenceCheck
{
  private PeppolExistenceCheck ()
  {}

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
}
