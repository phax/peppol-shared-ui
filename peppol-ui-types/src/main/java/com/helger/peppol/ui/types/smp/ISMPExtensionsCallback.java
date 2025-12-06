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
