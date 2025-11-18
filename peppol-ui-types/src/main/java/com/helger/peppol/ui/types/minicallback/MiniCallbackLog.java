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
package com.helger.peppol.ui.types.minicallback;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MiniCallbackLog implements IMiniCallback
{
  private @NonNull final Logger m_aLogger;
  private @NonNull final String m_sLogPrefix;

  public MiniCallbackLog (@NonNull final Logger aLogger, @NonNull final String sLogPrefix)
  {
    m_aLogger = aLogger;
    m_sLogPrefix = sLogPrefix;
  }

  public void info (@NonNull final String s)
  {
    m_aLogger.info (m_sLogPrefix + s);
  }

  public void warn (@NonNull final String s, @Nullable final Exception ex)
  {
    m_aLogger.warn (m_sLogPrefix + s, ex);
  }

  public void error (@NonNull final String s, @Nullable final Exception ex)
  {
    m_aLogger.error (m_sLogPrefix + s, ex);
  }
}
