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
package com.helger.peppol.ui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.CodingStyleguideUnaware;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.lang.clazz.ClassHelper;
import com.helger.base.string.StringHelper;
import com.helger.base.type.ITypedObject;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.page.IWebPageExecutionContext;
import com.helger.url.ISimpleURL;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

  private static final Logger LOGGER = LoggerFactory.getLogger (PeppolUI.class);

  private PeppolUI ()
  {}

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
}
