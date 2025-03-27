package com.helger.peppol.sharedui;

import java.util.List;
import java.util.Locale;

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.CodingStyleguideUnaware;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.locale.LocaleCache;

@Immutable
public final class CSharedUI
{
  public static final Locale LOCALE_DE = LocaleCache.getInstance ().getLocale ("de", "DE");
  public static final Locale LOCALE_EN = LocaleCache.getInstance ().getLocale ("en", "US");

  public static final String PATTERN_SMP_ID = "[a-zA-Z0-9\\-\\.]+";

  // Security stuff
  public static final String ROLE_CONFIG_ID = "config";
  public static final String ROLE_CONFIG_NAME = "Config user";
  public static final String ROLE_CONFIG_DESCRIPTION = null;
  public static final ICommonsMap <String, String> ROLE_CONFIG_CUSTOMATTRS = null;
  @CodingStyleguideUnaware
  public static final List <String> REQUIRED_ROLE_IDS_CONFIG = new CommonsArrayList <> (ROLE_CONFIG_ID).getAsUnmodifiable ();

  private CSharedUI ()
  {}
}
