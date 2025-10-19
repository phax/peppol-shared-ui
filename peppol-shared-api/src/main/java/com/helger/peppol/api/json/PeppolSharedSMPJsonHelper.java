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
package com.helger.peppol.api.json;

import java.util.Locale;
import java.util.Map;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.rt.StackTraceHelper;
import com.helger.base.state.ETriState;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.level.EErrorLevel;
import com.helger.diagnostics.error.level.IErrorLevel;
import com.helger.json.IJson;
import com.helger.json.IJsonArray;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.ui.types.nicename.NiceNameEntry;
import com.helger.peppol.ui.types.nicename.NiceNameHandler;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.smpclient.json.SMPJsonResponse;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@Immutable
public final class PeppolSharedSMPJsonHelper
{
  private static final String JSON_NICE_NAME = "niceName";
  private static final String JSON_STATE = "state";
  private static final String JSON_IS_DEPRECATED = "isDeprecated";

  private PeppolSharedSMPJsonHelper ()
  {}

  @Nonnull
  public static String getErrorLevel (@Nonnull final IErrorLevel aErrorLevel)
  {
    if (aErrorLevel.isGE (EErrorLevel.ERROR))
      return "ERROR";
    if (aErrorLevel.isGE (EErrorLevel.WARN))
      return "WARN";
    return "SUCCESS";
  }

  @Nonnull
  public static String getTriState (@Nonnull final ETriState eTriState)
  {
    if (eTriState.isTrue ())
      return "TRUE";
    if (eTriState.isFalse ())
      return "FALSE";
    return "UNDEFINED";
  }

  @Nullable
  public static IJsonObject getStackTrace (@Nullable final Throwable t)
  {
    if (t == null)
      return null;
    return new JsonObject ().add ("class", t.getClass ().getName ())
                            .addIfNotNull ("message", t.getMessage ())
                            .add ("stackTrace", StackTraceHelper.getStackAsString (t));
  }

  @Nonnull
  public static IJsonObject createItem (@Nonnull final IErrorLevel aErrorLevel,
                                        @Nullable final String sErrorID,
                                        @Nullable final String sErrorFieldName,
                                        @Nullable final String sErrorLocation,
                                        @Nonnull final String sErrorText,
                                        @Nullable final Throwable t)
  {
    return new JsonObject ().add ("errorLevel", getErrorLevel (aErrorLevel))
                            .addIfNotNull ("errorID", sErrorID)
                            .addIfNotNull ("errorFieldName", sErrorFieldName)
                            .addIfNotNull ("errorLocation", sErrorLocation)
                            .add ("errorText", sErrorText)
                            .addIfNotNull ("exception", getStackTrace (t));
  }

  @Nonnull
  public static IJsonObject createItem (@Nonnull final IError aError, @Nonnull final Locale aDisplayLocale)
  {
    return createItem (aError.getErrorLevel (),
                       aError.getErrorID (),
                       aError.getErrorFieldName (),
                       aError.hasErrorLocation () ? aError.getErrorLocation ().getAsString () : null,
                       aError.getErrorText (aDisplayLocale),
                       aError.getLinkedException ());
  }

  @Nonnull
  public static IJsonObject convert (@Nonnull final ESMPAPIType eSMPAPIType,
                                     @Nonnull final IParticipantIdentifier aParticipantID,
                                     @Nonnull final Map <String, String> aSGHrefs,
                                     @Nonnull final IIdentifierFactory aIF)
  {
    final IJsonObject aJson = SMPJsonResponse.convert (eSMPAPIType, aParticipantID, aSGHrefs, aIF);
    final IJsonArray aURLsArray = aJson.getAsArray (SMPJsonResponse.JSON_URLS);
    if (aURLsArray != null)
      for (final IJson aEntry : aURLsArray)
        if (aEntry.isObject ())
        {
          final IJsonObject aUrlEntry = aEntry.getAsObject ();
          final String sDocType = aUrlEntry.getAsString (SMPJsonResponse.JSON_DOCUMENT_TYPE_ID);
          if (sDocType != null)
          {
            final NiceNameEntry aNN = NiceNameHandler.getDocTypeNiceName (sDocType);
            if (aNN != null)
            {
              aUrlEntry.add (JSON_NICE_NAME, aNN.getName ());
              aUrlEntry.add (JSON_STATE, aNN.getState ().getID ());
              aUrlEntry.add (JSON_IS_DEPRECATED, aNN.getState ().isDeprecated ());
            }
          }
        }
    return aJson;
  }
}
