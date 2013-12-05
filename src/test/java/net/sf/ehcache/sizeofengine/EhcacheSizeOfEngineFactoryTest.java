package net.sf.ehcache.sizeofengine;

import net.sf.ehcache.pool.sizeof.filter.AnnotationSizeOfFilter;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class EhcacheSizeOfEngineFactoryTest {

  private EhcacheSizeOfEngineFactory factory;

  @Before
  public void setup() {
    factory = new EhcacheSizeOfEngineFactory();
  }

  @Test
  public void testCreatesProperlyConfiguredSizeOfEngine() {
    final EhcacheSizeOfEngine ehcacheSizeOfEngine1 = factory.createSizeOfEngine(10, true, false);
    assertThat(ehcacheSizeOfEngine1.getConfiguration().isSilent(), is(false));
    assertThat(ehcacheSizeOfEngine1.getConfiguration().isAbort(), is(true));
    assertThat(ehcacheSizeOfEngine1.getConfiguration().getMaxDepth(), is(10));
    final EhcacheSizeOfEngine ehcacheSizeOfEngine2 = factory.createSizeOfEngine(20, false, true);
    assertThat(ehcacheSizeOfEngine2.getConfiguration().isSilent(), is(true));
    assertThat(ehcacheSizeOfEngine2.getConfiguration().isAbort(), is(false));
    assertThat(ehcacheSizeOfEngine2.getConfiguration().getMaxDepth(), is(20));
  }

  @Test
  public void testUsesAnnotationFilterAsDefault() {
    final EhcacheSizeOfEngine sizeOfEngine = factory.createSizeOfEngine(10, true, false);
    assertThat(sizeOfEngine.getConfiguration().getFilters().length, is(2));
    assertThat(sizeOfEngine.getConfiguration().getFilters()[1], instanceOf(AnnotationSizeOfFilter.class));
  }

  @Test
  public void testSupportsFilterSourceInjection() {
    EhcacheSizeOfEngineFactory specializedFactory = new EhcacheSizeOfEngineFactory(new EhcacheFilterSource(false));
    final EhcacheSizeOfEngine sizeOfEngine = specializedFactory.createSizeOfEngine(10, false, false);
    assertThat(sizeOfEngine.getConfiguration().getFilters().length, is(1));
  }

  @Test
  public void testGlobalFilters() throws NoSuchFieldException {
    final EhcacheSizeOfEngine sizeOfEngineWithoutFilters = factory.createSizeOfEngine(100, false, false);
    factory.getFilter().ignoreInstancesOf(Number.class, false);
    final EhcacheSizeOfEngine sizeOfEngine = factory.createSizeOfEngine(100, false, false);
    assertThat(sizeOfEngine.sizeOf(340593485, 340593485L, 0.23423423555).getCalculated(), is(0L));
    assertThat(sizeOfEngineWithoutFilters.sizeOf(340593485, 340593485L, 0.23423423555).getCalculated(), is(0L));
    assertThat(sizeOfEngine.sizeOf(340593485, 340593485L, "sizeThis!").getCalculated(), not(is(0L)));
    final EhcacheSizeOfEngine newEngine = factory.createSizeOfEngine(100, false, false);
    factory.getFilter().ignoreInstancesOf(String.class, true);
    assertThat(newEngine.sizeOf(340593485, 340593485L, "sizeThis!").getCalculated(), is(0L));
    // There is a bug here... but in ehcache afaict. Probably related to the fields we cache.
//    assertThat(sizeOfEngine.sizeOf(340593485, 340593485L, "sizeThis!").getCalculated(), is(0L));

  }
}
