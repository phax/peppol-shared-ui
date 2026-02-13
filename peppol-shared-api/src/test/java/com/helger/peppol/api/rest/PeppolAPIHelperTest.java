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
package com.helger.peppol.api.rest;

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.peppol.businesscard.generic.PDBusinessCard;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.ui.types.feedbackcb.FeedbackCallbackLog;
import com.helger.peppol.ui.types.smp.SMPQueryParams;
import com.helger.peppolid.factory.PeppolIdentifierFactory;
import com.helger.peppolid.peppol.PeppolIdentifierHelper;

/**
 * Test class for class {@link PeppolAPIHelper}
 *
 * @author Philip Helger
 */
public final class PeppolAPIHelperTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PeppolAPIHelperTest.class);

  @Test
  public void testReadBCHelgerSMK ()
  {
    final SMPQueryParams aSMPQueryParams = SMPQueryParams.createForSMLOrNull (ESML.DIGIT_TEST,
                                                                              ESMPAPIType.PEPPOL,
                                                                              PeppolIdentifierFactory.INSTANCE,
                                                                              PeppolIdentifierHelper.DEFAULT_PARTICIPANT_SCHEME,
                                                                              "9915:helger",
                                                                              true);
    assertNotNull (aSMPQueryParams);
    final PDBusinessCard aBC = PeppolAPIHelper.retrieveBusinessCardParsed ("",
                                                                           aSMPQueryParams,
                                                                           x -> {},
                                                                           new FeedbackCallbackLog (LOGGER, ""),
                                                                           ex -> LOGGER.error ("oops", ex));
    assertNotNull (aBC);
  }

  @Test
  @Ignore ("No longer present")
  public void testReadBCOnfactNowValid ()
  {
    final SMPQueryParams aSMPQueryParams = SMPQueryParams.createForSMLOrNull (ESML.DIGIT_PRODUCTION,
                                                                              ESMPAPIType.PEPPOL,
                                                                              PeppolIdentifierFactory.INSTANCE,
                                                                              PeppolIdentifierHelper.DEFAULT_PARTICIPANT_SCHEME,
                                                                              "9925:be078220767",
                                                                              true);
    assertNotNull (aSMPQueryParams);
    final PDBusinessCard aBC = PeppolAPIHelper.retrieveBusinessCardParsed ("",
                                                                           aSMPQueryParams,
                                                                           x -> {},
                                                                           new FeedbackCallbackLog (LOGGER, ""),
                                                                           ex -> LOGGER.error ("oops", ex));
    assertNotNull (aBC);
  }
}
