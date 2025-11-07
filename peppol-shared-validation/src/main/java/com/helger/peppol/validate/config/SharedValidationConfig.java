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
package com.helger.peppol.validate.config;

import com.helger.annotation.style.UsedViaReflection;
import com.helger.config.IConfig;
import com.helger.peppol.ui.types.config.PeppolSharedConfig;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import jakarta.annotation.Nonnull;

/**
 * This class provides access to the settings as contained in the
 * <code>application.properties</code> file.
 *
 * @author Philip Helger
 */
public final class SharedValidationConfig extends AbstractGlobalSingleton
{
  @Deprecated
  @UsedViaReflection
  private SharedValidationConfig ()
  {}

  @Nonnull
  private static IConfig _getConfig ()
  {
    return PeppolSharedConfig.getConfig ();
  }

  public static long getValidationAPILimitDurationSeconds ()
  {
    return _getConfig ().getAsLong ("validation.limit.seconds", -1);
  }

  public static long getValidationAPILimitRequestsInDuration ()
  {
    return _getConfig ().getAsLong ("validation.limit.requests", -1);
  }
}
