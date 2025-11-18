package com.helger.peppol.ui.types.smp;

import java.util.List;

import org.jspecify.annotations.NonNull;

public interface ISMPExtensionsCallback
{
  void onPeppolSMPExtension (com.helger.xsds.peppol.smp1.@NonNull ExtensionType aExtension);

  void onBDXR1Extension (@NonNull List <com.helger.xsds.bdxr.smp1.@NonNull ExtensionType> aExtensionList);

  void onBDXR2Extension (@NonNull List <com.helger.xsds.bdxr.smp2.ec.@NonNull SMPExtensionType> aExtensionList);

  ISMPExtensionsCallback IGNORE = new ISMPExtensionsCallback ()
  {
    public void onPeppolSMPExtension (final com.helger.xsds.peppol.smp1.@NonNull ExtensionType aExtension)
    {
      // empty
    }

    public void onBDXR1Extension (@NonNull final List <com.helger.xsds.bdxr.smp1.@NonNull ExtensionType> aExtensionList)
    {
      // empty
    }

    public void onBDXR2Extension (@NonNull final List <com.helger.xsds.bdxr.smp2.ec.@NonNull SMPExtensionType> aExtensionList)
    {
      // empty
    }
  };
}
