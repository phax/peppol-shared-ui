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

  public static final boolean DEFAULT_SMP_USE_SECURE_VALIDATION = true;

  private PeppolUITypes ()
  {}
}
