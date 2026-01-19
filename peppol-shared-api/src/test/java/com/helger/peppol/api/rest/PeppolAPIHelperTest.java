package com.helger.peppol.api.rest;

import static org.junit.Assert.assertNotNull;

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

public class PeppolAPIHelperTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PeppolAPIHelperTest.class);

  @Test
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
