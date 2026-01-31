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
package com.helger.peppol.ui.types.smlconfig;

import org.jspecify.annotations.NonNull;

import com.helger.base.id.IHasID;
import com.helger.base.name.IHasDisplayName;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppolid.factory.ESMPIdentifierType;

public interface ISMLConfiguration extends IHasID <String>, IHasDisplayName
{
  int PRIO_MINIMUM = Integer.MIN_VALUE;
  int PRIO_DEFAULT = 0;
  int PRIO_MAXIMIM = Integer.MAX_VALUE;

  /**
   * @return The SML it's all about.
   */
  @NonNull
  ISMLInfo getSMLInfo ();


  default String getID ()
  {
    return getSMLInfo ().getID ();
  }

  default String getDisplayName ()
  {
    return getSMLInfo ().getDisplayName ();
  }

  /**
   * @return The API type to use.
   */
  @NonNull
  ESMPAPIType getSMPAPIType ();

  /**
   * @return The SMP identifier type to use.
   */
  @NonNull
  ESMPIdentifierType getSMPIdentifierType ();

  /**
   * @return <code>true</code> if this is a production configuration, <code>false</code> if not
   */
  boolean isProduction ();

  /**
   * @return The priority of this SML configuration in case of "auto detection". The higher the
   *         value, the higher the priority.
   */
  int getPriority ();
}
