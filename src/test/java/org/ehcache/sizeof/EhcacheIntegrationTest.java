package org.ehcache.sizeof;

import net.sf.ehcache.pool.SizeOfEngine;
import net.sf.ehcache.pool.SizeOfEngineLoader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class EhcacheIntegrationTest {

  @Test
  public void testEhcacheUsesOurImplementation() {
    final SizeOfEngine sizeOfEngine = SizeOfEngineLoader.newSizeOfEngine(10, false, false);
    assertThat(sizeOfEngine, instanceOf(EhcacheSizeOfEngine.class));
  }
}
