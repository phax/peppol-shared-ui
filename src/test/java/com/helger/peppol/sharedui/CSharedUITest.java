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
package com.helger.peppol.sharedui;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.regex.RegExHelper;

public final class CSharedUITest
{
  @Test
  public void testSMPIDPattern ()
  {
    for (final String s : new String [] { "1000000066",
                                          "2000000042",
                                          "b2brouter",
                                          "BRZ",
                                          "BRZ-TEST-SMP",
                                          "IBM.ECSCTEST",
                                          "smp.difi.no",
                                          "SMP123" })
      assertTrue (s + " failed!", RegExHelper.stringMatchesPattern (CSharedUI.PATTERN_SMP_ID, s));
  }
}
