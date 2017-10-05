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

import org.ehcache.sizeof.filters.AnnotationSizeOfFilter;
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
public class SizeOfFilterSourceTest {

  @Test
  public void testContainsAnnotationFilterWhenConfigured() {
    SizeOfFilterSource source = new SizeOfFilterSource(true);
    assertThat(source.getFilters().length, is(2));
    assertThat(source.getFilters()[1], instanceOf(AnnotationSizeOfFilter.class));
  }

  @Test
  public void testStartsEmptyWhenConfigured() {
    SizeOfFilterSource source = new SizeOfFilterSource(false);
    assertThat(source.getFilters().length, is(1));
  }

  @Test
  public void testAppliesMutators() {
    SizeOfFilterSource source = new SizeOfFilterSource(false);
    assertThat(source.getFilters().length, is(1));
    source.applyMutators(new CheatingClassLoader());
    assertThat(source.getFilters().length, is(1));
    assertThat(source.getFilters()[0].filterClass(TestMutator.class), is(false));
    assertThat(source.getFilters()[0].filterClass(String.class), is(true));
  }

  private static class CheatingClassLoader extends SecureClassLoader {

    public CheatingClassLoader() {
      super(SizeOfFilterSourceTest.class.getClassLoader());
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
