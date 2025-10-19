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
package com.helger.peppol.ui.nicename;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.string.StringRemove;
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
import com.helger.peppolid.peppol.PeppolIdentifierHelper;
import com.helger.photon.bootstrap4.badge.BootstrapBadge;
import com.helger.photon.bootstrap4.badge.EBootstrapBadgeType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@Immutable
public final class NiceNameUI
{
  private NiceNameUI ()
  {}

  @Nonnull
  public static IHCNode createFormattedID (@Nonnull final String sID,
                                           @Nullable final String sName,
                                           @Nullable final EBootstrapBadgeType eType,
                                           @Nullable final IHCNode aWarningsNode,
                                           final boolean bInDetails)
  {
    if (sName == null)
    {
      // No nice name present
      if (bInDetails)
        return new HCCode ().addChild (sID);
      return new HCTextNode (sID);
    }

    final HCNodeList ret = new HCNodeList ();
    final BootstrapBadge aNameBadge = ret.addAndReturnChild (new BootstrapBadge (eType).addChild (sName));
    if (aWarningsNode != null)
    {
      ret.addChild (" ").addChild (aWarningsNode);
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
    return ret;
  }

  @Nullable
  public static BootstrapBadge createStateBadge (@Nonnull final EPeppolCodeListItemState eState)
  {
    if (eState.isRemoved ())
      return new BootstrapBadge (EBootstrapBadgeType.DANGER).addChild ("Identifier is removed");
    if (eState.isDeprecated ())
      return new BootstrapBadge (EBootstrapBadgeType.WARNING).addChild ("Identifier is deprecated");
    return null;
  }

  @Nonnull
  private static IHCNode _createID (@Nonnull final String sID,
                                    @Nullable final NiceNameEntry aNiceName,
                                    final boolean bInDetails)
  {
    if (aNiceName == null)
      return createFormattedID (sID, null, null, null, bInDetails);

    final HCNodeList aWarnings = new HCNodeList ();
    aWarnings.addChild (createStateBadge (aNiceName.getState ()));
    if (aNiceName.hasSpecialLabel ())
    {
      if (aWarnings.hasChildren ())
        aWarnings.addChild (" ");
      aWarnings.addChild (new BootstrapBadge (EBootstrapBadgeType.WARNING).addChild (aNiceName.getSpecialLabel ()));
    }

    return createFormattedID (sID,
                              aNiceName.getName (),
                              EBootstrapBadgeType.SUCCESS,
                              aWarnings.hasChildren () ? aWarnings : null,
                              bInDetails);
  }

  private static boolean _isPintDocType (@Nonnull final IDocumentTypeIdentifier aDocTypeID)
  {
    return PeppolIdentifierHelper.DOCUMENT_TYPE_SCHEME_PEPPOL_DOCTYPE_WILDCARD.equals (aDocTypeID.getScheme ());
  }

  private static boolean _isWildcardDocType (@Nonnull final IDocumentTypeIdentifier aDocTypeID)
  {
    return aDocTypeID.getValue ().indexOf ('*') > 0;
  }

  @Nullable
  private static NiceNameEntry _getPintEnabledNiceNameEntry (@Nonnull final IDocumentTypeIdentifier aDocTypeID)
  {
    final boolean bIsPint = _isPintDocType (aDocTypeID);
    final boolean bIsWildcard = _isWildcardDocType (aDocTypeID);

    NiceNameEntry aNN = NiceNameManager.getDocTypeNiceName (aDocTypeID);
    if (aNN == null && bIsPint && bIsWildcard)
    {
      // Try version without the star
      aNN = NiceNameManager.getDocTypeNiceName (StringRemove.removeAll (aDocTypeID.getURIEncoded (), '*'));
    }
    if (aNN != null && bIsPint)
    {
      // Append something to the name
      // New logic since 15.5.2025 (PFUOI 4.3.0)
      aNN = aNN.withNewName (aNN.getName () + (bIsWildcard ? " (PINT wildcard match)" : " (PINT exact match)"));
    }

    return aNN;
  }

  @Nonnull
  public static IHCNode createDocTypeID (@Nonnull final IDocumentTypeIdentifier aDocTypeID, final boolean bInDetails)
  {
    final NiceNameEntry aNN = _getPintEnabledNiceNameEntry (aDocTypeID);
    return _createID (aDocTypeID.getURIEncoded (), aNN, bInDetails);
  }

  @Nonnull
  public static IHCNode createProcessID (@Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                         @Nonnull final IProcessIdentifier aProcessID,
                                         final boolean bInDetails)
  {
    final String sURI = aProcessID.getURIEncoded ();

    // Check in relation ship to Document Type first
    NiceNameEntry aNN = _getPintEnabledNiceNameEntry (aDocTypeID);
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
