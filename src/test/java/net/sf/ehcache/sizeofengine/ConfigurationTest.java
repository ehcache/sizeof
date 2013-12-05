package net.sf.ehcache.sizeofengine;

import net.sf.ehcache.pool.sizeof.filter.AnnotationSizeOfFilter;
import net.sf.ehcache.pool.sizeof.filter.SizeOfFilter;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Snaps
 */
public class ConfigurationTest {

  @Test
  public void testBuilderBuilds() {
    final AnnotationSizeOfFilter filter = new AnnotationSizeOfFilter();
    final Configuration configuration = new Configuration.Builder().abort(true)
        .maxDepth(666)
        .silent(true)
        .addFilter(filter)
        .build();
    assertThat(configuration.getFilters().length, is(1));
    assertThat(configuration.getFilters()[0], CoreMatchers.<SizeOfFilter>sameInstance(filter));
    assertThat(configuration.isAbort(), is(true));
    assertThat(configuration.isSilent(), is(true));
    assertThat(configuration.getMaxDepth(), is(666));
  }

  @Test
  public void testBuilderCopies() {
    final AnnotationSizeOfFilter filter = new AnnotationSizeOfFilter();
    final Configuration template = new Configuration.Builder().abort(true)
        .maxDepth(666)
        .silent(true)
        .addFilter(filter)
        .build();
    final Configuration cfg = new Configuration.Builder(template).build();
    assertThat(cfg.getMaxDepth(), is(template.getMaxDepth()));
    assertThat(cfg.isSilent(), is(template.isSilent()));
    assertThat(cfg.isAbort(), is(template.isAbort()));
    assertThat(cfg.getFilters()[0], sameInstance(template.getFilters()[0]));
  }

}
