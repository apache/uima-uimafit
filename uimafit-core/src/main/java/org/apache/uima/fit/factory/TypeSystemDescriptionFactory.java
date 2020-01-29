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
import static org.apache.uima.util.CasCreationUtils.mergeTypeSystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.internal.MetaDataType;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.Import_impl;
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TypeSystemDescriptionFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TypeSystemDescriptionFactory.class);
  
  private static final Object SCAN_LOCK = new Object();

  private static String[] typeDescriptorLocations;

  private TypeSystemDescriptionFactory() {
    // This class is not meant to be instantiated
  }

  /**
   * Creates a TypeSystemDescription from descriptor names.
   * 
   * @param descriptorNames
   *          The fully qualified, Java-style, dotted descriptor names.
   * @return A TypeSystemDescription that includes the types from all of the specified files.
   */
  public static TypeSystemDescription createTypeSystemDescription(String... descriptorNames) {
    TypeSystemDescription typeSystem = new TypeSystemDescription_impl();
    List<Import> imports = new ArrayList<Import>();
    for (String descriptorName : descriptorNames) {
      Import imp = new Import_impl();
      imp.setName(descriptorName);
      imports.add(imp);
    }
    Import[] importArray = new Import[imports.size()];
    typeSystem.setImports(imports.toArray(importArray));
    return typeSystem;
  }

  /**
   * Creates a TypeSystemDescription from a descriptor file
   * 
   * @param descriptorURIs
   *          The descriptor file paths.
   * @return A TypeSystemDescription that includes the types from all of the specified files.
   */
  public static TypeSystemDescription createTypeSystemDescriptionFromPath(String... descriptorURIs) {
    TypeSystemDescription typeSystem = new TypeSystemDescription_impl();
    List<Import> imports = new ArrayList<Import>();
    for (String descriptorURI : descriptorURIs) {
      Import imp = new Import_impl();
      imp.setLocation(descriptorURI);
      imports.add(imp);
    }
    Import[] importArray = new Import[imports.size()];
    typeSystem.setImports(imports.toArray(importArray));
    return typeSystem;
  }

  /**
   * Creates a {@link TypeSystemDescription} from all type descriptions that can be found via the
   * default import pattern or via the {@code META-INF/org.apache.uima.fit/types.txt} files in the
   * classpath.
   * 
   * @return the auto-scanned type system.
   * @throws ResourceInitializationException
   *           if the collected type system descriptions cannot be merged.
   */
  public static TypeSystemDescription createTypeSystemDescription()
          throws ResourceInitializationException {
    List<TypeSystemDescription> tsdList = new ArrayList<TypeSystemDescription>();
    for (String location : scanTypeDescriptors()) {
      try {
        XMLInputSource xmlInputType1 = new XMLInputSource(location);
        tsdList.add(getXMLParser().parseTypeSystemDescription(xmlInputType1));
        LOG.debug("Detected type system at [{}]", location);
      } catch (IOException e) {
        throw new ResourceInitializationException(e);
      } catch (InvalidXMLException e) {
        LOG.warn("[{}] is not a type file. Ignoring.", location, e);
      }
    }

    ResourceManager resMgr = ResourceManagerFactory.newResourceManager();
    return mergeTypeSystems(tsdList, resMgr);
  }

  /**
   * Get all currently accessible type system descriptor locations. A scan is actually only
   * performed on the first call and the locations are cached. To force a re-scan use
   * {@link #forceTypeDescriptorsScan()}.
   * 
   * @return an array of locations.
   * @throws ResourceInitializationException
   *           if the locations could not be resolved.
   */
  public static String[] scanTypeDescriptors() throws ResourceInitializationException {
    synchronized (SCAN_LOCK) {
      if (typeDescriptorLocations == null) {
        typeDescriptorLocations = scanDescriptors(MetaDataType.TYPE_SYSTEM);
      }
      return typeDescriptorLocations;
    }
  }

  /**
   * Force rescan of type descriptors. The next call to {@link #scanTypeDescriptors()} will rescan
   * all auto-import locations.
   */
  public static void forceTypeDescriptorsScan() {
    typeDescriptorLocations = null;
  }
}
