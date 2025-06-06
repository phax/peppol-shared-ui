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
package com.helger.peppol.sharedui.ui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.type.ITypedObject;
import com.helger.commons.url.ISimpleURL;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.textlevel.HCCode;
import com.helger.html.hc.html.textlevel.HCSmall;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.hc.impl.HCTextNode;
import com.helger.html.jquery.JQueryAjaxBuilder;
import com.helger.html.jscode.JSAssocArray;
import com.helger.peppol.sharedui.api.CSharedUIAjax;
import com.helger.peppol.sharedui.domain.NiceNameEntry;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.PeppolIdentifierFactory;
import com.helger.peppolid.peppol.EPeppolCodeListItemState;
import com.helger.peppolid.peppol.PeppolIdentifierHelper;
import com.helger.photon.bootstrap4.badge.BootstrapBadge;
import com.helger.photon.bootstrap4.badge.EBootstrapBadgeType;
import com.helger.photon.bootstrap4.ext.BootstrapSystemMessage;
import com.helger.photon.bootstrap4.uictrls.datatables.BootstrapDataTables;
import com.helger.photon.core.requestparam.RequestParameterHandlerURLPathNamed;
import com.helger.photon.core.requestparam.RequestParameterManager;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.page.IWebPageExecutionContext;
import com.helger.photon.uictrls.datatables.DataTablesLengthMenu;
import com.helger.photon.uictrls.datatables.EDataTablesFilterType;
import com.helger.photon.uictrls.datatables.ajax.AjaxExecutorDataTables;
import com.helger.photon.uictrls.datatables.ajax.AjaxExecutorDataTablesI18N;
import com.helger.photon.uictrls.datatables.plugins.DataTablesPluginSearchHighlight;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

public final class SharedCommonUI
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SharedCommonUI.class);
  private static final ICommonsMap <String, NiceNameEntry> DOCTYPE_NAMES = new CommonsHashMap <> ();
  private static final ICommonsMap <String, NiceNameEntry> PROCESS_NAMES = new CommonsHashMap <> ();
  private static final DataTablesLengthMenu LENGTH_MENU = new DataTablesLengthMenu ().addItem (25)
                                                                                     .addItem (50)
                                                                                     .addItem (100)
                                                                                     .addItemAll ();

  @Nonnull
  private static String _ensurePrefix (@Nonnull final String sPrefix, @Nonnull final String s)
  {
    final String sReal = StringHelper.trimStart (s, "PEPPOL").trim ();

    if (sReal.startsWith (sPrefix))
      return sReal;
    return sPrefix + sReal;
  }

  static
  {
    for (final com.helger.peppolid.peppol.doctype.EPredefinedDocumentTypeIdentifier e : com.helger.peppolid.peppol.doctype.EPredefinedDocumentTypeIdentifier.values ())
      DOCTYPE_NAMES.put (e.getURIEncoded (),
                         new NiceNameEntry (_ensurePrefix ("Peppol ", e.getCommonName ()),
                                            e.getState (),
                                            e.getAllProcessIDs ()));
    for (final com.helger.peppolid.peppol.process.EPredefinedProcessIdentifier e : com.helger.peppolid.peppol.process.EPredefinedProcessIdentifier.values ())
      PROCESS_NAMES.put (e.getURIEncoded (), new NiceNameEntry ("Peppol predefined", e.getState (), null));

    // Custom document types
    final PeppolIdentifierFactory PIF = PeppolIdentifierFactory.INSTANCE;
    DOCTYPE_NAMES.put ("busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:e-fff:ver3.0::2.1",
                       new NiceNameEntry ("e-FFF 3.0 Invoice",
                                          EPeppolCodeListItemState.DEPRECATED,
                                          new CommonsArrayList <> (PIF.createProcessIdentifierWithDefaultScheme ("urn:www.cenbii.eu:profile:bii05:ver1.0"))));
    DOCTYPE_NAMES.put ("busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:e-fff:ver3.0::2.1",
                       new NiceNameEntry ("e-FFF 3.0 CreditNote",
                                          EPeppolCodeListItemState.DEPRECATED,
                                          new CommonsArrayList <> (PIF.createProcessIdentifierWithDefaultScheme ("urn:www.cenbii.eu:profile:bii05:ver1.0"))));
  }

  private SharedCommonUI ()
  {}

  public static void init ()
  {
    RequestParameterManager.getInstance ().setParameterHandler (new RequestParameterHandlerURLPathNamed ());

    BootstrapDataTables.setConfigurator ( (aLEC, aTable, aDataTables) -> {
      final IRequestWebScopeWithoutResponse aRequestScope = aLEC.getRequestScope ();
      aDataTables.setAutoWidth (false)
                 .setLengthMenu (LENGTH_MENU)
                 .setAjaxBuilder (new JQueryAjaxBuilder ().url (CSharedUIAjax.DATATABLES.getInvocationURL (aRequestScope))
                                                          .data (new JSAssocArray ().add (AjaxExecutorDataTables.OBJECT_ID,
                                                                                          aTable.getID ())))
                 .setServerFilterType (EDataTablesFilterType.ALL_TERMS_PER_ROW)
                 .setTextLoadingURL (CSharedUIAjax.DATATABLES_I18N.getInvocationURL (aRequestScope),
                                     AjaxExecutorDataTablesI18N.REQUEST_PARAM_LANGUAGE_ID)
                 .addPlugin (new DataTablesPluginSearchHighlight ());
    });
    // By default allow markdown in system message
    BootstrapSystemMessage.setDefaultUseMarkdown (true);
  }

  @Nonnull
  public static ISimpleURL getViewLink (@Nonnull final IWebPageExecutionContext aWPEC,
                                        @Nonnull @Nonempty final String sMenuItemID,
                                        @Nonnull final ITypedObject <String> aObject)
  {
    ValueEnforcer.notNull (aObject, "Object");

    return getViewLink (aWPEC, sMenuItemID, aObject.getID ());
  }

  @Nonnull
  public static ISimpleURL getViewLink (@Nonnull final IWebPageExecutionContext aWPEC,
                                        @Nonnull @Nonempty final String sMenuItemID,
                                        @Nonnull final String sObjectID)
  {
    return aWPEC.getLinkToMenuItem (sMenuItemID)
                .add (CPageParam.PARAM_ACTION, CPageParam.ACTION_VIEW)
                .add (CPageParam.PARAM_OBJECT, sObjectID);
  }

  @Nonnull
  private static String _getString (@Nonnull final Throwable t)
  {
    return StringHelper.getConcatenatedOnDemand (ClassHelper.getClassLocalName (t.getClass ()), " - ", t.getMessage ());
  }

  @Nullable
  public static HCNodeList getTechnicalDetailsUI (@Nullable final Throwable t, final boolean bLogException)
  {
    if (t == null)
      return null;

    if (bLogException)
      LOGGER.warn ("Technical details", t);

    final HCNodeList ret = new HCNodeList ();
    Throwable aCur = t;
    while (aCur != null)
    {
      if (ret.hasNoChildren ())
        ret.addChild (new HCDiv ().addChild ("Technical details: " + _getString (aCur)));
      else
        ret.addChild (new HCDiv ().addChild ("Caused by: " + _getString (aCur)));
      aCur = aCur.getCause ();
    }
    return ret;
  }

  @Nullable
  public static String getTechnicalDetailsString (@Nullable final Throwable t, final boolean bLogException)
  {
    if (t == null)
      return null;

    if (bLogException)
      LOGGER.warn ("Technical details", t);

    final StringBuilder ret = new StringBuilder ();
    Throwable aCur = t;
    while (aCur != null)
    {
      if (ret.length () == 0)
        ret.append ("Technical details: ").append (_getString (aCur));
      else
        ret.append ("\nCaused by: ").append (_getString (aCur));
      aCur = aCur.getCause ();
    }
    return ret.toString ();
  }

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
    if (StringHelper.hasText (aNiceName.getWarning ()))
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

    NiceNameEntry aNN = DOCTYPE_NAMES.get (aDocTypeID.getURIEncoded ());
    if (aNN == null && bIsPint && bIsWildcard)
    {
      // Try version without the star
      aNN = DOCTYPE_NAMES.get (StringHelper.removeAll (aDocTypeID.getURIEncoded (), '*'));
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
    aNN = PROCESS_NAMES.get (sURI);
    if (aNN != null)
      return _createID (sURI, aNN, bInDetails);

    return _createFormattedID (sURI, null, null, null, bInDetails);
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsMap <String, NiceNameEntry> getDocTypeNames ()
  {
    return DOCTYPE_NAMES.getClone ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsMap <String, NiceNameEntry> getProcessNames ()
  {
    return PROCESS_NAMES.getClone ();
  }
}
