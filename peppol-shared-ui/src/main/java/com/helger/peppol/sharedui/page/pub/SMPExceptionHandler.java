package com.helger.peppol.sharedui.page.pub;

import org.apache.hc.client5.http.HttpResponseException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.CGlobal;
import com.helger.base.callback.exception.IExceptionCallback;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.html.hc.IHCNode;
import com.helger.http.CHttp;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.smpclient.exception.SMPClientHttpException;

final class SMPExceptionHandler implements IExceptionCallback <Exception>
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
        if (nStatusCode == CHttp.HTTP_NOT_FOUND)
        {
          // This is expected and handled via the UI
          nStatusCode = CGlobal.ILLEGAL_UINT;
          bIsError = false;
        }
      }

    if (nStatusCode != CGlobal.ILLEGAL_UINT)
    {
      if (m_eAPIType == ESMPAPIType.PEPPOL && nStatusCode >= 300 && nStatusCode < 400)
        m_aHCNode = new BootstrapErrorBox ().addChild ("The SMP server responsed with an HTTP Redirect Code (" +
                                                       nStatusCode +
                                                       ") which is not allowed according to Peppol SMP specification.");
      else
        m_aHCNode = new BootstrapErrorBox ().addChild ("The SMP server responded with an HTTP Status Code " +
                                                       nStatusCode +
                                                       ". Technical details: " +
                                                       ex.getMessage ());
    }
    else
    {
      if (bIsError)
        m_aHCNode = new BootstrapErrorBox ().addChild ("Technical error occurred while querying the SMP. Technical details: " +
                                                       ex.getMessage ());
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
