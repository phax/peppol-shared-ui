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
package com.helger.peppol.ui.types.config;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.style.UsedViaReflection;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.config.ConfigFactory;
import com.helger.config.IConfig;
import com.helger.scope.singleton.AbstractGlobalSingleton;

/**
 * This class provides access to the settings as contained in the
 * <code>application.properties</code> file.
 *
 * @author Philip Helger
 */
public final class PeppolSharedConfig extends AbstractGlobalSingleton
{
  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();

  /** The name of the file containing the settings */
  @GuardedBy ("RW_LOCK")
  private static IConfig s_aConfig = ConfigFactory.getDefaultConfig ();

  @Deprecated
  @UsedViaReflection
  private PeppolSharedConfig ()
  {}

  @NonNull
  public static IConfig getConfig ()
  {
    RW_LOCK.readLock ().lock ();
    try
    {
      return s_aConfig;
    }
    finally
    {
      RW_LOCK.readLock ().unlock ();
    }
  }

  public static void setConfig (@NonNull final IConfig aConfig)
  {
    ValueEnforcer.notNull (aConfig, "Config");
    RW_LOCK.writeLocked ( () -> s_aConfig = aConfig);
  }
}
