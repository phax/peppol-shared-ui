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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.http.CHttp;
import com.helger.peppol.api.config.PeppolSharedAPIConfig;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.app.PhotonUnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import jakarta.annotation.Nonnull;

public abstract class AbstractRateLimitingAPIExecutor extends AbstractAPIExecutor
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractRateLimitingAPIExecutor.class);

  protected final InMemorySlidingWindowRequestRateLimiter m_aRequestRateLimiter;

  /**
   * @param sUserAgent
   *        The user agent to be used for outbound HTTP connections.
   */
  protected AbstractRateLimitingAPIExecutor (@Nonnull @Nonempty final String sUserAgent)
  {
    super (sUserAgent);

    final long nRequestsPerSec = PeppolSharedAPIConfig.getRestAPIMaxRequestsPerSecond ();
    if (nRequestsPerSec > 0)
    {
      // 2 request per second, per key
      // Note: duration must be > 1 second
      m_aRequestRateLimiter = new InMemorySlidingWindowRequestRateLimiter (RequestLimitRule.of (Duration.ofSeconds (2),
                                                                                                nRequestsPerSec * 2));
      LOGGER.info ("Installed REST API rate limiter with a maximum of " +
                   nRequestsPerSec +
                   " requests per second for class " +
                   getClass ().getSimpleName ());
    }
    else
    {
      m_aRequestRateLimiter = null;
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("REST API runs without limit");
    }
  }

  protected abstract void rateLimitedInvokeAPI (@Nonnull @Nonempty String sLogPrefix,
                                                @Nonnull IAPIDescriptor aAPIDescriptor,
                                                @Nonnull @Nonempty String sPath,
                                                @Nonnull Map <String, String> aPathVariables,
                                                @Nonnull IRequestWebScopeWithoutResponse aRequestScope,
                                                @Nonnull PhotonUnifiedResponse aUnifiedResponse) throws Exception;

  @Override
  public final void invokeAPI (@Nonnull @Nonempty final String sLogPrefix,
                               @Nonnull final IAPIDescriptor aAPIDescriptor,
                               @Nonnull @Nonempty final String sPath,
                               @Nonnull final Map <String, String> aPathVariables,
                               @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                               @Nonnull final PhotonUnifiedResponse aUnifiedResponse) throws Exception
  {
    final String sRateLimitKey = "ip:" + aRequestScope.getRemoteAddr ();
    final boolean bOverRateLimit = m_aRequestRateLimiter != null ? m_aRequestRateLimiter.overLimitWhenIncremented (
                                                                                                                   sRateLimitKey)
                                                                 : false;

    if (bOverRateLimit)
    {
      // Too Many Requests
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug (sLogPrefix + "REST search rate limit exceeded for " + sRateLimitKey);

      aUnifiedResponse.setStatus (CHttp.HTTP_TOO_MANY_REQUESTS);
    }
    else
    {
      rateLimitedInvokeAPI (sLogPrefix, aAPIDescriptor, sPath, aPathVariables, aRequestScope, aUnifiedResponse);
    }
  }
}
