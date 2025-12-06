package com.helger.peppol.ui;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.OffsetDateTime;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.css.property.CCSSProperties;
import com.helger.css.propertyvalue.CCSSValue;
import com.helger.datetime.format.PDTToString;
import com.helger.datetime.helper.PDTFactory;
import com.helger.datetime.util.PDTDisplayHelper;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.IHCElement;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.tabular.HCCol;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.bootstrap4.badge.BootstrapBadge;
import com.helger.photon.bootstrap4.badge.EBootstrapBadgeType;
import com.helger.photon.bootstrap4.form.BootstrapFormHelper;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.security.certificate.CertificateHelper;

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

  @NonNull
  public static BootstrapTable createCertificateDetailsTable (@Nullable final String sAlias,
                                                              @NonNull final X509Certificate aX509Cert,
                                                              @NonNull final OffsetDateTime aNowLDT,
                                                              @NonNull final Locale aDisplayLocale)
  {
    final OffsetDateTime aNotBefore = PDTFactory.createOffsetDateTime (aX509Cert.getNotBefore ());
    final OffsetDateTime aNotAfter = PDTFactory.createOffsetDateTime (aX509Cert.getNotAfter ());
    final PublicKey aPublicKey = aX509Cert.getPublicKey ();

    final BootstrapTable aCertDetails = new BootstrapTable (new HCCol ().addStyle (CCSSProperties.WIDTH.newValue ("12rem")),
                                                            HCCol.star ());
    aCertDetails.setResponsive (true);
    if (StringHelper.isNotEmpty (sAlias))
      aCertDetails.addBodyRow ().addCell ("Alias:").addCell (sAlias);
    aCertDetails.addBodyRow ().addCell ("Version:").addCell (Integer.toString (aX509Cert.getVersion ()));
    aCertDetails.addBodyRow ().addCell ("Issuer:").addCell (CertificateUI.getCertIssuer (aX509Cert));
    aCertDetails.addBodyRow ().addCell ("Subject:").addCell (CertificateUI.getCertSubject (aX509Cert));
    aCertDetails.addBodyRow ().addCell ("Serial number:").addCell (CertificateUI.getCertSerialNumber (aX509Cert));
    aCertDetails.addBodyRow ()
                .addCell ("Not before:")
                .addCell (CertificateUI.getNodeCertNotBefore (aNotBefore, aNowLDT, aDisplayLocale));
    aCertDetails.addBodyRow ()
                .addCell ("Not after:")
                .addCell (CertificateUI.getNodeCertNotAfter (aNotAfter, aNowLDT, aDisplayLocale));

    if (aPublicKey instanceof final RSAPublicKey aRSAPubKey)
    {
      // Special handling for RSA
      aCertDetails.addBodyRow ()
                  .addCell ("Public key:")
                  .addCell (aX509Cert.getPublicKey ().getAlgorithm () +
                            " (" +
                            aRSAPubKey.getModulus ().bitLength () +
                            " bits)");
    }
    else
    {
      // Usually EC or DSA key
      aCertDetails.addBodyRow ().addCell ("Public key:").addCell (aX509Cert.getPublicKey ().getAlgorithm ());
    }
    aCertDetails.addBodyRow ()
                .addCell ("Signature algorithm:")
                .addCell (aX509Cert.getSigAlgName () + " (" + aX509Cert.getSigAlgOID () + ")");
    return aCertDetails;
  }

  @NonNull
  public static IHCElement <?> createCertificatePEMControl (@NonNull final X509Certificate aCert)
  {
    ValueEnforcer.notNull (aCert, "Certificate");
    final HCTextArea aTextArea = new HCTextArea ().setReadOnly (true)
                                                  .setRows (5)
                                                  .setValue (CertificateHelper.getPEMEncodedCertificate (aCert))
                                                  .addStyle (CCSSProperties.FONT_FAMILY.newValue (CCSSValue.FONT_MONOSPACE));
    BootstrapFormHelper.markAsFormControl (aTextArea);
    return new HCDiv ().addChild (aTextArea);
  }
}
