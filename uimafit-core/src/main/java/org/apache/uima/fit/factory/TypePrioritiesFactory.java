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

import static org.apache.uima.UIMAFramework.getXMLParser;
import static org.apache.uima.fit.internal.MetaDataUtil.scanDescriptors;
import static org.apache.uima.fit.util.CasUtil.UIMA_BUILTIN_JCAS_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.commons.logging.LogFactory;
import org.apache.uima.fit.internal.MetaDataType;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypePriorityList;
import org.apache.uima.resource.metadata.impl.TypePriorities_impl;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.springframework.util.ClassUtils;

public final class TypePrioritiesFactory {
  private static final Object SCAN_LOCK = new Object();

  private static final Object CREATE_LOCK = new Object();

  private static WeakHashMap<ClassLoader, String[]> typePrioritesLocationsByClassloader;

  private static WeakHashMap<ClassLoader, TypePriorities> typePrioritiesByClassloader;

  static {
    typePrioritesLocationsByClassloader = new WeakHashMap<>();
    typePrioritiesByClassloader = new WeakHashMap<>();
  }

  private TypePrioritiesFactory() {
    // This class is not meant to be instantiated
  }

  /**
   * Create a TypePriorities given a sequence of ordered type classes
   *
   * @param prioritizedTypes
   *          a sequence of ordered type classes
   * @return type priorities created from the ordered JCas classes
   */
  public static TypePriorities createTypePriorities(Class<?>... prioritizedTypes) {
    String[] typeNames = new String[prioritizedTypes.length];
    for (int i = 0; i < prioritizedTypes.length; i++) {
      String typeName = prioritizedTypes[i].getName();
      if (typeName.startsWith(UIMA_BUILTIN_JCAS_PREFIX)) {
        typeName = "uima." + typeName.substring(UIMA_BUILTIN_JCAS_PREFIX.length());
      }

      typeNames[i] = typeName;
    }
    return createTypePriorities(typeNames);
  }

  /**
   * Create a {@link TypePriorities} given a sequence of ordered type names
   *
   * @param prioritizedTypeNames
   *          a sequence of ordered type names
   * @return type priorities created from the ordered type names
   */
  public static TypePriorities createTypePriorities(String... prioritizedTypeNames) {
    TypePriorities typePriorities = new TypePriorities_impl();
    TypePriorityList typePriorityList = typePriorities.addPriorityList();
    for (String typeName : prioritizedTypeNames) {
      typePriorityList.addType(typeName);
    }
    return typePriorities;
  }

  /**
   * Creates a {@link TypePriorities} from all type priorities descriptions that can be found via
   * the pattern specified in the system property
   * {@code org.apache.uima.fit.typepriorities.import_pattern} or via the
   * {@code META-INF/org.apache.uima.fit/typepriorities.txt} files in the classpath.
   *
   * @return the auto-scanned type priorities.
   * @throws ResourceInitializationException
   *           if the collected type priorities cannot be merged.
   */
  public static TypePriorities createTypePriorities() throws ResourceInitializationException {
    ClassLoader cl = ClassUtils.getDefaultClassLoader();
    TypePriorities aggTypePriorities = typePrioritiesByClassloader.get(cl);
    if (aggTypePriorities == null) {
      synchronized (CREATE_LOCK) {
        List<TypePriorities> typePrioritiesList = new ArrayList<>();
        for (String location : scanTypePrioritiesDescriptors()) {
          try {
            XMLInputSource xmlInput = new XMLInputSource(location);
            TypePriorities typePriorities = getXMLParser().parseTypePriorities(xmlInput);
            typePriorities.resolveImports();
            typePrioritiesList.add(typePriorities);
            LogFactory.getLog(TypePrioritiesFactory.class)
                    .debug("Detected type priorities at [" + location + "]");
          } catch (IOException e) {
            throw new ResourceInitializationException(e);
          } catch (InvalidXMLException e) {
            LogFactory.getLog(TypePrioritiesFactory.class).warn(
                    "[" + location + "] is not a type priorities descriptor file. Ignoring.", e);
          }
        }

        ResourceManager resMgr = ResourceManagerFactory.newResourceManager();
        aggTypePriorities = CasCreationUtils.mergeTypePriorities(typePrioritiesList, resMgr);
        typePrioritiesByClassloader.put(cl, aggTypePriorities);
      }
    }

    return (TypePriorities) aggTypePriorities.clone();
  }

  /**
   * Get all currently accessible type priorities descriptor locations. A scan is actually only
   * performed on the first call and the locations are cached. To force a re-scan use
   * {@link #forceTypePrioritiesDescriptorsScan()}.
   *
   * @return an array of locations.
   * @throws ResourceInitializationException
   *           if the locations could not be resolved.
   */
  public static String[] scanTypePrioritiesDescriptors() throws ResourceInitializationException {
    synchronized (SCAN_LOCK) {
      ClassLoader cl = ClassUtils.getDefaultClassLoader();
      String[] typePrioritesLocations = typePrioritesLocationsByClassloader.get(cl);
      if (typePrioritesLocations == null) {
        typePrioritesLocations = scanDescriptors(MetaDataType.TYPE_PRIORITIES);
        typePrioritesLocationsByClassloader.put(cl, typePrioritesLocations);
      }
      return typePrioritesLocations;
    }
  }

  /**
   * Force rescan of type priorities descriptors. The next call to
   * {@link #scanTypePrioritiesDescriptors()} will rescan all auto-import locations.
   */
  public static void forceTypePrioritiesDescriptorsScan() {
    synchronized (SCAN_LOCK) {
      typePrioritesLocationsByClassloader.clear();
      typePrioritiesByClassloader.clear();
    }
  }
}
