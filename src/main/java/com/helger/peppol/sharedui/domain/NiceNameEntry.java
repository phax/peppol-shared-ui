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
package com.helger.peppol.sharedui.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.peppol.EPeppolCodeListItemState;

public final class NiceNameEntry
{
  private final String m_sName;
  private final EPeppolCodeListItemState m_eState;
  private final ICommonsList <IProcessIdentifier> m_aProcIDs;
  private final String m_sWarning;

  public NiceNameEntry (@Nonnull @Nonempty final String sName,
                        @Nonnull final EPeppolCodeListItemState eState,
                        @Nullable final ICommonsList <IProcessIdentifier> aProcIDs)
  {
    this (sName, eState, aProcIDs, null);
  }

  public NiceNameEntry (@Nonnull @Nonempty final String sName,
                        @Nonnull final EPeppolCodeListItemState eState,
                        @Nullable final ICommonsList <IProcessIdentifier> aProcIDs,
                        @Nullable final String sWarning)
  {
    m_sName = sName;
    m_eState = eState;
    m_aProcIDs = aProcIDs;
    m_sWarning = sWarning;
  }

  @Nonnull
  @Nonempty
  public String getName ()
  {
    return m_sName;
  }

  @Nonnull
  public EPeppolCodeListItemState getState ()
  {
    return m_eState;
  }

  public boolean hasProcessIDs ()
  {
    return CollectionHelper.isNotEmpty (m_aProcIDs);
  }

  public boolean containsProcessID (@Nonnull final IProcessIdentifier aProcID)
  {
    return m_aProcIDs != null && m_aProcIDs.containsAny (aProcID::hasSameContent);
  }

  @Nullable
  public ICommonsList <IProcessIdentifier> getAllProcIDs ()
  {
    return m_aProcIDs == null ? null : m_aProcIDs.getClone ();
  }

  @Nullable
  public String getWarning ()
  {
    return m_sWarning;
  }

  @Nonnull
  public NiceNameEntry withNewName (@Nonnull @Nonempty final String sNewName)
  {
    return new NiceNameEntry (sNewName, m_eState, m_aProcIDs, m_sWarning);
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
                                       .appendIfNotNull ("Warning", m_sWarning)
                                       .getToString ();
  }
}
