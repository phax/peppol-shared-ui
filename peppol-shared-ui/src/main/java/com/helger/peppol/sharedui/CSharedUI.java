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
package com.helger.peppol.sharedui;

import java.util.List;
import java.util.Locale;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.CodingStyleguideUnaware;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.text.locale.LocaleCache;

@Immutable
public final class CSharedUI
{
  public static final Locale LOCALE_DE = LocaleCache.getInstance ().getLocale ("de", "DE");
  public static final Locale LOCALE_EN = LocaleCache.getInstance ().getLocale ("en", "US");

  public static final String PATTERN_SMP_ID = "[a-zA-Z0-9\\-\\.]+";

  // Security stuff
  public static final String ROLE_CONFIG_ID = "config";
  public static final String ROLE_CONFIG_NAME = "Config user";
  public static final String ROLE_CONFIG_DESCRIPTION = null;
  public static final ICommonsMap <String, String> ROLE_CONFIG_CUSTOMATTRS = null;
  @CodingStyleguideUnaware
  public static final List <String> REQUIRED_ROLE_IDS_CONFIG = new CommonsArrayList <> (ROLE_CONFIG_ID).getAsUnmodifiable ();

  public static final boolean DEFAULT_SMP_USE_SECURE_VALIDATION = true;

  private CSharedUI ()
  {}
}
