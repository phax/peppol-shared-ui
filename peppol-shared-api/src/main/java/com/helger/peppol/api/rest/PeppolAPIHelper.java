package com.helger.peppol.api.rest;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.string.StringHelper;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.peppol.businesscard.generic.PDBusinessCard;
import com.helger.peppol.businesscard.helper.PDBusinessCardHelper;
import com.helger.peppol.ui.types.minicallback.IMiniCallback;
import com.helger.peppol.ui.types.smp.SMPQueryParams;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.smpclient.httpclient.SMPHttpClientSettings;

@Immutable
public final class PeppolAPIHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PeppolAPIHelper.class);

  private PeppolAPIHelper ()
  {}

  @NonNull
  public static String getBusinessCardURL (@NonNull final SMPQueryParams aSMPQueryParams)
  {
    final IParticipantIdentifier aParticipantID = aSMPQueryParams.getParticipantID ();
    final URI aSMPHostURI = aSMPQueryParams.getSMPHostURI ();
    return StringHelper.trimEnd (aSMPHostURI.toString (), '/') + "/businesscard/" + aParticipantID.getURIEncoded ();
  }

  @Nullable
  public static byte [] retrieveBusinessCard (@NonNull final String sLogPrefix,
                                              @NonNull final SMPQueryParams aSMPQueryParams,
                                              @Nullable final Consumer <? super SMPHttpClientSettings> aHCSModifier,
                                              @NonNull final IMiniCallback aMiniCallback)
  {
    LOGGER.info (sLogPrefix +
                 "BusinessCard of '" +
                 aSMPQueryParams.getParticipantID ().getURIEncoded () +
                 "' is queried using SMP API '" +
                 aSMPQueryParams.getSMPAPIType () +
                 "' from '" +
                 aSMPQueryParams.getSMPHostURI () +
                 "' using SML '" +
                 aSMPQueryParams.getSMLInfo ().getID () +
                 "'");

    final String sBCURL = getBusinessCardURL (aSMPQueryParams);
    LOGGER.info (sLogPrefix + "  Querying BC from '" + sBCURL + "'");

    final SMPHttpClientSettings aHCS = new SMPHttpClientSettings ();
    if (aHCSModifier != null)
      aHCSModifier.accept (aHCS);

    byte [] aBCBytes;
    try (final HttpClientManager aHttpClientMgr = HttpClientManager.create (aHCS))
    {
      final HttpGet aGet = new HttpGet (sBCURL);
      aBCBytes = aHttpClientMgr.execute (aGet, new ResponseHandlerByteArray ());
    }
    catch (final Exception ex)
    {
      aBCBytes = null;
    }

    if (aBCBytes == null)
      aMiniCallback.warn ("No Business Card is available for that participant.");

    return aBCBytes;
  }

  @Nullable
  public static PDBusinessCard retrieveBusinessCardParsed (@NonNull final String sLogPrefix,
                                                           @NonNull final SMPQueryParams aSMPQueryParams,
                                                           @Nullable final Consumer <? super HttpClientSettings> aHCSModifier,
                                                           @NonNull final IMiniCallback aMiniCallback)
  {
    final byte [] aBCBytes = retrieveBusinessCard (sLogPrefix, aSMPQueryParams, aHCSModifier, aMiniCallback);
    if (aBCBytes != null)
    {
      final PDBusinessCard aBC = PDBusinessCardHelper.parseBusinessCard (aBCBytes, StandardCharsets.UTF_8);
      if (aBC != null)
      {
        // Business Card found
        return aBC;
      }
      aMiniCallback.error ("Failed to parse BC:\n" + new String (aBCBytes, StandardCharsets.UTF_8));
    }
    return null;
  }

}
