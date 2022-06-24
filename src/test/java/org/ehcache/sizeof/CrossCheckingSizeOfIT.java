/**
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ehcache.sizeof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import org.junit.Test;

/**
 *
 * @author cdennis
 */
public class CrossCheckingSizeOfIT {

  private static final Comparator<Class<?>> COMPARATOR = Comparator.comparing(Class::getName);
  
  private static final Collection<Class<?>> FIELD_TYPES = Arrays.asList(
          Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Object.class);

  @Test
  public void testSingleClass() throws Exception {
    CrossCheckingSizeOf sizeOf = new CrossCheckingSizeOf();
    for (int i = 0; i <= 9; i++) {
      for (List<Class<?>> testcase : permutations(FIELD_TYPES, COMPARATOR, i)) {
        sizeOf.sizeOf(generateClassHierarchy(Collections.singletonList(testcase)).newInstance());
      }
    }
  }

  @Test
  public void testTwoClasses() throws Exception {
    CrossCheckingSizeOf sizeOf = new CrossCheckingSizeOf();
    for (int i = 0; i <= 5; i++) {
      for (int j = 0; j <= 5; j++) {
        for (List<Class<?>> superklazz : permutations(FIELD_TYPES, COMPARATOR, i)) {
          for (List<Class<?>> klazz : permutations(FIELD_TYPES, COMPARATOR, j)) {
            sizeOf.sizeOf(generateClassHierarchy(Arrays.asList(superklazz, klazz)).newInstance());
          }
        }
      }
    }
  }
  
  private static <T> Set<List<T>> permutations(Collection<T> choices, Comparator<? super T> comparator, int count) {
    if (count < 0) {
      throw new IllegalArgumentException();
    } else if (count == 0) {
      return Collections.singleton(Collections.<T>emptyList());
    } else {
      Set<List<T>> subperms = permutations(choices, comparator, count -1);
      Set<List<T>> perms = new HashSet<>(subperms.size() * choices.size());
      for (List<T> sub : subperms) {
        for (T element : choices) {
          List<T> p = new ArrayList<>(sub.size() + 1);
          p.addAll(sub);
          p.add(element);
          p.sort(comparator);
          perms.add(p);
        }
      }
      return perms;
    }
  }
  
  private static Class<?> generateClassHierarchy(List<List<Class<?>>> classes) {
    ByteBuddy byteBuddy = new ByteBuddy();

    Class<?> type = Object.class;
    int classIndex = 0;

    for (List<Class<?>> fields : classes) {
      DynamicType.Builder<?> builder = byteBuddy.subclass(type).name("A" + classIndex++);

      int fieldIndex = 0;
      for (Class<?> field : fields) {
        builder = builder.defineField("a" + fieldIndex++, field);
      }
      type = builder.make().load(type.getClassLoader()).getLoaded();
    }
    
    return type;
  }
}
