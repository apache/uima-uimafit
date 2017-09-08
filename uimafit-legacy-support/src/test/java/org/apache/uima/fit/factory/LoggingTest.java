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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.component.CasConsumer_ImplBase;
import org.apache.uima.fit.component.CasFlowController_ImplBase;
import org.apache.uima.fit.component.CasMultiplier_ImplBase;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.component.JCasFlowController_ImplBase;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.flow.Flow;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.Progress;
import org.junit.Test;

/**
 */
public class LoggingTest {
  @Test
  public void testLogger() throws Exception {
    final List<LogRecord> records = new ArrayList<LogRecord>();

    // Tell the logger to log everything
    ConsoleHandler handler = (ConsoleHandler) LogManager.getLogManager().getLogger("")
            .getHandlers()[0];
    java.util.logging.Level oldLevel = handler.getLevel();
    handler.setLevel(Level.ALL);
    // Capture the logging output without actually logging it
    handler.setFilter(new Filter() {
      @Override
      public boolean isLoggable(LogRecord record) {
        records.add(record);
        return false;
      }
    });

    try {
      JCas jcas = JCasFactory.createJCas();
      createEngine(LoggingCasConsumerChristmasTree.class).process(jcas.getCas());

      // Workaround for https://issues.apache.org/jira/browse/UIMA-5323
      Iterator<LogRecord> i = records.iterator();
      while (i.hasNext()) {
        if (i.next().getMessage().contains("Skipping adding")) {
          i.remove();
        }
      }

      assertEquals(10, records.size());
      assertEquals(Level.FINER, records.get(0).getLevel());
      assertEquals(Level.FINER, records.get(1).getLevel());
      assertEquals(Level.FINE, records.get(2).getLevel());
      assertEquals(Level.FINE, records.get(3).getLevel());
      assertEquals(Level.INFO, records.get(4).getLevel());
      assertEquals(Level.INFO, records.get(5).getLevel());
      assertEquals(Level.WARNING, records.get(6).getLevel());
      assertEquals(Level.WARNING, records.get(7).getLevel());
      assertEquals(Level.SEVERE, records.get(8).getLevel());
      assertEquals(Level.SEVERE, records.get(9).getLevel());
    } finally {
      if (oldLevel != null) {
        handler.setLevel(oldLevel);
        handler.setFilter(null);
      }
    }
  }

  @Test
  public void testAllKindsOfComponents() throws Exception {
    final List<LogRecord> records = new ArrayList<LogRecord>();

    // Tell the logger to log everything
    ConsoleHandler handler = (ConsoleHandler) LogManager.getLogManager().getLogger("")
            .getHandlers()[0];
    java.util.logging.Level oldLevel = handler.getLevel();
    handler.setLevel(Level.ALL);
    handler.setFilter(new Filter() {
      @Override
      public boolean isLoggable(LogRecord record) {
        records.add(record);
        return true;
      }
    });

    try {
      JCas jcas = JCasFactory.createJCas();

      createReader(LoggingCasCollectionReader.class).hasNext();
      assertLogDone(records);

      createReader(LoggingJCasCollectionReader.class).hasNext();
      assertLogDone(records);

      // createFlowControllerDescription(LoggingJCasFlowController.class).
      // assertLogDone(records);

      createEngine(LoggingCasAnnotator.class).process(jcas.getCas());
      assertLogDone(records);

      createEngine(LoggingJCasAnnotator.class).process(jcas);
      assertLogDone(records);

      createEngine(LoggingCasConsumer.class).process(jcas.getCas());
      assertLogDone(records);

      createEngine(LoggingJCasConsumer.class).process(jcas);
      assertLogDone(records);

      createEngine(LoggingCasMultiplier.class).process(jcas.getCas());
      assertLogDone(records);

      createEngine(LoggingJCasMultiplier.class).process(jcas);
      assertLogDone(records);
    } finally {
      if (oldLevel != null) {
        handler.setLevel(oldLevel);
        handler.setFilter(null);
      }
    }
  }

  private void assertLogDone(List<LogRecord> records) {
    // Workaround for https://issues.apache.org/jira/browse/UIMA-5323
    Iterator<LogRecord> i = records.iterator();
    while (i.hasNext()) {
      if (i.next().getMessage().contains("Skipping adding")) {
        i.remove();
      }
    }
    
    assertEquals(1, records.size());
    assertEquals(Level.INFO, records.get(0).getLevel());
    records.clear();
  }

  public static class LoggingCasConsumerChristmasTree extends CasConsumer_ImplBase {
    @Override
    public void process(CAS aCAS) throws AnalysisEngineProcessException {
      getLogger().setLevel(org.apache.uima.util.Level.ALL);
      trigger();
      getLogger().setLevel(org.apache.uima.util.Level.OFF);
      trigger();
    }

    private void trigger() {
      if (getLogger().isTraceEnabled()) {
        getLogger().trace("Logging: " + getClass().getName());
        getLogger().trace("Logging: " + getClass().getName(), new IllegalArgumentException());
      }
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Logging: " + getClass().getName());
        getLogger().debug("Logging: " + getClass().getName(), new IllegalArgumentException());
      }
      if (getLogger().isInfoEnabled()) {
        getLogger().info("Logging: " + getClass().getName());
        getLogger().info("Logging: " + getClass().getName(), new IllegalArgumentException());
      }
      if (getLogger().isWarnEnabled()) {
        getLogger().warn("Logging: " + getClass().getName());
        getLogger().warn("Logging: " + getClass().getName(), new IllegalArgumentException());
      }
      if (getLogger().isErrorEnabled()) {
        getLogger().error("Logging: " + getClass().getName());
        getLogger().error("Logging: " + getClass().getName(), new IllegalArgumentException());
      }
    }
  }

  public static class LoggingCasMultiplier extends CasMultiplier_ImplBase {

    @Override
    public boolean hasNext() throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
      return false;
    }

    @Override
    public AbstractCas next() throws AnalysisEngineProcessException {
      // Never called
      return null;
    }

    @Override
    public void process(CAS aCAS) throws AnalysisEngineProcessException {
      // Never called
    }
  }

  public static class LoggingJCasMultiplier extends JCasMultiplier_ImplBase {
    @Override
    public boolean hasNext() throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
      return false;
    }

    @Override
    public AbstractCas next() throws AnalysisEngineProcessException {
      // Never called
      return null;
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
      // Never called
    }
  }

  public static class LoggingJCasCollectionReader extends JCasCollectionReader_ImplBase {
    @Override
    public boolean hasNext() throws IOException, CollectionException {
      getLogger().info("Logging: " + getClass().getName());
      return false;
    }

    @Override
    public Progress[] getProgress() {
      return new Progress[0];
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
      // Never called
    }
  }

  public static class LoggingResource extends Resource_ImplBase {
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
            throws ResourceInitializationException {
      boolean ret = super.initialize(aSpecifier, aAdditionalParams);
      getLogger().info("Logging: " + getClass().getName());
      return ret;
    }
  }

  public static class LoggingCasCollectionReader extends CasCollectionReader_ImplBase {
    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException {
      // Never called
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
      getLogger().info("Logging: " + getClass().getName());
      return false;
    }

    @Override
    public Progress[] getProgress() {
      return new Progress[0];
    }
  }

  public static class LoggingCasAnnotator extends CasAnnotator_ImplBase {
    @Override
    public void process(CAS aCAS) throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
    }
  }

  public static class LoggingCasConsumer extends CasConsumer_ImplBase {
    @Override
    public void process(CAS aCAS) throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
    }
  }

  public static class LoggingJCasAnnotator extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
    }
  }

  public static class LoggingJCasConsumer extends JCasConsumer_ImplBase {
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
    }
  }

  public static class LoggingCasFlowController extends CasFlowController_ImplBase {
    @Override
    public Flow computeFlow(CAS aCAS) throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
      return null;
    }
  }

  public static class LoggingJCasFlowController extends JCasFlowController_ImplBase {
    @Override
    public Flow computeFlow(JCas aJCas) throws AnalysisEngineProcessException {
      getLogger().info("Logging: " + getClass().getName());
      return null;
    }
  }
}
