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
package com.helger.peppol.sharedui.config;

import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.peppol.sharedui.domain.SMLConfiguration;
import com.helger.peppol.sharedui.domain.SMLConfigurationMicroTypeConverter;
import com.helger.xml.microdom.convert.IMicroTypeConverterRegistrarSPI;
import com.helger.xml.microdom.convert.IMicroTypeConverterRegistry;

/**
 * SPI implementation to register all micro type converters of this application.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public final class SharedUIMicroTypeConverterRegistarSPI implements IMicroTypeConverterRegistrarSPI
{
  public void registerMicroTypeConverter (final IMicroTypeConverterRegistry aRegistry)
  {
    aRegistry.registerMicroElementTypeConverter (SMLConfiguration.class, new SMLConfigurationMicroTypeConverter ());
  }
}
