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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.StringHelper;
import com.helger.peppol.sharedui.config.SharedUIConfig;
import com.helger.peppol.smp.ISMPTransportProfile;
import com.helger.peppolid.peppol.pidscheme.EPredefinedParticipantIdentifierScheme;

/**
 * Misc utility methods
 *
 * @author Philip Helger
 */
@Immutable
public final class SharedUIHelper
{
  private static String s_sAppNamePrefix = "Peppol-Shared-UI";

  private SharedUIHelper ()
  {}

  public static void setApplicationName (@Nonnull @Nonempty final String sAppName)
  {
    ValueEnforcer.notEmpty (sAppName, "AppName");
    s_sAppNamePrefix = sAppName;
  }

  @Nonnull
  @Nonempty
  public static String getApplicationTitle ()
  {
    return s_sAppNamePrefix + (SharedUIConfig.isTestVersion () ? " [TEST]" : "");
  }

  @Nullable
  public static EPredefinedParticipantIdentifierScheme getParticipantIdentifierSchemeOfID (@Nullable final String sSchemeID)
  {
    if (StringHelper.hasText (sSchemeID))
      for (final EPredefinedParticipantIdentifierScheme eAgency : EPredefinedParticipantIdentifierScheme.values ())
        if (eAgency.getISO6523Code ().equals (sSchemeID) || eAgency.getSchemeID ().equals (sSchemeID))
          return eAgency;
    return null;
  }

  @Nullable
  public static String getSMPTransportProfileShortName (@Nullable final ISMPTransportProfile aTransportProfile)
  {
    return aTransportProfile == null ? null : aTransportProfile.getName ();
  }
}
