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

import org.apache.hc.client5.http.HttpResponseException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.debug.GlobalDebug;
import com.helger.base.state.EHandled;
import com.helger.base.string.StringHelper;
import com.helger.peppol.api.config.PeppolSharedAPIConfig;
import com.helger.photon.api.AbstractAPIExceptionMapper;
import com.helger.photon.api.InvokableAPIDescriptor;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Special API exception mapper for the SMP REST API.
 *
 * @author Philip Helger
 */
public class APIExceptionMapper extends AbstractAPIExceptionMapper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (APIExceptionMapper.class);

  private static void _logRestException (@NonNull final String sMsg, @NonNull final Throwable t)
  {
    if (PeppolSharedAPIConfig.isRestLogExceptions ())
      LOGGER.error (sMsg, t);
    else
      LOGGER.error (sMsg +
                    " - " +
                    getResponseEntityWithoutStackTrace (t) +
                    " (turn on REST exception logging to see all details)");
  }

  private static void _setSimpleTextResponse (@NonNull final UnifiedResponse aUnifiedResponse,
                                              final int nStatusCode,
                                              @Nullable final String sContent)
  {
    if (PeppolSharedAPIConfig.isRestExceptionsWithPayload ())
    {
      // With payload
      setSimpleTextResponse (aUnifiedResponse, nStatusCode, sContent);
      if (StringHelper.isNotEmpty (sContent))
        aUnifiedResponse.disableCaching ();
    }
    else
    {
      // No payload
      aUnifiedResponse.setStatus (nStatusCode);
    }
  }

  @NonNull
  public EHandled applyExceptionOnResponse (@NonNull final InvokableAPIDescriptor aInvokableDescriptor,
                                            @NonNull final IRequestWebScopeWithoutResponse aRequestScope,
                                            @NonNull final UnifiedResponse aUnifiedResponse,
                                            @NonNull final Throwable aThrowable)
  {
    // From specific to general
    if (aThrowable instanceof final HttpResponseException aEx)
    {
      _logRestException ("HttpResponse exception", aThrowable);
      _setSimpleTextResponse (aUnifiedResponse, aEx.getStatusCode (), aEx.getReasonPhrase ());
      return EHandled.HANDLED;
    }
    if (aThrowable instanceof APIParamException)
    {
      _logRestException ("Parameter exception", aThrowable);
      _setSimpleTextResponse (aUnifiedResponse,
                              HttpServletResponse.SC_BAD_REQUEST,
                              GlobalDebug.isDebugMode () ? getResponseEntityWithStackTrace (aThrowable)
                                                         : getResponseEntityWithoutStackTrace (aThrowable));
      return EHandled.HANDLED;
    }
    if (aThrowable instanceof RuntimeException)
    {
      _logRestException ("Runtime exception - " + aThrowable.getClass ().getName (), aThrowable);
      _setSimpleTextResponse (aUnifiedResponse,
                              HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                              GlobalDebug.isDebugMode () ? getResponseEntityWithStackTrace (aThrowable)
                                                         : getResponseEntityWithoutStackTrace (aThrowable));
      return EHandled.HANDLED;
    }

    // We don't know that exception
    return EHandled.UNHANDLED;
  }
}
