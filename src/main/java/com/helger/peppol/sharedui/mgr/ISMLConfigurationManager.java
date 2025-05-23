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
package com.helger.peppol.sharedui.mgr;

import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.state.EChange;
import com.helger.peppol.sharedui.domain.ISMLConfiguration;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppolid.factory.ESMPIdentifierType;

/**
 * Base interface for a manager that handles {@link ISMLInfo} objects.
 *
 * @author Philip Helger
 */
public interface ISMLConfigurationManager
{
  /**
   * Special ID used for "automatic detection" of SML
   */
  String ID_AUTO_DETECT = "autodetect";

  /**
   * Create a new SML information.
   *
   * @param sSMLInfoID
   *        Internal object ID. May neither be <code>null</code> nor empty.
   * @param sDisplayName
   *        The "shorthand" display name like "SML" or "SMK". May neither be
   *        <code>null</code> nor empty.
   * @param sDNSZone
   *        The DNS zone on which this SML is operating. May not be
   *        <code>null</code>. It must be ensured that the value consists only
   *        of lower case characters for comparability! Example:
   *        <code>sml.peppolcentral.org</code>
   * @param sManagementServiceURL
   *        The service URL where the management application is running on incl.
   *        the host name. May not be <code>null</code>. The difference to the
   *        host name is the eventually present context path.
   * @param bClientCertificateRequired
   *        <code>true</code> if this SML requires a client certificate for
   *        access, <code>false</code> otherwise.<br>
   *        Both Peppol production SML and SMK require a client certificate.
   *        Only a locally running SML software may not require a client
   *        certificate.
   * @param eSMPAPIType
   *        SMP API type. May not be <code>null</code>.
   * @param eSMPIdentifierType
   *        SMP identifier type. May not be <code>null</code>.
   * @param bProduction
   *        <code>true</code> if production SML, <code>false</code> if test
   * @return Never <code>null</code>.
   */
  @Nonnull
  ISMLConfiguration createSMLInfo (@Nonnull @Nonempty String sSMLInfoID,
                                   @Nonnull @Nonempty String sDisplayName,
                                   @Nonnull @Nonempty String sDNSZone,
                                   @Nonnull @Nonempty String sManagementServiceURL,
                                   boolean bClientCertificateRequired,
                                   @Nonnull ESMPAPIType eSMPAPIType,
                                   @Nonnull ESMPIdentifierType eSMPIdentifierType,
                                   boolean bProduction);

  /**
   * Update an existing SML information.
   *
   * @param sSMLInfoID
   *        The ID of the SML information to be updated. May be
   *        <code>null</code>.
   * @param sDisplayName
   *        The "shorthand" display name like "SML" or "SMK". May neither be
   *        <code>null</code> nor empty.
   * @param sDNSZone
   *        The DNS zone on which this SML is operating. May not be
   *        <code>null</code>. It must be ensured that the value consists only
   *        of lower case characters for comparability! Example:
   *        <code>sml.peppolcentral.org</code>
   * @param sManagementServiceURL
   *        The service URL where the management application is running on incl.
   *        the host name. May not be <code>null</code>. The difference to the
   *        host name is the eventually present context path.
   * @param bClientCertificateRequired
   *        <code>true</code> if this SML requires a client certificate for
   *        access, <code>false</code> otherwise.<br>
   *        Both Peppol production SML and SMK require a client certificate.
   *        Only a locally running SML software may not require a client
   *        certificate.
   * @param eSMPAPIType
   *        SMP API type. May not be <code>null</code>.
   * @param eSMPIdentifierType
   *        SMP identifier type. May not be <code>null</code>.
   * @param bProduction
   *        <code>true</code> if production SML, <code>false</code> if test
   * @return {@link EChange#CHANGED} if something was changed.
   */
  @Nonnull
  EChange updateSMLInfo (@Nullable String sSMLInfoID,
                         @Nonnull @Nonempty String sDisplayName,
                         @Nonnull @Nonempty String sDNSZone,
                         @Nonnull @Nonempty String sManagementServiceURL,
                         boolean bClientCertificateRequired,
                         @Nonnull ESMPAPIType eSMPAPIType,
                         @Nonnull ESMPIdentifierType eSMPIdentifierType,
                         boolean bProduction);

  /**
   * Delete an existing SML information.
   *
   * @param sSMLInfoID
   *        The ID of the SML information to be deleted. May be
   *        <code>null</code>.
   * @return {@link EChange#CHANGED} if the removal was successful.
   */
  @Nullable
  EChange removeSMLInfo (@Nullable String sSMLInfoID);

  /**
   * @return An unsorted collection of all contained SML information. Never
   *         <code>null</code> but maybe empty.
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsList <ISMLConfiguration> getAll ();

  @Nonnull
  @ReturnsMutableCopy
  ICommonsList <ISMLConfiguration> getAllSorted ();

  /**
   * Get the SML information with the passed ID.
   *
   * @param sID
   *        The ID to be resolved. May be <code>null</code>.
   * @return <code>null</code> if no such SML information exists.
   */
  @Nullable
  ISMLConfiguration getSMLInfoOfID (@Nullable String sID);

  /**
   * Find the first SML information that matches the provided predicate.
   *
   * @param aFilter
   *        The predicate to be applied for searching. May not be
   *        <code>null</code>.
   * @return <code>null</code> if no such SML information exists.
   */
  @Nullable
  ISMLConfiguration findFirst (@Nullable Predicate <? super ISMLConfiguration> aFilter);

  /**
   * Check if a SML information with the passed ID is contained.
   *
   * @param sID
   *        The ID of the SML information to be checked. May be
   *        <code>null</code>.
   * @return <code>true</code> if the ID is contained, <code>false</code>
   *         otherwise.
   */
  boolean containsSMLInfoWithID (@Nullable String sID);
}
