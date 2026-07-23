/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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
package com.helger.peppol.validate;

import java.util.Comparator;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.name.IHasDisplayName;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.diver.api.coord.DVRCoordinate;
import com.helger.json.IJsonArray;
import com.helger.json.JsonArray;
import com.helger.json.JsonObject;
import com.helger.phive.api.executorset.IValidationExecutorSet;
import com.helger.phive.api.executorset.ValidationExecutorSetRegistry;
import com.helger.phive.peppol.legacy.PeppolLegacyValidationBisAUNZ;
import com.helger.phive.peppol.legacy.PeppolLegacyValidationBisEurope;
import com.helger.phive.peppol.legacy.PeppolLegacyValidationSG;
import com.helger.phive.rules.all.PhiveRulesValidation;
import com.helger.phive.xml.source.IValidationSourceXML;
import com.helger.text.compare.ComparatorHelper;

@Immutable
public final class VESRegistry
{
  public static final ValidationExecutorSetRegistry <IValidationSourceXML> VES_REGISTRY = new ValidationExecutorSetRegistry <> ();
  static
  {
    PhiveRulesValidation.initPhiveRules (VES_REGISTRY);

    // Legacy
    PeppolLegacyValidationBisAUNZ.init (VES_REGISTRY);
    PeppolLegacyValidationBisEurope.init (VES_REGISTRY);
    PeppolLegacyValidationSG.init (VES_REGISTRY);
  }

  private VESRegistry ()
  {}

  @NonNull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <DVRCoordinate, IValidationExecutorSet <IValidationSourceXML>> getAllSortedByDisplayName (@NonNull final Locale aDisplayLocale)
  {
    final ICommonsMap <DVRCoordinate, IValidationExecutorSet <IValidationSourceXML>> aMap = new CommonsHashMap <> (VES_REGISTRY.getAll (),
                                                                                                                   IValidationExecutorSet::getID,
                                                                                                                   x -> x);
    return aMap.getSortedByValue (ComparatorHelper.getComparatorCollating (IHasDisplayName::getDisplayName,
                                                                           aDisplayLocale));
  }

  @NonNull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <DVRCoordinate, IValidationExecutorSet <IValidationSourceXML>> getAllSortedByID ()
  {
    final ICommonsMap <DVRCoordinate, IValidationExecutorSet <IValidationSourceXML>> aMap = new CommonsHashMap <> (VES_REGISTRY.getAll (),
                                                                                                                   IValidationExecutorSet::getID,
                                                                                                                   x -> x);
    return aMap.getSortedByKey (Comparator.naturalOrder ());
  }

  @Nullable
  public static IValidationExecutorSet <IValidationSourceXML> getFromIDOrNull (@Nullable final DVRCoordinate aID)
  {
    return VES_REGISTRY.getOfID (aID);
  }

  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <IValidationExecutorSet <IValidationSourceXML>> getAll ()
  {
    return VES_REGISTRY.getAll ();
  }

  public static void cleanupOnShutdown ()
  {
    VES_REGISTRY.removeAll ();
  }

  @NonNull
  public static IJsonArray getAllAsJson ()
  {
    final IJsonArray aJsonArray = new JsonArray ();
    for (final IValidationExecutorSet <IValidationSourceXML> aVES : VESRegistry.getAll ())
    {
      aJsonArray.add (new JsonObject ().add ("vesid", aVES.getID ().getAsSingleID ())
                                       .add ("name", aVES.getDisplayName ())
                                       .add ("deprecated", aVES.getStatus ().isDeprecated ()));
    }
    return aJsonArray;
  }
}
