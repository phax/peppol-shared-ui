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
package com.helger.peppol.sharedui.config;

import com.helger.annotation.style.UsedViaReflection;
import com.helger.base.debug.GlobalDebug;
import com.helger.config.IConfig;
import com.helger.peppol.ui.types.config.PeppolSharedConfig;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * This class provides access to the settings as contained in the
 * <code>application.properties</code> file.
 *
 * @author Philip Helger
 */
public final class SharedUIConfig extends AbstractGlobalSingleton
{
  @Deprecated
  @UsedViaReflection
  private SharedUIConfig ()
  {}

  @Nonnull
  private static IConfig _getConfig ()
  {
    return PeppolSharedConfig.getConfig ();
  }

  @Nullable
  public static String getGlobalDebug ()
  {
    return _getConfig ().getAsString ("global.debug");
  }

  @Nullable
  public static String getGlobalProduction ()
  {
    return _getConfig ().getAsString ("global.production");
  }

  @Nullable
  public static String getDataPath ()
  {
    return _getConfig ().getAsString ("webapp.datapath");
  }

  public static boolean isCheckFileAccess ()
  {
    return _getConfig ().getAsBoolean ("webapp.checkfileaccess", true);
  }

  public static boolean isTestVersion ()
  {
    return _getConfig ().getAsBoolean ("webapp.testversion", GlobalDebug.isDebugMode ());
  }

  @Nullable
  public static String getRecaptchaWebKey ()
  {
    return _getConfig ().getAsString ("recaptcha.webkey");
  }

  @Nullable
  public static String getRecaptchaSecretKey ()
  {
    return _getConfig ().getAsString ("recaptcha.secretkey");
  }

  public static long getValidationAPIMaxRequestsPerSecond ()
  {
    return _getConfig ().getAsLong ("validation.limit.requestspersecond", -1);
  }
}
