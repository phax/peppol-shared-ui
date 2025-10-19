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
package com.helger.peppol.photon.mgr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.style.UsedViaReflection;
import com.helger.base.exception.InitializationException;
import com.helger.dao.DAOException;
import com.helger.peppol.photon.smlconfig.ISMLConfigurationManager;
import com.helger.peppol.photon.smlconfig.SMLConfigurationManager;
import com.helger.scope.IScope;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import jakarta.annotation.Nonnull;

/**
 * Central manager for all sub managers
 *
 * @author Philip Helger
 */
public final class PhotonPeppolMetaManager extends AbstractGlobalSingleton
{
  // Legacy filename
  private static final String SML_CONFIGURATION_XML = "sml-info.xml";

  private static final Logger LOGGER = LoggerFactory.getLogger (PhotonPeppolMetaManager.class);

  private SMLConfigurationManager m_aSMLConfigurationMgr;

  @Deprecated
  @UsedViaReflection
  public PhotonPeppolMetaManager ()
  {}

  @Override
  protected void onAfterInstantiation (@Nonnull final IScope aScope)
  {
    try
    {
      // Before TestEndpoint manager!
      m_aSMLConfigurationMgr = new SMLConfigurationManager (SML_CONFIGURATION_XML);

      LOGGER.info (getClass ().getName () + " was initialized");
    }
    catch (final DAOException ex)
    {
      throw new InitializationException ("Failed to init " + getClass ().getName (), ex);
    }
  }

  @Nonnull
  public static PhotonPeppolMetaManager getInstance ()
  {
    return getGlobalSingleton (PhotonPeppolMetaManager.class);
  }

  @Nonnull
  public static ISMLConfigurationManager getSMLConfigurationMgr ()
  {
    return getInstance ().m_aSMLConfigurationMgr;
  }
}
