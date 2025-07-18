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
import javax.annotation.concurrent.Immutable;

import com.helger.commons.string.StringHelper;

/**
 * Key value pair
 *
 * @author Philip Helger
 */
@Immutable
public final class KVPair
{
  private final String m_sKey;
  private final String m_sValue;
  private final int m_nIndent;

  public KVPair (@Nonnull final String sKey, @Nullable final String sValue)
  {
    this (sKey, sValue, 0);
  }

  public KVPair (@Nonnull final String sKey, @Nullable final String sValue, final int nIndent)
  {
    m_sKey = sKey;
    m_sValue = sValue;
    m_nIndent = nIndent;
  }

  @Nonnull
  public String getKey ()
  {
    return m_sKey;
  }

  public boolean hasValue ()
  {
    return StringHelper.hasText (m_sValue);
  }

  @Nullable
  public String getValue ()
  {
    return m_sValue;
  }

  public int getIndent ()
  {
    return m_nIndent;
  }
}
