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

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineFromPath;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.metadata.SofaMapping;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.ComponentTestBase;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.factory.testAes.Annotator1;
import org.apache.uima.fit.factory.testAes.Annotator2;
import org.apache.uima.fit.factory.testAes.Annotator3;
import org.apache.uima.fit.factory.testAes.Annotator4;
import org.apache.uima.fit.factory.testAes.ParameterizedAE;
import org.apache.uima.fit.factory.testAes.SerializationTestAnnotator;
import org.apache.uima.fit.factory.testAes.ViewNames;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.type.AnalyzedText;
import org.apache.uima.fit.type.Sentence;
import org.apache.uima.fit.type.Token;
import org.apache.uima.fit.util.CasIOUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.pear.tools.PackageBrowser;
import org.apache.uima.pear.tools.PackageInstaller;
import org.apache.uima.resource.PearSpecifier;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.Capability;
import org.apache.uima.resource.metadata.ConfigurationParameter;
import org.apache.uima.resource.metadata.ConfigurationParameterDeclarations;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.FsIndexDescription;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypePriorityList;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.XMLInputSource;
import org.junit.Test;
import org.xmlunit.assertj.XmlAssert;

public class AnalysisEngineFactoryTest extends ComponentTestBase {

  @Test
  public void testViewAE() throws Exception {
    AnalysisEngineDescription aed = AnalysisEngineFactory.createEngineDescription(
            Annotator4.class, typeSystemDescription);
    AnalysisEngine ae = AnalysisEngineFactory.createEngine(aed, "A");

    JCas aView = jCas.createView("A");
    tokenBuilder.buildTokens(aView, "'Verb' is a noun!?");
    ae.process(jCas);
    assertEquals("'Verb' is a noun!?", jCas.getView("A").getDocumentText());
    assertEquals("NN", JCasUtil.selectByIndex(aView, Token.class, 0).getPos());
  }

  @Test
  public void testCreateAnalysisEngineFromPath() throws UIMAException, IOException {
    AnalysisEngine engine = createEngineFromPath(
            "src/main/resources/org/apache/uima/fit/component/NoOpAnnotator.xml");
    assertNotNull(engine);
  }

  @Test
  public void testCreateAnalysisEngineWithPrioritizedTypes() throws UIMAException {
    String[] prioritizedTypeNames = new String[] { "org.apache.uima.fit.type.Token",
        "org.apache.uima.fit.type.Sentence" };
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(
            org.apache.uima.fit.component.NoOpAnnotator.class, typeSystemDescription,
            prioritizedTypeNames, (Object[]) null);

    typePriorities = engine.getAnalysisEngineMetaData().getTypePriorities();
    assertEquals(1, typePriorities.getPriorityLists().length);
    TypePriorityList typePriorityList = typePriorities.getPriorityLists()[0];
    assertEquals(2, typePriorityList.getTypes().length);
    assertEquals("org.apache.uima.fit.type.Token", typePriorityList.getTypes()[0]);
    assertEquals("org.apache.uima.fit.type.Sentence", typePriorityList.getTypes()[1]);

    jCas = engine.newJCas();
    tokenBuilder.buildTokens(jCas, "word");
    FSIterator<Annotation> tokensInSentence = jCas.getAnnotationIndex().subiterator(
            JCasUtil.selectByIndex(jCas, Sentence.class, 0));
    assertFalse(tokensInSentence.hasNext());

    prioritizedTypeNames = new String[] { "org.apache.uima.fit.type.Sentence",
        "org.apache.uima.fit.type.Token" };
    engine = AnalysisEngineFactory.createEngine(
            org.apache.uima.fit.component.NoOpAnnotator.class, typeSystemDescription,
            prioritizedTypeNames, (Object[]) null);
    jCas = engine.newJCas();
    tokenBuilder.buildTokens(jCas, "word");
    tokensInSentence = jCas.getAnnotationIndex().subiterator(
            JCasUtil.selectByIndex(jCas, Sentence.class, 0));
    assertTrue(tokensInSentence.hasNext());

  }

  @Test
  public void testAggregate() throws UIMAException {
    tokenBuilder.buildTokens(jCas, "Anyone up for a game of Foosball?");

    SofaMapping[] sofaMappings = new SofaMapping[] {
        SofaMappingFactory.createSofaMapping(Annotator1.class, ViewNames.PARENTHESES_VIEW, "A"),
        SofaMappingFactory.createSofaMapping(Annotator2.class, ViewNames.SORTED_VIEW, "B"),
        SofaMappingFactory.createSofaMapping(Annotator2.class, ViewNames.SORTED_PARENTHESES_VIEW,
                "C"),
        SofaMappingFactory.createSofaMapping(Annotator2.class, ViewNames.PARENTHESES_VIEW, "A"),
        SofaMappingFactory.createSofaMapping(Annotator3.class, ViewNames.INITIAL_VIEW, "B") };

    List<Class<? extends AnalysisComponent>> primitiveAEClasses = new ArrayList<Class<? extends AnalysisComponent>>();
    primitiveAEClasses.add(Annotator1.class);
    primitiveAEClasses.add(Annotator2.class);
    primitiveAEClasses.add(Annotator3.class);

    AnalysisEngine aggregateEngine = AnalysisEngineFactory.createEngine(primitiveAEClasses,
            typeSystemDescription, null, sofaMappings);

    aggregateEngine.process(jCas);

    assertEquals("Anyone up for a game of Foosball?", jCas.getDocumentText());
    assertEquals("Any(o)n(e) (u)p f(o)r (a) g(a)m(e) (o)f F(oo)sb(a)ll?", jCas.getView("A")
            .getDocumentText());
    assertEquals("?AFaaabeeffgllmnnoooooprsuy", jCas.getView("B").getDocumentText());
    assertEquals("(((((((((())))))))))?AFaaabeeffgllmnnoooooprsuy", jCas.getView("C")
            .getDocumentText());
    assertEquals("yusrpooooonnmllgffeebaaaFA?", jCas.getView(ViewNames.REVERSE_VIEW)
            .getDocumentText());

  }

  @Test
  public void testAggregate2() throws UIMAException, IOException {
    tokenBuilder.buildTokens(jCas, "Anyone up for a game of Foosball?");

    SofaMapping[] sofaMappings = new SofaMapping[] {
        SofaMappingFactory.createSofaMapping("ann1", ViewNames.PARENTHESES_VIEW, "A"),
        SofaMappingFactory.createSofaMapping("ann2", ViewNames.SORTED_VIEW, "B"),
        SofaMappingFactory.createSofaMapping("ann2", ViewNames.SORTED_PARENTHESES_VIEW, "C"),
        SofaMappingFactory.createSofaMapping("ann2", ViewNames.PARENTHESES_VIEW, "A"),
        SofaMappingFactory.createSofaMapping("ann3", ViewNames.INITIAL_VIEW, "B") };

    List<AnalysisEngineDescription> primitiveDescriptors = new ArrayList<AnalysisEngineDescription>();
    primitiveDescriptors.add(AnalysisEngineFactory.createEngineDescription(Annotator1.class,
            typeSystemDescription, (TypePriorities) null));
    primitiveDescriptors.add(AnalysisEngineFactory.createEngineDescription(Annotator2.class,
            typeSystemDescription, (TypePriorities) null));
    primitiveDescriptors.add(AnalysisEngineFactory.createEngineDescription(Annotator3.class,
            typeSystemDescription, (TypePriorities) null));

    List<String> componentNames = Arrays.asList("ann1", "ann2", "ann3");

    AnalysisEngine aggregateEngine = AnalysisEngineFactory.createEngine(primitiveDescriptors,
            componentNames, null, sofaMappings);

    aggregateEngine.process(jCas);

    assertEquals("Anyone up for a game of Foosball?", jCas.getDocumentText());
    assertEquals("Any(o)n(e) (u)p f(o)r (a) g(a)m(e) (o)f F(oo)sb(a)ll?", jCas.getView("A")
            .getDocumentText());
    assertEquals("?AFaaabeeffgllmnnoooooprsuy", jCas.getView("B").getDocumentText());
    assertEquals("(((((((((())))))))))?AFaaabeeffgllmnnoooooprsuy", jCas.getView("C")
            .getDocumentText());
    assertEquals("yusrpooooonnmllgffeebaaaFA?", jCas.getView(ViewNames.REVERSE_VIEW)
            .getDocumentText());

    CasIOUtil.readJCas(jCas, new File("src/test/resources/data/docs/test.xmi"));
    AnalysisEngine ae1 = AnalysisEngineFactory.createEngine(NoOpAnnotator.class,
            typeSystemDescription);

    SimplePipeline.runPipeline(jCas, ae1, aggregateEngine);

  }

  @Test
  public void testReflectPrimitiveDescription() throws ResourceInitializationException {
    AnalysisEngineDescription aed = AnalysisEngineFactory.createEngineDescription(
            Annotator2.class, typeSystemDescription, typePriorities);
    Capability[] capabilities = aed.getAnalysisEngineMetaData().getCapabilities();
    assertEquals(1, capabilities.length);
    String[] inputSofas = capabilities[0].getInputSofas();
    assertArrayEquals(new String[] { CAS.NAME_DEFAULT_SOFA, ViewNames.PARENTHESES_VIEW },
            inputSofas);
    String[] outputSofas = capabilities[0].getOutputSofas();
    assertArrayEquals(new String[] { ViewNames.SORTED_VIEW, ViewNames.SORTED_PARENTHESES_VIEW },
            outputSofas);

    aed = AnalysisEngineFactory.createEngineDescription(ParameterizedAE.class,
            typeSystemDescription, typePriorities);
    capabilities = aed.getAnalysisEngineMetaData().getCapabilities();
    assertEquals(1, capabilities.length);
    inputSofas = capabilities[0].getInputSofas();
    assertArrayEquals(new String[] { CAS.NAME_DEFAULT_SOFA }, inputSofas);
    outputSofas = capabilities[0].getOutputSofas();
    assertArrayEquals(new String[] {}, outputSofas);

    testConfigurationParameter(aed, ParameterizedAE.PARAM_STRING_1,
            ConfigurationParameter.TYPE_STRING, true, false, "pineapple");
    testConfigurationParameter(aed, ParameterizedAE.PARAM_STRING_2,
            ConfigurationParameter.TYPE_STRING, false, true, new String[] { "coconut", "mango" });
    testConfigurationParameter(aed, ParameterizedAE.PARAM_STRING_3,
            ConfigurationParameter.TYPE_STRING, false, false, null);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_STRING_4,
            ConfigurationParameter.TYPE_STRING, true, true, new String[] { "apple" });
    testConfigurationParameter(aed, ParameterizedAE.PARAM_STRING_5,
            ConfigurationParameter.TYPE_STRING, false, true, new String[] { "" });

    testConfigurationParameter(aed, ParameterizedAE.PARAM_BOOLEAN_1,
            ConfigurationParameter.TYPE_BOOLEAN, true, false, Boolean.FALSE);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_BOOLEAN_2,
            ConfigurationParameter.TYPE_BOOLEAN, false, false, null);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_BOOLEAN_3,
            ConfigurationParameter.TYPE_BOOLEAN, true, true, new Boolean[] { true, true, false });
    testConfigurationParameter(aed, ParameterizedAE.PARAM_BOOLEAN_4,
            ConfigurationParameter.TYPE_BOOLEAN, true, true, new Boolean[] { true, false, true });
    testConfigurationParameter(aed, ParameterizedAE.PARAM_BOOLEAN_5,
            ConfigurationParameter.TYPE_BOOLEAN, true, true, new Boolean[] { false });

    testConfigurationParameter(aed, ParameterizedAE.PARAM_INT_1,
            ConfigurationParameter.TYPE_INTEGER, true, false, 0);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_INT_2,
            ConfigurationParameter.TYPE_INTEGER, true, false, 42);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_INT_3,
            ConfigurationParameter.TYPE_INTEGER, true, true, new Integer[] { 42, 111 });
    testConfigurationParameter(aed, ParameterizedAE.PARAM_INT_4,
            ConfigurationParameter.TYPE_INTEGER, true, true, new Integer[] { 2 });

    testConfigurationParameter(aed, ParameterizedAE.PARAM_FLOAT_1,
            ConfigurationParameter.TYPE_FLOAT, true, false, 0.0f);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_FLOAT_2,
            ConfigurationParameter.TYPE_FLOAT, false, false, 3.1415f);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_FLOAT_3,
            ConfigurationParameter.TYPE_FLOAT, true, false, null);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_FLOAT_4,
            ConfigurationParameter.TYPE_FLOAT, false, true, null);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_FLOAT_5,
            ConfigurationParameter.TYPE_FLOAT, false, true,
            new Float[] { 0.0f, 3.1415f, 2.7182818f });
    testConfigurationParameter(aed, ParameterizedAE.PARAM_FLOAT_6,
            ConfigurationParameter.TYPE_FLOAT, true, true, null);
    testConfigurationParameter(aed, ParameterizedAE.PARAM_FLOAT_7,
            ConfigurationParameter.TYPE_FLOAT, true, true, new Float[] { 1.1111f, 2.2222f, 3.333f });

    AnalysisEngine ae = AnalysisEngineFactory
            .createEngine(aed, ParameterizedAE.PARAM_FLOAT_3, 3.1415f,
                    ParameterizedAE.PARAM_FLOAT_6, new Float[] { 2.71828183f }, "file2", "foo/bar");
    Object paramValue = ae.getAnalysisEngineMetaData().getConfigurationParameterSettings()
            .getParameterValue(ParameterizedAE.PARAM_FLOAT_3);
    assertEquals(paramValue, 3.1415f);
    paramValue = ae.getAnalysisEngineMetaData().getConfigurationParameterSettings()
            .getParameterValue(ParameterizedAE.PARAM_FLOAT_6);
    assertEquals(((Float[]) paramValue)[0].floatValue(), 2.71828183f, 0.00001f);

  }

  private void testConfigurationParameter(AnalysisEngineDescription aed, String parameterName,
          String parameterType, boolean mandatory, boolean multiValued, Object parameterValue) {
    ConfigurationParameterDeclarations cpd = aed.getMetaData()
            .getConfigurationParameterDeclarations();
    ConfigurationParameter cp = cpd.getConfigurationParameter(null, parameterName);
    assertNotNull("Parameter [" + parameterName + "] does not exist!", cp);
    assertEquals("Parameter [" + parameterName + "] has wrong name", parameterName, cp.getName());
    assertEquals("Parameter [" + parameterName + "] has wrong type", parameterType, cp.getType());
    assertEquals("Parameter [" + parameterName + "] has wrong mandatory flag", mandatory,
            cp.isMandatory());
    assertEquals("Parameter [" + parameterName + "] has wrong multi-value flag", multiValued,
            cp.isMultiValued());
    ConfigurationParameterSettings cps = aed.getMetaData().getConfigurationParameterSettings();
    Object actualValue = cps.getParameterValue(parameterName);
    if (parameterValue == null) {
      assertNull(actualValue);
    } else if (!multiValued) {
      if (parameterType.equals(ConfigurationParameter.TYPE_FLOAT)) {
        assertEquals(((Float) parameterValue).floatValue(), ((Float) actualValue).floatValue(),
                .001f);
      } else {
        assertEquals(parameterValue, actualValue);
      }
    } else {
      assertEquals(Array.getLength(parameterValue), Array.getLength(actualValue));
      for (int i = 0; i < Array.getLength(parameterValue); ++i) {
        assertEquals(Array.get(parameterValue, i), Array.get(actualValue, i));
      }
    }

  }

  @Test
  public void testPrimitiveDescription() throws ResourceInitializationException {

    AnalysisEngineDescription aed = AnalysisEngineFactory.createEngineDescription(
            NoOpAnnotator.class, typeSystemDescription);
    assertNotNull(aed);
    // assertEquals("org.apache.uima.fit.type.TypeSystem",
    // aed.getAnalysisEngineMetaData().getTypeSystem().getImports()[0].getName());
  }

  /**
   * Test that a {@link OperationalProperties} annotation on an ancestor of a analysis engine class
   * is found and taken into account.
   */
  @Test
  public void testComponentAnnotationOnAncestor() throws Exception {
    AnalysisEngineDescription desc1 = AnalysisEngineFactory.createEngineDescription(
            PristineAnnotatorClass.class, (Object[]) null);
    assertTrue(
            "Multiple deployment should be allowed on " + desc1.getAnnotatorImplementationName(),
            desc1.getAnalysisEngineMetaData().getOperationalProperties()
                    .isMultipleDeploymentAllowed());

    AnalysisEngineDescription desc2 = AnalysisEngineFactory.createEngineDescription(
            UnannotatedAnnotatorClass.class, (Object[]) null);
    assertFalse(
            "Multiple deployment should be prohibited on " + desc2.getAnnotatorImplementationName(),
            desc2.getAnalysisEngineMetaData().getOperationalProperties()
                    .isMultipleDeploymentAllowed());

    AnalysisEngineDescription desc3 = AnalysisEngineFactory.createEngineDescription(
            AnnotatedAnnotatorClass.class, (Object[]) null);
    assertTrue(
            "Multiple deployment should be allowed  on " + desc3.getAnnotatorImplementationName(),
            desc3.getAnalysisEngineMetaData().getOperationalProperties()
                    .isMultipleDeploymentAllowed());
  }

  /*
   * This test case illustrates that UIMA throws an exception unless the multipleDeploymentAllowed
   * flag is properly set to false when mixing multi-deployment and non-multi-deployment AEs.
   */
  @Test(expected = ResourceInitializationException.class)
  public void testAAEMultipleDeploymentPolicyProblem() throws Exception {
    {
      AnalysisEngineDescription desc1 = AnalysisEngineFactory.createEngineDescription(
              PristineAnnotatorClass.class, (Object[]) null);
      assertTrue(
              "Multiple deployment should be allowed on " + desc1.getAnnotatorImplementationName(),
              desc1.getAnalysisEngineMetaData().getOperationalProperties()
                      .isMultipleDeploymentAllowed());

      AnalysisEngineDescription desc2 = AnalysisEngineFactory.createEngineDescription(
              UnannotatedAnnotatorClass.class, (Object[]) null);
      assertFalse(
              "Multiple deployment should be prohibited on "
                      + desc2.getAnnotatorImplementationName(), desc2.getAnalysisEngineMetaData()
                      .getOperationalProperties().isMultipleDeploymentAllowed());

      AnalysisEngineDescription aae = AnalysisEngineFactory
              .createEngineDescription(desc1, desc2);
      aae.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
      UIMAFramework.produceAnalysisEngine(aae);
    }
  }

  @Test
  public void testAAEMultipleDeploymentPolicy() throws Exception {
    {
      AnalysisEngineDescription desc1 = AnalysisEngineFactory.createEngineDescription(
              PristineAnnotatorClass.class, (Object[]) null);
      assertTrue(
              "Multiple deployment should be allowed on " + desc1.getAnnotatorImplementationName(),
              desc1.getAnalysisEngineMetaData().getOperationalProperties()
                      .isMultipleDeploymentAllowed());

      AnalysisEngineDescription desc2 = AnalysisEngineFactory.createEngineDescription(
              UnannotatedAnnotatorClass.class, (Object[]) null);
      assertFalse(
              "Multiple deployment should be prohibited on "
                      + desc2.getAnnotatorImplementationName(), desc2.getAnalysisEngineMetaData()
                      .getOperationalProperties().isMultipleDeploymentAllowed());

      AnalysisEngineDescription aae = AnalysisEngineFactory
              .createEngineDescription(desc1, desc2);
      UIMAFramework.produceAnalysisEngine(aae);

      assertFalse("Multiple deployment should be prohibited on AAE", aae
              .getAnalysisEngineMetaData().getOperationalProperties().isMultipleDeploymentAllowed());
    }

    {
      AnalysisEngineDescription desc1 = AnalysisEngineFactory.createEngineDescription(
              PristineAnnotatorClass.class, (Object[]) null);
      assertTrue(
              "Multiple deployment should be allowed on " + desc1.getAnnotatorImplementationName(),
              desc1.getAnalysisEngineMetaData().getOperationalProperties()
                      .isMultipleDeploymentAllowed());

      AnalysisEngineDescription desc3 = AnalysisEngineFactory.createEngineDescription(
              AnnotatedAnnotatorClass.class, (Object[]) null);
      assertTrue(
              "Multiple deployment should be allowed  on " + desc3.getAnnotatorImplementationName(),
              desc3.getAnalysisEngineMetaData().getOperationalProperties()
                      .isMultipleDeploymentAllowed());

      AnalysisEngineDescription aae = AnalysisEngineFactory
              .createEngineDescription(desc1, desc3);
      UIMAFramework.produceAnalysisEngine(aae);

      assertTrue("Multiple deployment should be prohibited on AAE", aae.getAnalysisEngineMetaData()
              .getOperationalProperties().isMultipleDeploymentAllowed());
    }
  }

  @Test
  public void testResourceMetaData() throws Exception {
    AnalysisEngineDescription desc = AnalysisEngineFactory
            .createEngineDescription(AnnotatorWithMetaDataClass.class);

    org.apache.uima.resource.metadata.ResourceMetaData meta = desc.getMetaData();

    assertEquals("dummy", meta.getName());
    assertEquals("1.0", meta.getVersion());
    assertEquals("Just a dummy", meta.getDescription());
    assertEquals("ASL 2.0", meta.getCopyright());
    assertEquals("uimaFIT", meta.getVendor());
  }

  @ResourceMetaData(name = "dummy", version = "1.0", description = "Just a dummy", copyright = "ASL 2.0", vendor = "uimaFIT")
  public static class AnnotatorWithMetaDataClass extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
      // Dummy
    }
  }

  public static class PristineAnnotatorClass extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
      // Dummy
    }
  }

  @org.apache.uima.fit.descriptor.OperationalProperties(multipleDeploymentAllowed = false)
  public static class AncestorClass extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
      // Dummy
    }
  }

  public static class UnannotatedAnnotatorClass extends AncestorClass {
    // Dummy
  }

  @org.apache.uima.fit.descriptor.OperationalProperties(multipleDeploymentAllowed = true)
  public static class AnnotatedAnnotatorClass extends UnannotatedAnnotatorClass {
    // Vessel for the annotation
  }

  @Test
  public void testIssue5a() throws ResourceInitializationException {
    AnalysisEngineFactory.createEngineDescription(ParameterizedAE.class, typeSystemDescription);
  }

  @Test(expected = ResourceInitializationException.class)
  public void testIssue5b() throws ResourceInitializationException {
    AnalysisEngineFactory.createEngine(ParameterizedAE.class, typeSystemDescription);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnbalancedComponentAndNames() throws ResourceInitializationException {
    List<AnalysisEngineDescription> descriptions = new ArrayList<AnalysisEngineDescription>();
    descriptions.add(AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class));
    descriptions.add(AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class));
    List<String> names = new ArrayList<String>();

    createEngineDescription(descriptions, names, null, null, null);
  }

  /**
   * Configuring new types on an aggregate is not allowed, but configuring new priorities is
   * allowed. Just testing if it actually works.
   * 
   * @see <a href="https://issues.apache.org/jira/browse/UIMA-2891">UIMA-2891</a>
   */
  @Test
  public void testExtraTypeConfigsOnAggregate() throws Exception {
    TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescription();
    TypePriorities extraPrios = TypePrioritiesFactory.createTypePriorities(Sentence.class,
            Token.class);

    // This one doesn't use any auto-configuration
    AnalysisEngineDescription ae = createEngineDescription(UnannotatedAnnotatorClass.class,
            typeSystem, null, null, null, null, null, null);

    // Try configuring priorities on the aggregate
    AnalysisEngineDescription aae = createEngineDescription(asList(ae), asList("ae1"),
            extraPrios, null, null);

    AnalysisEngine engine = createEngine(aae);
    ProcessingResourceMetaData meta = engine.getProcessingResourceMetaData();
    
    // When the meta data from the ae and the aae are merged in the engine, then it should be a 
    // new instance.
    assertFalse("Merged meta-data is same instance as original",
            aae.getMetaData().hashCode() == meta.hashCode());
    assertFalse("Merged meta-data is same instance as original",
            ae.getMetaData().hashCode() == meta.hashCode());
    
    // Check that the priorities arrived
    TypePriorities expected = ((ProcessingResourceMetaData) aae.getMetaData()).getTypePriorities();
    TypePriorities actual = meta.getTypePriorities();
    assertArrayEquals(expected.getPriorityLists()[0].getTypes(),
            actual.getPriorityLists()[0].getTypes());
  }
  
  @Test
  public void serializeComponent() throws Exception {
    File reference = new File("src/test/resources/data/reference/SerializationTestAnnotator.xml");
    
    File target = new File("target/test-output/AnalysisEngineFactoryTest/SerializationTestAnnotator.xml");
    target.getParentFile().mkdirs();
    
    AnalysisEngineDescription desc = createEngineDescription(SerializationTestAnnotator.class);
    try (OutputStream os = new FileOutputStream(target)) {
      desc.toXML(os);
    }
    
    String actual = FileUtils.readFileToString(target, "UTF-8");
    String expected = FileUtils.readFileToString(reference, "UTF-8");
    XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().areIdentical();
  }
  
  @Test
  public void testPear() throws Exception {
    // Install PEAR package
    PackageBrowser instPear = PackageInstaller.installPackage(
            new File("target/test-output/AnalysisEngineFactoryTest/testPear"), 
            new File("src/test/resources/pear/DateTime.pear"), true);

    // Create analysis engine from the installed PEAR package
    XMLInputSource in = new XMLInputSource(instPear.getComponentPearDescPath());
    PearSpecifier specifier = UIMAFramework.getXMLParser().parsePearSpecifier(in);
    
    AnalysisEngine ae = createEngine(createEngineDescription(specifier));
    
    // Create a CAS with a sample document text and process the CAS   
    CAS cas = ae.newCAS();
    cas.setDocumentText("Sample text to process with a date 05/29/07 and a time 9:45 AM");
    cas.setDocumentLanguage("en");
    ae.process(cas);
  }
  
  @Test
  public void thatCreateEngineDescriptorAutoDetectionWorks() throws Exception
  {
    AnalysisEngineDescription aed = createEngineDescription(NoOpAnnotator.class);
    
    TypeSystemDescription tsd = createTypeSystemDescription();
    assertThat(tsd.getType(Token.class.getName()))
        .as("Token type auto-detection")
        .isNotNull();
    assertThat(tsd.getType(Sentence.class.getName()))
        .as("Sentence type auto-detection")
        .isNotNull();
    assertThat(tsd.getType(AnalyzedText.class.getName()))
        .as("AnalyzedText type auto-detection")
        .isNotNull();

    TypePriorityList[] typePrioritiesLists = typePriorities.getPriorityLists();
    assertThat(typePrioritiesLists.length).isEqualTo(1);
    assertThat(typePrioritiesLists[0].getTypes())
        .as("Type priorities auto-detection")
        .containsExactly(Sentence.class.getName(), AnalyzedText.class.getName(), Token.class.getName());

    FsIndexDescription[] indexes = aed.getAnalysisEngineMetaData().getFsIndexCollection().getFsIndexes();
    assertThat(indexes.length).isEqualTo(1);
    assertThat(indexes[0])
        .extracting(FsIndexDescription::getLabel, FsIndexDescription::getTypeName, FsIndexDescription::getKind)
        .containsExactly("Automatically Scanned Index", Token.class.getName(), FsIndexDescription.KIND_SORTED);
  }
}
