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
package com.helger.peppol.sharedui.mgr;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.exception.InitializationException;
import com.helger.dao.DAOException;
import com.helger.scope.IScope;
import com.helger.scope.singleton.AbstractGlobalSingleton;

/**
 * Central manager for all sub managers
 *
 * @author Philip Helger
 */
public final class SharedUIMetaManager extends AbstractGlobalSingleton
{
  private static final String SML_INFO_XML = "sml-info.xml";

  private static final Logger LOGGER = LoggerFactory.getLogger (SharedUIMetaManager.class);

  private SMLConfigurationManager m_aSMLConfigurationMgr;

  @Deprecated
  @UsedViaReflection
  public SharedUIMetaManager ()
  {}

  @Override
  protected void onAfterInstantiation (@Nonnull final IScope aScope)
  {
    try
    {
      // Before TestEndpoint manager!
      m_aSMLConfigurationMgr = new SMLConfigurationManager (SML_INFO_XML);

      LOGGER.info (getClass ().getName () + " was initialized");
    }
    catch (final DAOException ex)
    {
      throw new InitializationException ("Failed to init " + getClass ().getName (), ex);
    }
  }

  @Nonnull
  public static SharedUIMetaManager getInstance ()
  {
    return getGlobalSingleton (SharedUIMetaManager.class);
  }

  @Nonnull
  public static ISMLConfigurationManager getSMLConfigurationMgr ()
  {
    return getInstance ().m_aSMLConfigurationMgr;
  }
}
