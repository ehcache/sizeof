package net.sf.ehcache.sizeofengine;

import java.lang.reflect.Field;

/**
 * @author Alex Snaps
 */
public interface Filter {

  void ignoreInstancesOf(final Class clazz, final boolean strict);

  void ignoreField(final Field field);

}
