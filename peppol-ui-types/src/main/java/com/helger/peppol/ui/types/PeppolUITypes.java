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
package com.helger.peppol.ui.types;

import java.util.Locale;

import com.helger.annotation.concurrent.Immutable;
import com.helger.text.locale.LocaleCache;

@Immutable
public final class PeppolUITypes
{
  public static final Locale LOCALE_DE = LocaleCache.getInstance ().getLocale ("de", "DE");
  public static final Locale LOCALE_EN = LocaleCache.getInstance ().getLocale ("en", "US");

  public static final String PATTERN_SMP_ID = "[a-zA-Z0-9\\-\\.]+";

  // Defaulting to true per 11.8.2025
  public static final boolean DEFAULT_SMP_USE_SECURE_VALIDATION = true;
  public static final boolean DEFAULT_QUERY_BUSINESS_CARD = true;
  public static final boolean DEFAULT_SHOW_TIME = false;
  public static final boolean DEFAULT_XSD_VALIDATION = true;
  public static final boolean DEFAULT_VERIFY_SIGNATURES = true;

  private PeppolUITypes ()
  {}
}
