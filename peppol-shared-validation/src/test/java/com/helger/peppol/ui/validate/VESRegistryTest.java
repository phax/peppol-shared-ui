package com.helger.peppol.ui.validate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for class {@link VESRegistry}.
 *
 * @author Philip Helger
 */
public final class VESRegistryTest
{
  @Test
  public void testBasic ()
  {
    assertNotNull (VESRegistry.getAll ());
    assertTrue (VESRegistry.getAllSortedByID ().isNotEmpty ());
  }
}
