package com.helger.peppol.sharedui.page.pub;

import org.apache.hc.client5.http.HttpResponseException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.CGlobal;
import com.helger.base.callback.exception.IExceptionCallback;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.ext.HCExtHelper;
import com.helger.html.hc.html.grouping.HCHR;
import com.helger.http.CHttp;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.photon.bootstrap4.traits.IHCBootstrap4Trait;
import com.helger.smpclient.exception.SMPClientHttpException;

final class SMPExceptionHandler implements IExceptionCallback <Exception>, IHCBootstrap4Trait
{
  @NonNull
  private final ESMPAPIType m_eAPIType;
  private IHCNode m_aHCNode;

  public SMPExceptionHandler (@NonNull final ESMPAPIType eAPIType)
  {
    ValueEnforcer.notNull (eAPIType, "APIType");
    m_eAPIType = eAPIType;
  }

  public void onException (@NonNull final Exception ex)
  {
    int nStatusCode = CGlobal.ILLEGAL_UINT;
    boolean bIsError = true;
    boolean bIsDirectoryQuery = false;
    if (ex instanceof final SMPClientHttpException schex)
    {
      // SMP client
      nStatusCode = schex.getResponseStatusCode ();
    }
    else
      if (ex instanceof final HttpResponseException hrex)
      {
        // Business Card query
        nStatusCode = hrex.getStatusCode ();
        bIsDirectoryQuery = true;

        if (nStatusCode == CHttp.HTTP_NOT_FOUND)
        {
          // This is expected and handled properly via the UI
          nStatusCode = CGlobal.ILLEGAL_UINT;
          bIsError = false;
        }
      }

    if (nStatusCode != CGlobal.ILLEGAL_UINT)
    {
      if (m_eAPIType == ESMPAPIType.PEPPOL && nStatusCode >= 300 && nStatusCode < 400)
      {
        m_aHCNode = error ().addChild ("The SMP server responsed with an HTTP Redirect Code (" +
                                       nStatusCode +
                                       ") which is not allowed according to Peppol SMP specification.");
      }
      else
      {
        m_aHCNode = error ().addChildren (div ("The SMP server responded with an HTTP Status Code " +
                                               nStatusCode +
                                               "."),
                                          new HCHR (),
                                          HCExtHelper.nl2divNodeList ("Technical details: " + ex.getMessage ()));
      }
    }
    else
    {
      if (bIsError)
      {
        final String sTechnicalDetails = ex.getMessage ();
        final BootstrapErrorBox aErrBox = error ().addChildren (div ("Technical error occurred while querying the SMP."),
                                                                new HCHR (),
                                                                HCExtHelper.nl2divNodeList ("Technical details: " +
                                                                                            sTechnicalDetails));
        if (bIsDirectoryQuery &&
            sTechnicalDetails.contains ("unable to find valid certification path to requested target"))
        {
          aErrBox.addChild (div ("The error most likely stems from an outdated trusted certificate list on my end - don't worry too much."));
        }
        m_aHCNode = aErrBox;
      }
    }
  }

  public boolean hasResultNode ()
  {
    return m_aHCNode != null;
  }

  @Nullable
  public IHCNode getResultNode ()
  {
    return m_aHCNode;
  }
}
