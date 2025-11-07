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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.timing.StopWatch;
import com.helger.http.CHttp;
import com.helger.http.CHttpHeader;
import com.helger.httpclient.HttpClientSettings;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.peppol.api.config.PeppolSharedAPIConfig;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.photon.app.PhotonUnifiedResponse;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import jakarta.annotation.Nonnull;

public abstract class AbstractAPIExecutor implements IAPIExecutor
{
  public static final int DEFAULT_RETRY_AFTER_SECONDS = 5;
  protected static final AtomicInteger COUNTER = new AtomicInteger (0);

  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractAPIExecutor.class);

  protected final Consumer <? super HttpClientSettings> m_aHCSModifier;
  protected boolean m_bRateLimitEnabled = false;
  protected InMemorySlidingWindowRequestRateLimiter m_aRequestRateLimiter = null;

  /**
   * @param sUserAgent
   *        The user agent to be used for outbound HTTP connections.
   */
  protected AbstractAPIExecutor (@Nonnull @Nonempty final String sUserAgent)
  {
    ValueEnforcer.notEmpty (sUserAgent, "UserAgent");
    m_aHCSModifier = hcs -> { hcs.setUserAgent (sUserAgent); };
  }

  @Nonnull
  public final AbstractAPIExecutor setRateLimitEnabled (final boolean bEnabled)
  {
    if (bEnabled)
    {
      m_bRateLimitEnabled = true;

      final long nDurationSecs = PeppolSharedAPIConfig.getRestAPILimitDurationSeconds ();
      final long nRequestsInDuration = PeppolSharedAPIConfig.getRestAPILimitRequestsInDuration ();
      if (nDurationSecs >= 2 && nRequestsInDuration > 0)
      {
        // 2 request per second, per key
        // Note: duration must be > 1 second
        m_aRequestRateLimiter = new InMemorySlidingWindowRequestRateLimiter (RequestLimitRule.of (Duration.ofSeconds (nDurationSecs),
                                                                                                  nRequestsInDuration));
        LOGGER.info ("Installed REST API rate limiter with a maximum of " +
                     nRequestsInDuration +
                     " requests per " +
                     nDurationSecs +
                     " seconds for class " +
                     getClass ().getSimpleName ());
      }
      else
      {
        m_aRequestRateLimiter = null;
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("REST API runs without limit (" + nDurationSecs + "/" + nRequestsInDuration + ")");
      }
    }
    else
    {
      m_bRateLimitEnabled = false;
      m_aRequestRateLimiter = null;
    }
    return this;
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

    if (m_bRateLimitEnabled)
    {
      final String sRateLimitKey = "ip:" + aRequestScope.getRemoteAddr ();
      final boolean bOverRateLimit = m_aRequestRateLimiter != null ? m_aRequestRateLimiter.overLimitWhenIncremented (
                                                                                                                     sRateLimitKey)
                                                                   : false;
      if (bOverRateLimit)
      {
        // Too Many Requests
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug (sLogPrefix + "REST search rate limit exceeded for '" + sRateLimitKey + "'");

        aUnifiedResponse.setStatus (CHttp.HTTP_TOO_MANY_REQUESTS)
                        .addCustomResponseHeader (CHttpHeader.RETRY_AFTER,
                                                  Integer.toString (DEFAULT_RETRY_AFTER_SECONDS));
        return;
      }
    }

    final StopWatch aSW = StopWatch.createdStarted ();

    final PhotonUnifiedResponse aPUR = (PhotonUnifiedResponse) aUnifiedResponse;

    // Disable all caching by default
    aPUR.disableCaching ();

    // Default JSON result is formatted
    aPUR.setJsonWriterSettings (JsonWriterSettings.DEFAULT_SETTINGS_FORMATTED);

    // Go on
    invokeAPI (sLogPrefix, aAPIDescriptor, sPath, aPathVariables, aRequestScope, aPUR);

    // Measure time and log only above a certain threshold
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
