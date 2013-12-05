package net.sf.ehcache.sizeofengine;

import net.sf.ehcache.pool.sizeof.filter.AnnotationSizeOfFilter;
import net.sf.ehcache.pool.sizeof.filter.SizeOfFilter;
import net.sf.ehcache.sizeofengine.filters.TypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alex Snaps
 */
public final class EhcacheFilterSource implements Filter {

  private final CopyOnWriteArrayList<SizeOfFilter> filters = new CopyOnWriteArrayList<SizeOfFilter>();
  private final TypeFilter typeFilter = new TypeFilter();

  public EhcacheFilterSource(boolean registerAnnotationFilter) {
    filters.add(typeFilter);
    if (registerAnnotationFilter) {
      filters.add(new AnnotationSizeOfFilter());
    }
    applyMutators();
  }

  private void applyMutators() {
    applyMutators(EhcacheFilterSource.class.getClassLoader());
  }

  void applyMutators(final ClassLoader classLoader) {
    final ServiceLoader<FilterConfigurator> loader = ServiceLoader.load(FilterConfigurator.class, classLoader);
    for (FilterConfigurator filterConfigurator : loader) {
      filterConfigurator.configure(this);
    }
  }

  public SizeOfFilter[] getFilters() {
    List<SizeOfFilter> allFilters = new ArrayList<SizeOfFilter>(filters);
    return allFilters.toArray(new SizeOfFilter[allFilters.size()]);
  }

  public void ignoreInstancesOf(final Class clazz, final boolean strict) {
    typeFilter.addClass(clazz, Modifier.isFinal(clazz.getModifiers()) || strict);
  }

  public void ignoreField(final Field field) {
    typeFilter.addField(field);
  }

}
