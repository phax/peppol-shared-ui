package com.helger.peppol.ui.types.smp;

import org.jspecify.annotations.NonNull;

import com.helger.smpclient.bdxr1.BDXRClientReadOnly;
import com.helger.smpclient.bdxr2.BDXR2ClientReadOnly;
import com.helger.smpclient.peppol.SMPClientReadOnly;

public interface ISMPClientCreationCallback
{
  void onPeppolSMPClient (@NonNull SMPClientReadOnly aSMPClient);

  void onBDXR1Client (@NonNull BDXRClientReadOnly aSMPClient);

  void onBDXR2Client (@NonNull BDXR2ClientReadOnly aSMPClient);

  ISMPClientCreationCallback IGNORE = new ISMPClientCreationCallback ()
  {
    public void onPeppolSMPClient (@NonNull SMPClientReadOnly aSMPClient)
    {
      // empty
    }

    public void onBDXR1Client (@NonNull BDXRClientReadOnly aSMPClient)
    {
      // empty
    }

    public void onBDXR2Client (@NonNull BDXR2ClientReadOnly aSMPClient)
    {
      // empty
    }
  };
}
