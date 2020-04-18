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
package org.apache.uima.fit.util;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.apache.uima.fit.util.CasUtil.getAnnotationType;
import static org.apache.uima.fit.util.CasUtil.getType;
import static org.apache.uima.fit.util.CasUtil.iterator;
import static org.apache.uima.fit.util.CasUtil.iteratorFS;
import static org.apache.uima.fit.util.CasUtil.select;
import static org.apache.uima.fit.util.CasUtil.selectByIndex;
import static org.apache.uima.fit.util.CasUtil.selectFS;
import static org.apache.uima.fit.util.CasUtil.toText;
import static org.apache.uima.fit.util.CasUtil.exists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.ComponentTestBase;
import org.apache.uima.fit.type.Token;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

/**
 * Test cases for {@link JCasUtil}.
 * 
 */
public class CasUtilTest extends ComponentTestBase {
  @Test
  public void testGetType() {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();

    assertEquals(Token.class.getName(), getType(cas, Token.class.getName()).getName());
    assertEquals(Token.class.getName(), getType(cas, Token.class).getName());
    assertEquals(Token.class.getName(), getAnnotationType(cas, Token.class.getName()).getName());
    assertEquals(Token.class.getName(), getAnnotationType(cas, Token.class).getName());
    assertEquals("uima.cas.TOP", getType(cas, FeatureStructure.class).getName());
    assertEquals("uima.cas.TOP", getType(cas, TOP.class).getName());
    assertEquals("uima.cas.TOP", getType(cas, TOP.class.getName()).getName());
    assertEquals("uima.tcas.Annotation", getType(cas, AnnotationFS.class).getName());
    assertEquals("uima.tcas.Annotation", getType(cas, Annotation.class).getName());
    assertEquals("uima.tcas.Annotation", getType(cas, Annotation.class.getName()).getName());
    assertEquals("uima.tcas.Annotation", getAnnotationType(cas, Annotation.class).getName());
    assertEquals("uima.tcas.Annotation", getAnnotationType(cas, Annotation.class.getName())
            .getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNonExistingType() {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();

    getType(cas, Token.class.getName() + "_dummy");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNonAnnotationType() {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();

    getAnnotationType(cas, TOP.class);
  }

  @Test
  public void testSelectByIndex() {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();
    Type type = JCasUtil.getType(jCas, Token.class);

    assertEquals("dew?", selectByIndex(cas, type, -1).getCoveredText());
    assertEquals("dew?", selectByIndex(cas, type, 3).getCoveredText());
    assertEquals("Rot", selectByIndex(cas, type, 0).getCoveredText());
    assertEquals("Rot", selectByIndex(cas, type, -4).getCoveredText());
    assertNull(selectByIndex(cas, type, -5));
    assertNull(selectByIndex(cas, type, 4));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testSelectOnAnnotations() throws Exception {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();

    assertEquals(asList("Rot", "wood", "cheeses", "dew?"),
            toText(select(cas, getType(cas, Token.class.getName()))));

    assertEquals(
            asList("Rot", "wood", "cheeses", "dew?"),
            toText((Collection<AnnotationFS>) (Collection) selectFS(cas,
                    getType(cas, Token.class.getName()))));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testSelectOnArrays() throws Exception {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();

    Collection<FeatureStructure> allFS = selectFS(cas, getType(cas, TOP.class.getName()));
    ArrayFS allFSArray = cas.createArrayFS(allFS.size());
    int i = 0;
    for (FeatureStructure fs : allFS) {
      allFSArray.set(i, fs);
      i++;
    }

    // Print what is expected
//    for (FeatureStructure fs : allFS) {
//      System.out.println("Type: " + fs.getType().getName() + "]");
//    }
//    System.out
//            .println("Tokens: [" + toText(select(cas, getType(cas, Token.class.getName()))) + "]");

    // Document Annotation, one sentence and 4 tokens.
    assertEquals(6, allFS.size());

    assertEquals(toText(select(cas, getType(cas, Token.class.getName()))),
            toText(select(allFSArray, getType(cas, Token.class.getName()))));

    assertEquals(toText((Iterable) selectFS(cas, getType(cas, Token.class.getName()))),
            toText((Iterable) selectFS(allFSArray, getType(cas, Token.class.getName()))));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testIterator() throws Exception {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();

    assertEquals(asList("Rot", "wood", "cheeses", "dew?"),
            toText(iterator(cas, getType(cas, Token.class))));

    assertEquals(asList("Rot", "wood", "cheeses", "dew?"),
            toText((Iterator<AnnotationFS>) (Iterator) iteratorFS(cas, getType(cas, Token.class))));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testIterate() throws Exception {
    String text = "Rot wood cheeses dew?";
    tokenBuilder.buildTokens(jCas, text);

    CAS cas = jCas.getCas();

    assertEquals(asList("Rot", "wood", "cheeses", "dew?"),
            toText(select(cas, getType(cas, Token.class))));

    assertEquals(asList("Rot", "wood", "cheeses", "dew?"),
            toText((Iterable<AnnotationFS>) (Iterable) selectFS(cas, getType(cas, Token.class))));
  }
  
  @Test
  public void testExists() throws UIMAException {
    CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null);

    Type tokenType = CasUtil.getAnnotationType(cas, Token.class);
    
    assertFalse(exists(cas, tokenType));

    cas.addFsToIndexes(cas.createAnnotation(tokenType, 0, 1));

    assertTrue(exists(cas, tokenType));
  }
}
