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
package com.helger.peppol.ui.nicename;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.textlevel.HCCode;
import com.helger.html.hc.html.textlevel.HCSmall;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.hc.impl.HCTextNode;
import com.helger.peppol.ui.types.nicename.NiceNameEntry;
import com.helger.peppol.ui.types.nicename.NiceNameManager;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.peppol.EPeppolCodeListItemState;
import com.helger.photon.bootstrap4.badge.BootstrapBadge;
import com.helger.photon.bootstrap4.badge.EBootstrapBadgeType;

/**
 * Helper class for display Participant IDs, Document Type IDs and Process IDs in combination with
 * {@link NiceNameEntry} objects.
 *
 * @author Philip Helger
 */
@Immutable
public final class NiceNameUI
{
  private NiceNameUI ()
  {}

  @NonNull
  public static HCNodeList createFormattedID (@NonNull final String sID,
                                              @Nullable final String sDisplayName,
                                              @Nullable final EBootstrapBadgeType eType,
                                              @Nullable final IHCNode aSpecialNode,
                                              final boolean bInDetails)
  {
    final HCNodeList ret = new HCNodeList ();
    if (sDisplayName == null)
    {
      // No nice name present
      if (bInDetails)
        ret.addChild (new HCCode ().addChild (sID));
      else
        ret.addChild (new HCTextNode (sID));
    }
    else
    {
      final BootstrapBadge aNameBadge = ret.addAndReturnChild (new BootstrapBadge (eType).addChild (sDisplayName));
      if (aSpecialNode != null)
      {
        ret.addChild (" ").addChild (aSpecialNode);
      }

      if (bInDetails)
      {
        ret.addChild (new HCSmall ().addChild (" (").addChild (new HCCode ().addChild (sID)).addChild (")"));
      }
      else
      {
        // Add ID as mouse over
        aNameBadge.setTitle (sID);
      }
    }
    return ret;
  }

  @Nullable
  public static BootstrapBadge createStateBadge (@NonNull final EPeppolCodeListItemState eState)
  {
    if (eState.isRemoved ())
      return new BootstrapBadge (EBootstrapBadgeType.DANGER).addChild ("Identifier is removed");
    if (eState.isDeprecated ())
      return new BootstrapBadge (EBootstrapBadgeType.WARNING).addChild ("Identifier is deprecated");
    return null;
  }

  @NonNull
  private static HCNodeList _createID (@NonNull final String sID,
                                       @Nullable final NiceNameEntry aNiceName,
                                       final boolean bInDetails)
  {
    if (aNiceName == null)
      return createFormattedID (sID, null, null, null, bInDetails);

    final HCNodeList aSpecialNode = new HCNodeList ();
    // Add deprecated/removed stuff
    aSpecialNode.addChild (createStateBadge (aNiceName.getState ()));
    if (aNiceName.hasSpecialLabel ())
    {
      if (aSpecialNode.hasChildren ())
        aSpecialNode.addChild (" ");
      aSpecialNode.addChild (new BootstrapBadge (EBootstrapBadgeType.WARNING).addChild (aNiceName.getSpecialLabel ()));
    }

    return createFormattedID (sID,
                              aNiceName.getName (),
                              EBootstrapBadgeType.SUCCESS,
                              aSpecialNode.hasChildren () ? aSpecialNode : null,
                              bInDetails);
  }

  @NonNull
  public static HCNodeList createDocTypeID (@NonNull final IDocumentTypeIdentifier aDocTypeID, final boolean bInDetails)
  {
    final NiceNameEntry aNN = NiceNameManager.getPintEnabledNiceNameEntry (aDocTypeID);
    return _createID (aDocTypeID.getURIEncoded (), aNN, bInDetails);
  }

  @NonNull
  public static HCNodeList createProcessID (@NonNull final IDocumentTypeIdentifier aDocTypeID,
                                            @NonNull final IProcessIdentifier aProcessID,
                                            final boolean bInDetails)
  {
    final String sURI = aProcessID.getURIEncoded ();

    // Check in relation ship to Document Type first
    NiceNameEntry aNN = NiceNameManager.getPintEnabledNiceNameEntry (aDocTypeID);
    if (aNN != null)
    {
      if (aNN.containsProcessID (aProcessID))
        return createFormattedID (sURI, "Matching Process Identifier", EBootstrapBadgeType.SUCCESS, null, bInDetails);
      return createFormattedID (sURI, "Unexpected Process Identifier", EBootstrapBadgeType.DANGER, null, bInDetails);
    }

    // Check direct match first
    aNN = NiceNameManager.getProcessNiceName (sURI);
    if (aNN != null)
      return _createID (sURI, aNN, bInDetails);

    return createFormattedID (sURI, null, null, null, bInDetails);
  }
}
