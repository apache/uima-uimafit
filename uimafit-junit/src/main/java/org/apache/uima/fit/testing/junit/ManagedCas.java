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
package org.apache.uima.fit.testing.junit;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;
import static org.apache.uima.fit.factory.CasFactory.createCas;

import java.util.Set;
import java.util.WeakHashMap;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Provides a {@link CAS} object which is automatically reset before the test.
 */
public final class ManagedCas
    extends TestWatcher
{
    private final ThreadLocal<CAS> casHolder;
    
    private final static Set<CAS> managedCases = synchronizedSet(newSetFromMap(new WeakHashMap<>()));

    /**
     * Provides a CAS with an auto-detected type system.
     */
    public ManagedCas()
    {
        casHolder = ThreadLocal.withInitial(() -> {
            try {
                CAS cas = createCas();
                managedCases.add(cas);
                return cas;
            }
            catch (UIMAException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Provides a CAS with the specified type system.
     * 
     * @param aTypeSystemDescription
     *            the type system used to initialize the CAS.
     */
    public ManagedCas(TypeSystemDescription aTypeSystemDescription)
    {
        casHolder = ThreadLocal.withInitial(() -> {
            try {
                CAS cas = createCas(aTypeSystemDescription);
                managedCases.add(cas);
                return cas;
            }
            catch (UIMAException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * @return the CAS object managed by this rule.
     */
    public CAS get()
    {
        return casHolder.get();
    }

    @Override
    protected void starting(Description description)
    {
        managedCases.forEach(cas -> cas.reset());
    }
}