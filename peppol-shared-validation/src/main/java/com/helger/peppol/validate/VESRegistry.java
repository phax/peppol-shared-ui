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
import com.helger.phive.cii.CIIValidation;
import com.helger.phive.ciuspt.CIUS_PTValidation;
import com.helger.phive.ciusro.CIUS_ROValidation;
import com.helger.phive.ebinterface.EbInterfaceValidation;
import com.helger.phive.ehf.EHFValidation;
import com.helger.phive.en16931.EN16931Validation;
import com.helger.phive.energieefactuur.EnergieEFactuurValidation;
import com.helger.phive.eracun.HReRacunValidation;
import com.helger.phive.facturae.FacturaeValidation;
import com.helger.phive.fatturapa.FatturaPAValidation;
import com.helger.phive.finvoice.FinvoiceValidation;
import com.helger.phive.france.FranceCTCValidation;
import com.helger.phive.isdoc.ISDOCValidation;
import com.helger.phive.ksef.KSeFValidation;
import com.helger.phive.oioubl.OIOUBLValidation;
import com.helger.phive.peppol.PeppolValidation;
import com.helger.phive.peppol.italy.PeppolItalyValidation;
import com.helger.phive.peppol.legacy.PeppolLegacyValidationBisAUNZ;
import com.helger.phive.peppol.legacy.PeppolLegacyValidationBisEurope;
import com.helger.phive.peppol.legacy.PeppolLegacyValidationSG;
import com.helger.phive.setu.SETUValidation;
import com.helger.phive.simplerinvoicing.SimplerInvoicingValidation;
import com.helger.phive.svefaktura.SvefakturaValidation;
import com.helger.phive.teapps.TEAPPSValidation;
import com.helger.phive.ubl.UBLValidation;
import com.helger.phive.ublbe.UBLBEValidation;
import com.helger.phive.xml.source.IValidationSourceXML;
import com.helger.phive.xrechnung.XRechnungValidation;
import com.helger.phive.zatca.ZATCAValidation;
import com.helger.phive.zugferd.ZugferdValidation;
import com.helger.text.compare.ComparatorHelper;

@SuppressWarnings ("deprecation")
@Immutable
public final class VESRegistry
{
  public static final ValidationExecutorSetRegistry <IValidationSourceXML> VES_REGISTRY = new ValidationExecutorSetRegistry <> ();
  static
  {
    // Must be first
    EN16931Validation.initEN16931 (VES_REGISTRY);
    // The rest in alphabetical order, except where explicitly stated
    CIIValidation.initCII (VES_REGISTRY);
    CIUS_PTValidation.initCIUS_PT (VES_REGISTRY);
    CIUS_ROValidation.initCIUS_RO (VES_REGISTRY);
    EbInterfaceValidation.initEbInterface (VES_REGISTRY);
    EHFValidation.initEHF (VES_REGISTRY);
    FacturaeValidation.initFacturae (VES_REGISTRY);
    FatturaPAValidation.initFatturaPA (VES_REGISTRY);
    FinvoiceValidation.initFinvoice (VES_REGISTRY);
    FranceCTCValidation.initFranceCTC (VES_REGISTRY);
    HReRacunValidation.init (VES_REGISTRY);
    KSeFValidation.initKSeF (VES_REGISTRY);
    ISDOCValidation.initISDOC (VES_REGISTRY);
    OIOUBLValidation.initOIOUBL (VES_REGISTRY);
    PeppolItalyValidation.init (VES_REGISTRY);
    PeppolValidation.initStandard (VES_REGISTRY);
    PeppolLegacyValidationBisEurope.init (VES_REGISTRY);
    PeppolLegacyValidationBisAUNZ.init (VES_REGISTRY);
    PeppolLegacyValidationSG.init (VES_REGISTRY);
    SETUValidation.initSETU (VES_REGISTRY);
    SimplerInvoicingValidation.initSimplerInvoicing (VES_REGISTRY);
    // Must be after SimplerInvoicing
    EnergieEFactuurValidation.initEnergieEFactuur (VES_REGISTRY);
    SvefakturaValidation.initSvefaktura (VES_REGISTRY);
    TEAPPSValidation.initTEAPPS (VES_REGISTRY);
    UBLValidation.initUBLAllVersions (VES_REGISTRY);
    UBLBEValidation.initUBLBE (VES_REGISTRY);
    XRechnungValidation.initXRechnung (VES_REGISTRY);
    ZATCAValidation.initZATCA (VES_REGISTRY);
    ZugferdValidation.initZugferd (VES_REGISTRY);
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
