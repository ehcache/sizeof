package net.sf.ehcache.sizeofengine;

import net.sf.ehcache.pool.SizeOfEngineLoader;
import net.sf.ehcache.pool.sizeof.filter.AnnotationSizeOfFilter;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Enumeration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class EhcacheFilterSourceTest {

  @Test
  public void testContainsAnnotationFilterWhenConfigured() {
    EhcacheFilterSource source = new EhcacheFilterSource(true);
    assertThat(source.getFilters().length, is(2));
    assertThat(source.getFilters()[1], instanceOf(AnnotationSizeOfFilter.class));
  }

  @Test
  public void testStartsEmptyWhenConfigured() {
    EhcacheFilterSource source = new EhcacheFilterSource(false);
    assertThat(source.getFilters().length, is(1));
  }

  @Test
  public void testAppliesMutators() {
    EhcacheFilterSource source = new EhcacheFilterSource(false);
    assertThat(source.getFilters().length, is(1));
    source.applyMutators(new CheatingClassLoader());
    assertThat(source.getFilters().length, is(1));
    assertThat(source.getFilters()[0].filterClass(TestMutator.class), is(false));
    assertThat(source.getFilters()[0].filterClass(String.class), is(true));
  }

  private static class CheatingClassLoader extends SecureClassLoader {

    public CheatingClassLoader() {
      super(SizeOfEngineLoader.class.getClassLoader());
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
      final String className = FilterConfigurator.class.getName();
      if (name.equals("META-INF/services/" + className)) {
        return super.getResources("services/" + className + ".txt");
      }
      return super.getResources(name);
    }
  }

  public static final class TestMutator implements FilterConfigurator {

    @Override
    public void configure(final Filter ehcacheFilter) {
      ehcacheFilter.ignoreInstancesOf(TestMutator.class, true);
    }
  }
}
