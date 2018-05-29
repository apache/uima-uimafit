/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.fit.factory;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createResourceDependencies;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.ComponentTestBase;
import org.apache.uima.fit.component.initialize.ExternalResourceInitializer;
import org.apache.uima.fit.factory.testAes.ParameterizedAE2;
import org.apache.uima.resource.ExternalResourceDependency;
import org.junit.Test;

/**
 * Test the {@link ExternalResourceInitializer}.
 * 
 */
public class ExternalResourceConfiguratorTest extends ComponentTestBase {
  @Test
  public void testAnalyze() throws Exception {
    ExternalResourceDependency[] deps = createResourceDependencies(ParameterizedAE2.class);

    verify(deps);
  }

  @Test
  public void testDescriptor() throws Exception {
    AnalysisEngineDescription desc = createEngineDescription(ParameterizedAE2.class,
            typeSystemDescription);
    verify(desc.getExternalResourceDependencies());
  }

  private void verify(ExternalResourceDependency[] depList) {
    Map<String, ExternalResourceDependency> deps = new HashMap<String, ExternalResourceDependency>();
    for (ExternalResourceDependency dep : depList) {
      deps.put(dep.getKey(), dep);
    }
    
    assertEquals(3, deps.size());

    String key = ParameterizedAE2.DummyResource.class.getName();
    String api = ParameterizedAE2.DummyResource.class.getName();
    ExternalResourceDependency d = deps.get(key);
    assertEquals(key, d.getKey());
    assertEquals(api, d.getInterfaceName());
    assertEquals(false, d.isOptional());

    key = ParameterizedAE2.RES_OTHER;
    d = deps.get(key);
    assertEquals(key, d.getKey());
    assertEquals(api, d.getInterfaceName());
    assertEquals(false, d.isOptional());

    key = ParameterizedAE2.RES_OPTIONAL;
    d = deps.get(key);
    assertEquals(key, d.getKey());
    assertEquals(api, d.getInterfaceName());
    assertEquals(true, d.isOptional());
  }
}
