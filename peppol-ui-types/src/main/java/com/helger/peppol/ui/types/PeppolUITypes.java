package com.helger.peppol.ui.types;

import java.util.Locale;

import com.helger.annotation.concurrent.Immutable;
import com.helger.text.locale.LocaleCache;

@Immutable
public final class PeppolUITypes
{
  public static final Locale LOCALE_DE = LocaleCache.getInstance ().getLocale ("de", "DE");
  public static final Locale LOCALE_EN = LocaleCache.getInstance ().getLocale ("en", "US");

  public static final String PATTERN_SMP_ID = "[a-zA-Z0-9\\-\\.]+";

  // Defaulting to true per 11.8.2025
  public static final boolean DEFAULT_SMP_USE_SECURE_VALIDATION = true;
  public static final boolean DEFAULT_QUERY_BUSINESS_CARD = true;
  public static final boolean DEFAULT_SHOW_TIME = false;
  public static final boolean DEFAULT_XSD_VALIDATION = true;
  public static final boolean DEFAULT_VERIFY_SIGNATURES = true;
  public static final boolean DEFAULT_CNAME_LOOKUP = false;

  private PeppolUITypes ()
  {}
}
