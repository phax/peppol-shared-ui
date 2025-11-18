package com.helger.peppol.ui;

import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.Locale;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.concurrent.Immutable;
import com.helger.datetime.format.PDTToString;
import com.helger.datetime.util.PDTDisplayHelper;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.bootstrap4.badge.BootstrapBadge;
import com.helger.photon.bootstrap4.badge.EBootstrapBadgeType;

@Immutable
public final class CertificateUI
{
  private CertificateUI ()
  {}

  @NonNull
  public static String inGroupsOf (@NonNull final String s, final int nChars)
  {
    if (nChars < 1)
      return s;

    final int nMax = s.length ();
    // Worst case: 1 char 1 separator
    final StringBuilder aSB = new StringBuilder (nMax * 2);
    int nIndex = 0;
    while (nIndex < nMax - 1)
    {
      if (aSB.length () > 0)
        aSB.append (' ');
      aSB.append (s, nIndex, Integer.min (nIndex + nChars, nMax));
      nIndex += nChars;
    }
    return aSB.toString ();
  }

  @NonNull
  public static String getCertIssuer (@NonNull final X509Certificate aX509Cert)
  {
    return aX509Cert.getIssuerX500Principal ().getName ();
  }

  @NonNull
  public static String getCertSubject (@NonNull final X509Certificate aX509Cert)
  {
    return aX509Cert.getSubjectX500Principal ().getName ();
  }

  @NonNull
  public static String getCertSerialNumber (@NonNull final X509Certificate aX509Cert)
  {
    return aX509Cert.getSerialNumber ().toString () +
           " / 0x" +
           inGroupsOf (aX509Cert.getSerialNumber ().toString (16), 4);
  }

  @NonNull
  public static IHCNode getNodeCertNotBefore (@NonNull final OffsetDateTime aNotBefore,
                                              @NonNull final OffsetDateTime aNowDT,
                                              @NonNull final Locale aDisplayLocale)
  {
    final HCNodeList ret = new HCNodeList ();
    ret.addChild (PDTToString.getAsString (aNotBefore, aDisplayLocale));
    if (aNowDT.isBefore (aNotBefore))
      ret.addChild (new HCDiv ().addChild (new BootstrapBadge (EBootstrapBadgeType.DANGER).addChild ("!!!NOT YET VALID!!!")));
    return ret;
  }

  @NonNull
  public static IHCNode getNodeCertNotAfter (@NonNull final OffsetDateTime aNotAfter,
                                             @NonNull final OffsetDateTime aNowDT,
                                             @NonNull final Locale aDisplayLocale)
  {
    final HCNodeList ret = new HCNodeList ();
    ret.addChild (PDTToString.getAsString (aNotAfter, aDisplayLocale));
    if (aNowDT.isAfter (aNotAfter))
      ret.addChild (" ").addChild (new BootstrapBadge (EBootstrapBadgeType.DANGER).addChild ("!!!NO LONGER VALID!!!"));
    else
      ret.addChild (" ")
         .addChild (new BootstrapBadge (EBootstrapBadgeType.SUCCESS).addChild ("Valid for: " +
                                                                               PDTDisplayHelper.getPeriodTextEN (aNowDT.toLocalDateTime (),
                                                                                                                 aNotAfter.toLocalDateTime ())));
    return ret;
  }
}
