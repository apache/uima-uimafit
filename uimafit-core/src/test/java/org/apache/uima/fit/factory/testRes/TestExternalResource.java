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
package org.apache.uima.fit.factory.testRes;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

/**
 */
public class TestExternalResource extends Resource_ImplBase {
  public static final String EXPECTED_VALUE = "expected value";

  public final static String PARAM_VALUE = "value";

  @ConfigurationParameter(name = PARAM_VALUE)
  private String value;

  public void assertConfiguredOk() {
    // System.out.println(getClass().getSimpleName() + ".assertConfiguredOk()");
    // Ensure normal parameters get passed to External Resource
    assertThat(value).isEqualTo(EXPECTED_VALUE);
  }
}