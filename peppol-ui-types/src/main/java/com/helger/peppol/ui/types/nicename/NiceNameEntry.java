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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.string.StringHelper;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.CollectionHelper;
import com.helger.collection.commons.ICommonsList;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.peppol.EPeppolCodeListItemState;

public final class NiceNameEntry
{
  private final String m_sName;
  private final EPeppolCodeListItemState m_eState;
  private final ICommonsList <IProcessIdentifier> m_aProcIDs;
  private final String m_sSpecialLabel;

  public NiceNameEntry (@NonNull @Nonempty final String sName,
                        @NonNull final EPeppolCodeListItemState eState,
                        @Nullable final ICommonsList <IProcessIdentifier> aProcIDs)
  {
    this (sName, eState, aProcIDs, null);
  }

  public NiceNameEntry (@NonNull @Nonempty final String sName,
                        @NonNull final EPeppolCodeListItemState eState,
                        @Nullable final ICommonsList <IProcessIdentifier> aProcIDs,
                        @Nullable final String sSpecialLabel)
  {
    m_sName = sName;
    m_eState = eState;
    m_aProcIDs = aProcIDs;
    m_sSpecialLabel = sSpecialLabel;
  }

  @NonNull
  @Nonempty
  public String getName ()
  {
    return m_sName;
  }

  @NonNull
  public EPeppolCodeListItemState getState ()
  {
    return m_eState;
  }

  public boolean hasProcessIDs ()
  {
    return CollectionHelper.isNotEmpty (m_aProcIDs);
  }

  public boolean containsProcessID (@NonNull final IProcessIdentifier aProcID)
  {
    return m_aProcIDs != null && m_aProcIDs.containsAny (aProcID::hasSameContent);
  }

  @Nullable
  public ICommonsList <IProcessIdentifier> getAllProcIDs ()
  {
    return m_aProcIDs == null ? null : m_aProcIDs.getClone ();
  }

  @Nullable
  public String getSpecialLabel ()
  {
    return m_sSpecialLabel;
  }

  public boolean hasSpecialLabel ()
  {
    return StringHelper.isNotEmpty (m_sSpecialLabel);
  }

  @NonNull
  public NiceNameEntry withNewName (@NonNull @Nonempty final String sNewName)
  {
    return new NiceNameEntry (sNewName, m_eState, m_aProcIDs, m_sSpecialLabel);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final NiceNameEntry rhs = (NiceNameEntry) o;
    return m_sName.equals (rhs.m_sName) && m_eState == rhs.m_eState;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sName).append (m_eState).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Name", m_sName)
                                       .append ("Deprecated", m_eState)
                                       .append ("ProcessIDs", m_aProcIDs)
                                       .appendIfNotNull ("SpecialLabel", m_sSpecialLabel)
                                       .getToString ();
  }
}
