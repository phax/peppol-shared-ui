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
package com.helger.peppol.ui.feedbackcb;

import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.html.hc.IHCNodeWithChildren;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.peppol.ui.types.feedbackcb.IFeedbackCallback;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.photon.bootstrap4.alert.BootstrapWarnBox;
import com.helger.photon.bootstrap4.uictrls.ext.BootstrapTechnicalUI;

public class FeedbackCallbackAddToNode implements IFeedbackCallback
{
  private @NonNull final IHCNodeWithChildren <?> m_aDestNode;
  private @NonNull final Locale m_aDisplayLocale;

  public FeedbackCallbackAddToNode (@NonNull final IHCNodeWithChildren <?> aDestNode,
                                    @NonNull final Locale aDisplayLocale)
  {
    m_aDestNode = aDestNode;
    m_aDisplayLocale = aDisplayLocale;
  }

  public void info (@NonNull final String s)
  {
    m_aDestNode.addChild (new HCDiv ().addChild (s));
  }

  public void warn (@NonNull final String s, @Nullable final Exception ex)
  {
    final BootstrapWarnBox aWarnBox = new BootstrapWarnBox ().addChild (s);
    if (ex != null)
      aWarnBox.addChild (BootstrapTechnicalUI.getTechnicalDetailsNode (ex, m_aDisplayLocale));
    m_aDestNode.addChild (aWarnBox);
  }

  public void error (@NonNull final String s, @Nullable final Exception ex)
  {
    final BootstrapErrorBox aErrorBox = new BootstrapErrorBox ().addChild (s);
    if (ex != null)
      aErrorBox.addChild (BootstrapTechnicalUI.getTechnicalDetailsNode (ex, m_aDisplayLocale));
    m_aDestNode.addChild (aErrorBox);
  }
}
