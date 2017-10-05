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
    assertThat(configuration.getFilters()[0], CoreMatchers.sameInstance(filter));
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
