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
package com.helger.peppol.ui;

import java.util.List;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.CodingStyleguideUnaware;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.lang.clazz.ClassHelper;
import com.helger.base.string.StringHelper;
import com.helger.base.type.ITypedObject;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.html.textlevel.HCCode;
import com.helger.peppolid.peppol.doctype.IPeppolDocumentTypeIdentifierParts;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.page.IWebPageExecutionContext;
import com.helger.url.ISimpleURL;

@Immutable
public final class PeppolUI
{

  // Security stuff
  public static final String ROLE_CONFIG_ID = "config";
  public static final String ROLE_CONFIG_NAME = "Config user";
  public static final String ROLE_CONFIG_DESCRIPTION = null;
  public static final ICommonsMap <String, String> ROLE_CONFIG_CUSTOMATTRS = null;
  @CodingStyleguideUnaware
  public static final List <String> REQUIRED_ROLE_IDS_CONFIG = new CommonsArrayList <> (ROLE_CONFIG_ID).getAsUnmodifiable ();

  private PeppolUI ()
  {}

  @NonNull
  private static String _getString (@NonNull final Throwable t)
  {
    return StringHelper.getConcatenatedOnDemand (ClassHelper.getClassLocalName (t.getClass ()), " - ", t.getMessage ());
  }

  @NonNull
  public static ISimpleURL getViewLink (@NonNull final IWebPageExecutionContext aWPEC,
                                        @NonNull @Nonempty final String sMenuItemID,
                                        @NonNull final ITypedObject <String> aObject)
  {
    ValueEnforcer.notNull (aObject, "Object");

    return getViewLink (aWPEC, sMenuItemID, aObject.getID ());
  }

  @NonNull
  public static ISimpleURL getViewLink (@NonNull final IWebPageExecutionContext aWPEC,
                                        @NonNull @Nonempty final String sMenuItemID,
                                        @NonNull final String sObjectID)
  {
    return aWPEC.getLinkToMenuItem (sMenuItemID)
                .add (CPageParam.PARAM_ACTION, CPageParam.ACTION_VIEW)
                .add (CPageParam.PARAM_OBJECT, sObjectID);
  }

  @NonNull
  public static HCUL getDocumentTypeIDDetails (@NonNull final IPeppolDocumentTypeIdentifierParts aParts)
  {
    final HCUL aUL = new HCUL ();
    aUL.addItem ().addChild ("Root namespace: ").addChild (new HCCode ().addChild (aParts.getRootNS ()));
    aUL.addItem ().addChild ("Local name: ").addChild (new HCCode ().addChild (aParts.getLocalName ()));
    aUL.addItem ().addChild ("Customization ID: ").addChild (new HCCode ().addChild (aParts.getCustomizationID ()));
    aUL.addItem ().addChild ("Version: ").addChild (new HCCode ().addChild (aParts.getVersion ()));
    return aUL;
  }
}
