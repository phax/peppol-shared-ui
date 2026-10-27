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
package com.helger.peppol.ui.types.nicename;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;

import com.helger.io.resource.FileSystemResource;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.factory.SimpleIdentifierFactory;

/**
 * Test class for class {@link NiceNameManager}.
 *
 * @author Philip Helger
 */
public final class NiceNameManagerTest
{
  private static final String PINT_OM_INVOICE = "peppol-doctype-wildcard::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@om-1::2.1";
  private static final String PINT_OM_INVOICE_WILDCARD = "peppol-doctype-wildcard::urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:peppol:pint:billing-1@om-1*::2.1";

  @After
  public void after ()
  {
    NiceNameManager.setAllToDefaults ();
  }

  private static void _checkPint ()
  {
    final IDocumentTypeIdentifier aExact = SimpleIdentifierFactory.INSTANCE.parseDocumentTypeIdentifier (PINT_OM_INVOICE);
    NiceNameEntry aNN = NiceNameManager.getPintEnabledNiceNameEntry (aExact);
    assertNotNull (aNN);
    assertEquals ("OM PINT Invoice v1.0 (PINT exact match)", aNN.getName ());

    final IDocumentTypeIdentifier aWildcard = SimpleIdentifierFactory.INSTANCE.parseDocumentTypeIdentifier (PINT_OM_INVOICE_WILDCARD);
    aNN = NiceNameManager.getPintEnabledNiceNameEntry (aWildcard);
    assertNotNull (aNN);
    assertEquals ("OM PINT Invoice v1.0 (PINT wildcard match)", aNN.getName ());

    aNN = NiceNameManager.getPintResolvedNiceNameEntry (aExact);
    assertNotNull (aNN);
    assertEquals ("OM PINT Invoice v1.0", aNN.getName ());

    aNN = NiceNameManager.getPintResolvedNiceNameEntry (aWildcard);
    assertNotNull (aNN);
    assertEquals ("OM PINT Invoice v1.0", aNN.getName ());
  }

  @Test
  public void testPintFromDefaults ()
  {
    NiceNameManager.setAllToDefaults ();
    _checkPint ();
  }

  @Test
  public void testPintFromXML ()
  {
    NiceNameManager.loadDocTypeNames (new FileSystemResource ("docs/doctypeid-mapping.xml"));
    _checkPint ();
  }
}
