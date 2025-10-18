/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.timing.StopWatch;
import com.helger.httpclient.HttpClientSettings;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.photon.app.PhotonUnifiedResponse;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import jakarta.annotation.Nonnull;

public abstract class AbstractAPIExecutor implements IAPIExecutor
{
  protected static final AtomicInteger COUNTER = new AtomicInteger (0);

  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractAPIExecutor.class);

  protected final Consumer <? super HttpClientSettings> m_aHCSModifier;

  /**
   * @param sUserAgent
   *        The user agent to be used for outbound HTTP connections.
   */
  protected AbstractAPIExecutor (@Nonnull @Nonempty final String sUserAgent)
  {
    ValueEnforcer.notEmpty (sUserAgent, "UserAgent");
    m_aHCSModifier = hcs -> { hcs.setUserAgent (sUserAgent); };
  }

  protected abstract void invokeAPI (@Nonnull @Nonempty String sLogPrefix,
                                     @Nonnull IAPIDescriptor aAPIDescriptor,
                                     @Nonnull @Nonempty String sPath,
                                     @Nonnull Map <String, String> aPathVariables,
                                     @Nonnull IRequestWebScopeWithoutResponse aRequestScope,
                                     @Nonnull PhotonUnifiedResponse aUnifiedResponse) throws Exception;

  public final void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                               @Nonnull @Nonempty final String sPath,
                               @Nonnull final Map <String, String> aPathVariables,
                               @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                               @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    final String sLogPrefix = "[API-" + COUNTER.incrementAndGet () + "] ";
    final StopWatch aSW = StopWatch.createdStarted ();

    final PhotonUnifiedResponse aPUR = (PhotonUnifiedResponse) aUnifiedResponse;
    aPUR.disableCaching ();
    aPUR.setJsonWriterSettings (JsonWriterSettings.DEFAULT_SETTINGS_FORMATTED);
    invokeAPI (sLogPrefix, aAPIDescriptor, sPath, aPathVariables, aRequestScope, aPUR);

    aSW.stop ();
    if (aSW.getMillis () > 100)
      LOGGER.info (sLogPrefix +
                   "Succesfully finished '" +
                   aAPIDescriptor.getPathDescriptor ().getAsURLString () +
                   "' after " +
                   aSW.getMillis () +
                   " milliseconds");
  }
}
