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
package com.helger.peppol.sharedui.page.pub;

import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.base.string.StringRemove;
import com.helger.cache.regex.RegExHelper;
import com.helger.collection.commons.CommonsLinkedHashSet;
import com.helger.collection.commons.ICommonsCollection;
import com.helger.collection.commons.ICommonsOrderedSet;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.forms.EHCFormMethod;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.masterdata.vat.VATINSyntaxChecker;
import com.helger.peppol.servicedomain.EPeppolNetwork;
import com.helger.peppol.sharedui.page.AbstractAppWebPage;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.ui.types.mgr.PhotonPeppolMetaManager;
import com.helger.peppol.ui.types.smlconfig.ISMLConfiguration;
import com.helger.peppol.ui.types.smlconfig.ISMLConfigurationManager;
import com.helger.peppol.ui.types.smp.SMPQueryParams;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.peppol.PeppolIdentifierHelper;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.button.BootstrapLinkButton;
import com.helger.photon.bootstrap4.buttongroup.BootstrapButtonToolbar;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.photon.uictrls.famfam.EFamFamIcon;
import com.helger.url.ISimpleURL;
import com.helger.url.SimpleURL;

public class PagePublicToolsPeppolParticipantCheckBelgium extends AbstractAppWebPage
{
  public static final String FIELD_ID_VALUE = "value";

  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicToolsPeppolParticipantCheckBelgium.class);

  public PagePublicToolsPeppolParticipantCheckBelgium (@NonNull @Nonempty final String sID)
  {
    this (sID, "Belgium Peppol Participant Check");
  }

  public PagePublicToolsPeppolParticipantCheckBelgium (@NonNull @Nonempty final String sID,
                                                 @NonNull @Nonempty final String sName)
  {
    super (sID, sName);
  }

  @NonNull
  static IHCNode createPeppolDirectoryButton (@NonNull final IParticipantIdentifier aPID)
  {
    final ISimpleURL aDirectoryURL = new SimpleURL (EPeppolNetwork.PRODUCTION.getDirectoryURL () +
                                                    "/participant/" +
                                                    aPID.getURIPercentEncoded ());
    return new BootstrapLinkButton ().addChild ("Peppol Directory Lookup").setHref (aDirectoryURL).setTargetBlank ();
  }

  private void _checkParticipants (@NonNull final WebPageExecutionContext aWPEC,
                                   @NonNull final ICommonsCollection <String> aParticipantIDs)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final ISMLConfigurationManager aSMLConfigurationMgr = PhotonPeppolMetaManager.getSMLConfigurationMgr ();

    if (aParticipantIDs.size () > 1)
      aNodeList.addChild (div ("Now checking " + aParticipantIDs.size () + " different values"));

    for (final String sParticipantIDValue : aParticipantIDs)
    {
      LOGGER.info ("Performing Belgium Participant Check for '" + sParticipantIDValue + "'");

      if (aParticipantIDs.size () > 1)
        aNodeList.addChild (h2 (code (sParticipantIDValue)));

      if (!VATINSyntaxChecker.isValidVATIN_BE (sParticipantIDValue))
        aNodeList.addChild (warn ("The CBE number '" +
                                  sParticipantIDValue +
                                  "' does not seem to match the syntax requirements (length 10, start with 0 or 1, mod97 check digit)"));

      final ISMLConfiguration aSMLConfiguration = aSMLConfigurationMgr.getSMLConfigurationfID (ESML.DIGIT_PRODUCTION.getID ());

      // 0208 does not use prefix
      final SMPQueryParams aSMPQP_CBE = SMPQueryParams.createForSMLOrNull (aSMLConfiguration,
                                                                           PeppolIdentifierHelper.DEFAULT_PARTICIPANT_SCHEME,
                                                                           "0208:" + sParticipantIDValue,
                                                                           false);
      final boolean bIsCBE = aSMPQP_CBE == null ? false : aSMPQP_CBE.isSMPRegisteredInDNS ();

      // 9925 uses prefix
      final SMPQueryParams aSMPQP_VAT = SMPQueryParams.createForSMLOrNull (aSMLConfiguration,
                                                                           PeppolIdentifierHelper.DEFAULT_PARTICIPANT_SCHEME,
                                                                           "9925:be" + sParticipantIDValue,
                                                                           false);
      final boolean bIsVAT = aSMPQP_VAT == null ? false : aSMPQP_VAT.isSMPRegisteredInDNS ();

      if (bIsCBE || bIsVAT)
      {
        if (bIsCBE)
        {
          final IParticipantIdentifier aPID = aSMPQP_CBE.getParticipantID ();
          aNodeList.addChild (success ("The Belgium Enterprise is registered in the Peppol Production Network with their CBE number and the Participant ID ").addChild (code (aPID.getURIEncoded ())));

          final BootstrapButtonToolbar aToolbar = new BootstrapButtonToolbar (aWPEC);
          aToolbar.addChild (new BootstrapButton ().addChild ("Participant Information")
                                                   .setIcon (EFamFamIcon.USER_GREEN)
                                                   .setOnClick (aWPEC.getLinkToMenuItem (CSharedUIMenuPublic.MENU_TOOLS_PARTICIPANT_INFO)
                                                                     .add (PagePublicToolsParticipantInformation.FIELD_ID_SCHEME,
                                                                           aPID.getScheme ())
                                                                     .add (PagePublicToolsParticipantInformation.FIELD_ID_VALUE,
                                                                           aPID.getValue ())
                                                                     .add (PagePublicToolsParticipantInformation.FIELD_SML,
                                                                           aSMLConfiguration.getID ())
                                                                     .add (PagePublicToolsParticipantInformation.PARAM_QUERY_BUSINESS_CARD,
                                                                           true)
                                                                     .add (PagePublicToolsParticipantInformation.PARAM_VERIFY_SIGNATURES,
                                                                           true)
                                                                     .add (PagePublicToolsParticipantInformation.PARAM_XSD_VALIDATION,
                                                                           true)
                                                                     .add (CPageParam.PARAM_ACTION,
                                                                           CPageParam.ACTION_PERFORM)));

          aToolbar.addChild (createPeppolDirectoryButton (aPID));
          aNodeList.addChild (aToolbar);
        }
        if (bIsVAT)
        {
          final IParticipantIdentifier aPID = aSMPQP_VAT.getParticipantID ();
          aNodeList.addChild (success ("The Belgium Enterprise is registered in the Peppol Production Network with their VATIN and the Participant ID ").addChild (code (aPID.getURIEncoded ())));

          final BootstrapButtonToolbar aToolbar = new BootstrapButtonToolbar (aWPEC);
          aToolbar.addChild (new BootstrapButton ().addChild ("Participant Information")
                                                   .setIcon (EFamFamIcon.USER_GREEN)
                                                   .setOnClick (aWPEC.getLinkToMenuItem (CSharedUIMenuPublic.MENU_TOOLS_PARTICIPANT_INFO)
                                                                     .add (PagePublicToolsParticipantInformation.FIELD_ID_SCHEME,
                                                                           aPID.getScheme ())
                                                                     .add (PagePublicToolsParticipantInformation.FIELD_ID_VALUE,
                                                                           aPID.getValue ())
                                                                     .add (PagePublicToolsParticipantInformation.FIELD_SML,
                                                                           aSMLConfiguration.getID ())
                                                                     .add (CPageParam.PARAM_ACTION,
                                                                           CPageParam.ACTION_PERFORM)));

          aToolbar.addChild (createPeppolDirectoryButton (aPID));
          aNodeList.addChild (aToolbar);
        }
      }
      else
      {
        aNodeList.addChild (error ("The Belgium Enterprise was not found in the Peppol Production Network - neither with the CBE number nor with the VAT number"));
      }
    }
  }

  @Override
  protected void fillContent (@NonNull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bShowInput = true;

    aNodeList.addChild (info ("With this page you can check if a Belgium enterprise is registered to the Peppol Production Network." +
                              " You can test with either a ").addChild (strong ("CBE number"))
                                                             .addChild (" or a ")
                                                             .addChild (strong ("VAT number")));

    boolean bQueried = false;
    final ICommonsOrderedSet <String> aParticipantIDs = new CommonsLinkedHashSet <> ();
    if (aWPEC.hasAction (CPageParam.ACTION_PERFORM))
    {
      // Validate fields
      final String sParticipantIDValue = StringHelper.trim (aWPEC.params ().getAsString (FIELD_ID_VALUE));
      if (sParticipantIDValue != null)
      {
        for (final String sPart : RegExHelper.getSplitToArray (sParticipantIDValue, "\\s+"))
        {
          // CBE values may contain "." chars
          String sID = StringRemove.removeAll (sPart, '.');
          // Lowercase
          sID = sID.toLowerCase (Locale.US);
          // Remove any "BE" prefix
          sID = StringHelper.trimStart (sID, "be");
          // Remove prefixes
          sID = StringHelper.trimStart (sID, "0208:");
          sID = StringHelper.trimStart (sID, "9925:");

          if (!sID.isEmpty ())
            aParticipantIDs.add (sID);
        }
      }

      if (aParticipantIDs.isEmpty ())
        aFormErrors.addFieldError (FIELD_ID_VALUE, "Please provide an value to check");

      if (aFormErrors.isEmpty ())
      {
        _checkParticipants (aWPEC, aParticipantIDs);
        bQueried = true;
      }
    }

    if (bShowInput)
    {
      final BootstrapForm aForm = aNodeList.addAndReturnChild (getUIHandler ().createFormSelf (aWPEC)
                                                                              .setMethod (EHCFormMethod.GET)
                                                                              .setLeft (3, 3, 3, 2, 2));
      if (bQueried)
      {
        // Just some spacing
        aForm.addClass (CBootstrapCSS.MT_5);
      }

      aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Identifier value")
                                                   .setCtrl (new HCTextArea (new RequestField (FIELD_ID_VALUE,
                                                                                               StringImplode.imploder ()
                                                                                                            .source (aParticipantIDs)
                                                                                                            .separator (' ')
                                                                                                            .build ())).setPlaceholder ("Identifier value(s)")
                                                                                                                       .setRows (3))
                                                   .setHelpText (div ("This should either be the CBE number or the VAT number of the enterprise your want to check." +
                                                                      " Multiple numbers can be provided with blanks as separators."))
                                                   .setErrorList (aFormErrors.getListOfField (FIELD_ID_VALUE)));

      final BootstrapButtonToolbar aToolbar = aForm.addAndReturnChild (new BootstrapButtonToolbar (aWPEC));
      aToolbar.addHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM);
      aToolbar.addSubmitButton ("Check Existence");
    }
  }
}
