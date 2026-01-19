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
package com.helger.peppol.ui.types.smlconfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.state.EChange;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.ICommonsList;
import com.helger.dao.DAOException;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.sml.SMLInfo;
import com.helger.peppolid.factory.ESMPIdentifierType;
import com.helger.photon.audit.AuditHelper;
import com.helger.photon.io.dao.AbstractPhotonMapBasedWALDAO;

public final class SMLConfigurationManager extends AbstractPhotonMapBasedWALDAO <ISMLConfiguration, SMLConfiguration>
                                           implements
                                           ISMLConfigurationManager
{
  public SMLConfigurationManager (@NonNull @Nonempty final String sFilename) throws DAOException
  {
    super (SMLConfiguration.class, sFilename);
  }

  @Override
  @NonNull
  protected EChange onInit ()
  {
    // Add the default transport profiles
    for (final ESML e : ESML.values ())
      internalCreateItem (SMLConfiguration.createForPeppol (e));
    return EChange.CHANGED;
  }

  @NonNull
  public ISMLConfiguration createSMLConfiguration (@NonNull @Nonempty final String sSMLInfoID,
                                                   @NonNull @Nonempty final String sDisplayName,
                                                   @NonNull @Nonempty final String sDNSZone,
                                                   @NonNull @Nonempty final String sManagementServiceURL,
                                                   @NonNull final String sURLSuffixManageSMP,
                                                   @NonNull final String sURLSuffixManageParticipant,
                                                   final boolean bClientCertificateRequired,
                                                   @NonNull final ESMPAPIType eSMPAPIType,
                                                   @NonNull final ESMPIdentifierType eSMPIdentifierType,
                                                   final boolean bProduction,
                                                   final int nPriority)
  {
    final SMLInfo aSMLInfo = SMLInfo.builder ()
                                    .id (sSMLInfoID)
                                    .displayName (sDisplayName)
                                    .dnsZone (sDNSZone)
                                    .managementServiceURL (sManagementServiceURL)
                                    .urlSuffixManageSMP (sURLSuffixManageSMP)
                                    .urlSuffixManageParticipant (sURLSuffixManageParticipant)
                                    .clientCertificateRequired (bClientCertificateRequired)
                                    .build ();
    final SMLConfiguration aSMLConfig = new SMLConfiguration (aSMLInfo,
                                                              eSMPAPIType,
                                                              eSMPIdentifierType,
                                                              bProduction,
                                                              nPriority);

    m_aRWLock.writeLocked ( () -> { internalCreateItem (aSMLConfig); });
    AuditHelper.onAuditCreateSuccess (SMLConfiguration.OT,
                                      sSMLInfoID,
                                      sDisplayName,
                                      sDNSZone,
                                      sManagementServiceURL,
                                      sURLSuffixManageSMP,
                                      sURLSuffixManageParticipant,
                                      Boolean.valueOf (bClientCertificateRequired),
                                      eSMPAPIType,
                                      eSMPIdentifierType,
                                      Boolean.valueOf (bProduction),
                                      Integer.valueOf (nPriority));
    return aSMLConfig;
  }

  @NonNull
  public EChange updateSMLConfiguration (@Nullable final String sSMLInfoID,
                                         @NonNull @Nonempty final String sDisplayName,
                                         @NonNull @Nonempty final String sDNSZone,
                                         @NonNull @Nonempty final String sManagementServiceURL,
                                         @NonNull final String sURLSuffixManageSMP,
                                         @NonNull final String sURLSuffixManageParticipant,
                                         final boolean bClientCertificateRequired,
                                         @NonNull final ESMPAPIType eSMPAPIType,
                                         @NonNull final ESMPIdentifierType eSMPIdentifierType,
                                         final boolean bProduction,
                                         final int nPriority)
  {
    final SMLConfiguration aExtSMLInfo = getOfID (sSMLInfoID);
    if (aExtSMLInfo == null)
    {
      AuditHelper.onAuditModifyFailure (SMLConfiguration.OT, "all", sSMLInfoID, "no-such-id");
      return EChange.UNCHANGED;
    }

    m_aRWLock.writeLock ().lock ();
    try
    {
      final SMLInfo aSMLInfo = aExtSMLInfo.getSMLInfo ();
      final SMLInfo aNewSMLInfo = SMLInfo.builder (aSMLInfo)
                                         .displayName (sDisplayName)
                                         .dnsZone (sDNSZone)
                                         .managementServiceURL (sManagementServiceURL)
                                         .urlSuffixManageSMP (sURLSuffixManageSMP)
                                         .urlSuffixManageParticipant (sURLSuffixManageParticipant)
                                         .clientCertificateRequired (bClientCertificateRequired)
                                         .build ();

      EChange eChange = EChange.UNCHANGED;
      eChange = eChange.or (aExtSMLInfo.setSMLInfo (aNewSMLInfo));
      eChange = eChange.or (aExtSMLInfo.setSMPAPIType (eSMPAPIType));
      eChange = eChange.or (aExtSMLInfo.setSMPIdentifierType (eSMPIdentifierType));
      eChange = eChange.or (aExtSMLInfo.setProduction (bProduction));
      eChange = eChange.or (aExtSMLInfo.setPriority (nPriority));
      if (eChange.isUnchanged ())
        return EChange.UNCHANGED;

      internalUpdateItem (aExtSMLInfo);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
    AuditHelper.onAuditModifySuccess (SMLConfiguration.OT,
                                      "all",
                                      sSMLInfoID,
                                      sDisplayName,
                                      sDNSZone,
                                      sManagementServiceURL,
                                      sURLSuffixManageSMP,
                                      sURLSuffixManageParticipant,
                                      Boolean.valueOf (bClientCertificateRequired),
                                      eSMPAPIType,
                                      eSMPIdentifierType,
                                      Boolean.valueOf (bProduction),
                                      Integer.valueOf (nPriority));
    return EChange.CHANGED;
  }

  @Nullable
  public EChange removeSMLConfiguration (@Nullable final String sSMLInfoID)
  {
    if (StringHelper.isEmpty (sSMLInfoID))
      return EChange.UNCHANGED;

    m_aRWLock.writeLock ().lock ();
    try
    {
      final SMLConfiguration aExtSMLInfo = internalDeleteItem (sSMLInfoID);
      if (aExtSMLInfo == null)
      {
        AuditHelper.onAuditDeleteFailure (SMLConfiguration.OT, "no-such-id", sSMLInfoID);
        return EChange.UNCHANGED;
      }
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
    AuditHelper.onAuditDeleteSuccess (SMLConfiguration.OT, sSMLInfoID);
    return EChange.CHANGED;
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <ISMLConfiguration> getAllSorted ()
  {
    return getAll ().getSortedInline ( (c1, c2) -> {
      // Higher priority before lower
      int ret = c2.getPriority () - c1.getPriority ();
      if (ret == 0)
      {
        // Production before test
        final int nProd1 = c1.isProduction () ? -1 : +1;
        final int nProd2 = c2.isProduction () ? -1 : +1;
        ret = nProd1 - nProd2;
        if (ret == 0)
        {
          // to be deterministic
          ret = c1.getID ().compareTo (c2.getID ());
        }
      }
      return ret;
    });
  }

  @Nullable
  public ISMLConfiguration getSMLConfigurationfID (@Nullable final String sID)
  {
    return getOfID (sID);
  }

  public boolean containsSMLConfigurationWithID (@Nullable final String sID)
  {
    return containsWithID (sID);
  }
}
