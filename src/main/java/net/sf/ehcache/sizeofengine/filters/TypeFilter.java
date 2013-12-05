package net.sf.ehcache.sizeofengine.filters;

import net.sf.ehcache.pool.sizeof.filter.SizeOfFilter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alex Snaps
 */
public class TypeFilter implements SizeOfFilter {

  private final ConcurrentHashMap<Class, Object> classesIgnored = new ConcurrentHashMap<Class, Object>();
  private final ConcurrentHashMap<Class, Object> superClasses = new ConcurrentHashMap<Class, Object>();
  private final ConcurrentHashMap<Class, ConcurrentMap<Field, Object>> fieldsIgnored = new ConcurrentHashMap<Class, ConcurrentMap<Field, Object>>();

  @Override
  public Collection<Field> filterFields(final Class<?> klazz, final Collection<Field> fields) {
    final ConcurrentMap<Field, Object> fieldsToIgnore = fieldsIgnored.get(klazz);
    if (fieldsToIgnore != null) {
      for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); ) {
        if (fieldsToIgnore.containsKey(iterator.next())) {
          iterator.remove();
        }
      }
    }
    return fields;
  }

  @Override
  public boolean filterClass(final Class<?> klazz) {
    if (!classesIgnored.containsKey(klazz)) {
      for (Class aClass : superClasses.keySet()) {
        if (aClass.isAssignableFrom(klazz)) {
          classesIgnored.put(klazz, this);
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public void addClass(final Class<?> classToFilterOut, final boolean strict) {
    if (!strict) {
      superClasses.putIfAbsent(classToFilterOut, this);
    } else {
      classesIgnored.put(classToFilterOut, this);
    }
  }

  public void addField(final Field fieldToFilterOut) {
    final Class<?> klazz = fieldToFilterOut.getDeclaringClass();
    ConcurrentMap<Field, Object> fields = fieldsIgnored.get(klazz);
    if (fields == null) {
      fields = new ConcurrentHashMap<Field, Object>();
      final ConcurrentMap<Field, Object> previous = fieldsIgnored.putIfAbsent(klazz, fields);
      if (previous != null) {
        fields = previous;
      }
    }
    fields.put(fieldToFilterOut, this);
  }
}
