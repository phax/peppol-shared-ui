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

import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.state.EChange;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.sml.SMLInfo;
import com.helger.peppolid.factory.ESMPIdentifierType;

public final class SMLConfiguration implements ISMLConfiguration
{
  private SMLInfo m_aSMLInfo;
  private ESMPAPIType m_eSMPAPIType;
  private ESMPIdentifierType m_eSMPIdentifierType;
  private boolean m_bProduction;
  private int m_nPriority;

  public SMLConfiguration (@NonNull final SMLInfo aSMLInfo,
                           @NonNull final ESMPAPIType eSMPAPIType,
                           @NonNull final ESMPIdentifierType eSMPIdentifierType,
                           final boolean bProduction,
                           final int nPriority)
  {
    setSMLInfo (aSMLInfo);
    setSMPAPIType (eSMPAPIType);
    setSMPIdentifierType (eSMPIdentifierType);
    setProduction (bProduction);
    setPriority (nPriority);
  }

  @NonNull
  @ReturnsMutableObject
  public SMLInfo getSMLInfo ()
  {
    return m_aSMLInfo;
  }

  @NonNull
  public EChange setSMLInfo (@NonNull final SMLInfo aSMLInfo)
  {
    ValueEnforcer.notNull (aSMLInfo, "SMLInfo");
    if (aSMLInfo.equals (m_aSMLInfo))
      return EChange.UNCHANGED;
    m_aSMLInfo = aSMLInfo;
    return EChange.CHANGED;
  }

  @NonNull
  public ESMPAPIType getSMPAPIType ()
  {
    return m_eSMPAPIType;
  }

  @NonNull
  public EChange setSMPAPIType (@NonNull final ESMPAPIType eSMPAPIType)
  {
    ValueEnforcer.notNull (eSMPAPIType, "SMPAPIType");
    if (eSMPAPIType.equals (m_eSMPAPIType))
      return EChange.UNCHANGED;
    m_eSMPAPIType = eSMPAPIType;
    return EChange.CHANGED;
  }

  @NonNull
  public ESMPIdentifierType getSMPIdentifierType ()
  {
    return m_eSMPIdentifierType;
  }

  @NonNull
  public EChange setSMPIdentifierType (@NonNull final ESMPIdentifierType eSMPIdentifierType)
  {
    ValueEnforcer.notNull (eSMPIdentifierType, "SMPIdentifierType");
    if (eSMPIdentifierType.equals (m_eSMPIdentifierType))
      return EChange.UNCHANGED;
    m_eSMPIdentifierType = eSMPIdentifierType;
    return EChange.CHANGED;
  }

  public boolean isProduction ()
  {
    return m_bProduction;
  }

  @NonNull
  public EChange setProduction (final boolean bProduction)
  {
    if (bProduction == m_bProduction)
      return EChange.UNCHANGED;
    m_bProduction = bProduction;
    return EChange.CHANGED;
  }

  public int getPriority ()
  {
    return m_nPriority;
  }

  @NonNull
  public EChange setPriority (final int nPriority)
  {
    if (nPriority == m_nPriority)
      return EChange.UNCHANGED;
    m_nPriority = nPriority;
    return EChange.CHANGED;
  }

  @NonNull
  public static SMLConfiguration createForPeppol (@NonNull final ESML eSML)
  {
    final boolean bIsProd = eSML == ESML.DIGIT_PRODUCTION;
    return new SMLConfiguration (SMLInfo.builder (eSML).build (),
                                 ESMPAPIType.PEPPOL,
                                 ESMPIdentifierType.PEPPOL,
                                 bIsProd,
                                 bIsProd ? 200 : 100);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final SMLConfiguration rhs = (SMLConfiguration) o;
    return getID ().equals (rhs.getID ());
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (getID ()).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("SMLInfo", m_aSMLInfo)
                                       .append ("APIType", m_eSMPAPIType)
                                       .append ("IdentifierType", m_eSMPIdentifierType)
                                       .append ("Production", m_bProduction)
                                       .append ("Priority", m_nPriority)
                                       .getToString ();
  }
}
