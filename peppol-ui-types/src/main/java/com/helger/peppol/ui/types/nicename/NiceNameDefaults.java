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
package com.helger.peppol.ui.types.nicename;

import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.peppolid.factory.PeppolIdentifierFactory;
import com.helger.peppolid.peppol.EPeppolCodeListItemState;

import jakarta.annotation.Nonnull;

/**
 * This class contains the default document type IDs and process IDs as defined in the Peppol code
 * lists.
 *
 * @author Philip Helger
 */
public final class NiceNameDefaults
{
  private static final ICommonsOrderedMap <String, NiceNameEntry> DEFAULT_DOCTYPES = new CommonsLinkedHashMap <> ();
  private static final ICommonsOrderedMap <String, NiceNameEntry> DEFAULT_PROCESSES = new CommonsLinkedHashMap <> ();

  @Nonnull
  private static String _ensurePrefix (@Nonnull final String sPrefix, @Nonnull final String s)
  {
    final String sReal = StringHelper.trimStart (s, "PEPPOL").trim ();

    if (sReal.startsWith (sPrefix))
      return sReal;
    return sPrefix + sReal;
  }

  static
  {
    for (final com.helger.peppolid.peppol.doctype.EPredefinedDocumentTypeIdentifier e : com.helger.peppolid.peppol.doctype.EPredefinedDocumentTypeIdentifier.values ())
      DEFAULT_DOCTYPES.put (e.getURIEncoded (),
                            new NiceNameEntry (_ensurePrefix ("Peppol ", e.getCommonName ()),
                                               e.getState (),
                                               e.getAllProcessIDs ()));
    for (final com.helger.peppolid.peppol.process.EPredefinedProcessIdentifier e : com.helger.peppolid.peppol.process.EPredefinedProcessIdentifier.values ())
      DEFAULT_PROCESSES.put (e.getURIEncoded (), new NiceNameEntry ("Peppol predefined", e.getState (), null));

    // Custom document types
    final PeppolIdentifierFactory PIF = PeppolIdentifierFactory.INSTANCE;
    DEFAULT_DOCTYPES.put ("busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:e-fff:ver3.0::2.1",
                          new NiceNameEntry ("e-FFF 3.0 Invoice",
                                             EPeppolCodeListItemState.DEPRECATED,
                                             new CommonsArrayList <> (PIF.createProcessIdentifierWithDefaultScheme ("urn:www.cenbii.eu:profile:bii05:ver1.0"))));
    DEFAULT_DOCTYPES.put ("busdox-docid-qns::urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:e-fff:ver3.0::2.1",
                          new NiceNameEntry ("e-FFF 3.0 CreditNote",
                                             EPeppolCodeListItemState.DEPRECATED,
                                             new CommonsArrayList <> (PIF.createProcessIdentifierWithDefaultScheme ("urn:www.cenbii.eu:profile:bii05:ver1.0"))));
  }

  private NiceNameDefaults ()
  {}

  @Nonnull
  @ReturnsMutableObject
  public static ICommonsOrderedMap <String, NiceNameEntry> defaultDocTypes ()
  {
    return DEFAULT_DOCTYPES;
  }

  @Nonnull
  @ReturnsMutableObject
  public static ICommonsOrderedMap <String, NiceNameEntry> defaultProcesses ()
  {
    return DEFAULT_PROCESSES;
  }
}
