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

import com.helger.base.string.StringHelper;
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
import com.helger.peppolid.peppol.PeppolIdentifierHelper;
import com.helger.photon.bootstrap4.badge.BootstrapBadge;
import com.helger.photon.bootstrap4.badge.EBootstrapBadgeType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class NiceNameUI
{

  @Nonnull
  private static IHCNode _createFormattedID (@Nonnull final String sID,
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
    ret.addChild (new BootstrapBadge (eType).addChild (sName));
    if (aWarningsNode != null)
    {
      ret.addChild (" ").addChild (aWarningsNode);
    }
    if (bInDetails)
    {
      ret.addChild (new HCSmall ().addChild (" (").addChild (new HCCode ().addChild (sID)).addChild (")"));
    }
    return ret;
  }

  @Nonnull
  private static IHCNode _createID (@Nonnull final String sID,
                                    @Nullable final NiceNameEntry aNiceName,
                                    final boolean bInDetails)
  {
    if (aNiceName == null)
      return _createFormattedID (sID, null, null, null, bInDetails);

    final HCNodeList aWarnings = new HCNodeList ();
    if (aNiceName.getState ().isRemoved ())
      aWarnings.addChild (new BootstrapBadge (EBootstrapBadgeType.DANGER).addChild ("Identifier is removed"));
    else
      if (aNiceName.getState ().isDeprecated ())
        aWarnings.addChild (new BootstrapBadge (EBootstrapBadgeType.WARNING).addChild ("Identifier is deprecated"));
    if (StringHelper.isNotEmpty (aNiceName.getWarning ()))
    {
      if (aWarnings.hasChildren ())
        aWarnings.addChild (" ");
      aWarnings.addChild (new BootstrapBadge (EBootstrapBadgeType.WARNING).addChild (aNiceName.getWarning ()));
    }

    return _createFormattedID (sID,
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
  private static NiceNameEntry _getPintEnabledNN (@Nonnull final IDocumentTypeIdentifier aDocTypeID)
  {
    final boolean bIsPint = _isPintDocType (aDocTypeID);
    final boolean bIsWildcard = _isWildcardDocType (aDocTypeID);

    NiceNameEntry aNN = NiceNameManager.docTypeNames ().get (aDocTypeID.getURIEncoded ());
    if (aNN == null && bIsPint && bIsWildcard)
    {
      // Try version without the star
      aNN = NiceNameManager.docTypeNames ().get (StringRemove.removeAll (aDocTypeID.getURIEncoded (), '*'));
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
    final NiceNameEntry aNN = _getPintEnabledNN (aDocTypeID);
    return _createID (aDocTypeID.getURIEncoded (), aNN, bInDetails);
  }

  @Nonnull
  public static IHCNode createProcessID (@Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                         @Nonnull final IProcessIdentifier aProcessID)
  {
    final String sURI = aProcessID.getURIEncoded ();
    final boolean bInDetails = true;

    // Check in relation ship to Document Type first
    NiceNameEntry aNN = _getPintEnabledNN (aDocTypeID);
    if (aNN != null)
    {
      if (aNN.containsProcessID (aProcessID))
        return _createFormattedID (sURI, "Matching Process Identifier", EBootstrapBadgeType.SUCCESS, null, bInDetails);
      return _createFormattedID (sURI, "Unexpected Process Identifier", EBootstrapBadgeType.DANGER, null, bInDetails);
    }

    // Check direct match first
    aNN = NiceNameManager.processNames ().get (sURI);
    if (aNN != null)
      return _createID (sURI, aNN, bInDetails);

    return _createFormattedID (sURI, null, null, null, bInDetails);
  }

}
