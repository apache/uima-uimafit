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
package org.apache.uima.fit.internal;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.internal.util.I18nUtil;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.apache.uima.util.impl.JSR47Logger_impl;

/**
 * INTERNAL API - Wrapper for the UIMA {@link Logger} offering a more convenient API similar to that
 * of the Apache Commons Logging interface {@link org.apache.commons.logging.Log Log} or to that of
 * Log4J's {@code Category} and SLF4J's {@code Logger}, using the names {@code error}, {@code warn},
 * {@code info}, {@code debug} and {@code trace} and mapping these to UIMA logging levels.
 * 
 */
public class ExtendedLogger implements Logger {

  private final UimaContext context;

  public ExtendedLogger(final UimaContext aContext) {
    context = aContext;
  }

  @Deprecated
  public void log(String aMessage) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        logger.log(aMessage);
      }
    }
  }

  @Deprecated
  public void log(String aResourceBundleName, String aMessageKey, Object[] aArguments) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        logger.log(aResourceBundleName, aMessageKey, aArguments);
      }
    }
  }

  @Deprecated
  public void logException(Exception aException) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        logger.logException(aException);
      }
    }
  }

  @Deprecated
  public void setOutputStream(PrintStream aStream) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().setOutputStream(aStream);
      }
    }
  }

  @Deprecated
  public void setOutputStream(OutputStream aStream) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().setOutputStream(aStream);
      }
    }
  }

  public void log(Level level, String aMessage) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        logger.log(getClass().getName(), level, aMessage, null);
      }
    }
  }

  public void log(Level level, String aMessage, Object param1) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        String result = MessageFormat.format(aMessage, new Object[] { param1 });
        logger.log(getClass().getName(), level, result, null);
      }
    }
  }

  public void log(Level level, String aMessage, Object[] params) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        String result = MessageFormat.format(aMessage, params);
        logger.log(getClass().getName(), level, result, null);
      }
    }
  }

  public void log(Level level, String aMessage, Throwable thrown) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        logger.log(getClass().getName(), level, aMessage, thrown);
      }
    }
  }

  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName,
          String msgKey) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        String result = I18nUtil.localizeMessage(bundleName, msgKey, null,
                getExtensionClassLoader());
        logger.log(getClass().getName(), level, result, null);
      }
    }
  }

  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName,
          String msgKey, Object param1) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        String result = I18nUtil.localizeMessage(bundleName, msgKey, new Object[] { param1 },
                getExtensionClassLoader());
        logger.log(getClass().getName(), level, result, null);
      }
    }
  }

  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName,
          String msgKey, Object[] params) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        String result = I18nUtil.localizeMessage(bundleName, msgKey, params,
                getExtensionClassLoader());
        logger.log(getClass().getName(), level, result, null);
      }
    }
  }

  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName,
          String msgKey, Throwable thrown) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null && logger.isLoggable(level)) {
        String result = I18nUtil.localizeMessage(bundleName, msgKey, null,
                getExtensionClassLoader());
        logger.log(getClass().getName(), level, result, thrown);
      }
    }
  }

  public void log(String wrapperFQCN, Level level, String message, Throwable thrown) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(wrapperFQCN, level, message, thrown);
      }
    }
  }

  public boolean isLoggable(Level level) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        return context.getLogger().isLoggable(level);
      }
    }
    return false;
  }

  public void setLevel(Level level) {
    context.getLogger().setLevel(level);
  }

  public void setResourceManager(ResourceManager resourceManager) {
    context.getLogger().setResourceManager(resourceManager);
  }

  @Override
  public String rb(String bundleName, String msgKey, Object... parameters) {
    return I18nUtil.localizeMessage(bundleName, msgKey, parameters, getExtensionClassLoader());
  }
  
  /**
   * Logs a message at {@link Level#FINE}.
   * 
   * @param paramObject
   *          a message.
   */
  public void debug(Object paramObject) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger()
                .log(getClass().getName(), Level.FINE, String.valueOf(paramObject), null);
      }
    }
  }

  /**
   * Logs a message at {@link Level#FINE}.
   * 
   * @param paramObject
   *          a message.
   * @param paramThrowable
   *          a cause.
   */
  public void debug(Object paramObject, Throwable paramThrowable) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.FINE, String.valueOf(paramObject),
                paramThrowable);
      }
    }
  }

  /**
   * Logs a message at {@link Level#SEVERE}.
   * 
   * @param paramObject
   *          a message.
   */
  public void error(Object paramObject) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.SEVERE, String.valueOf(paramObject),
                null);
      }
    }
  }

  /**
   * Logs a message at {@link Level#SEVERE}.
   * 
   * @param paramObject
   *          a message.
   * @param paramThrowable
   *          a cause.
   */
  public void error(Object paramObject, Throwable paramThrowable) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.SEVERE, String.valueOf(paramObject),
                paramThrowable);
      }
    }
  }

  /**
   * Logs a message at {@link Level#INFO}.
   * 
   * @param paramObject
   *          a message.
   */
  public void info(Object paramObject) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        if (context.getLogger() instanceof JSR47Logger_impl) {
          JSR47Logger_impl impl = (JSR47Logger_impl) context.getLogger();
        }
        context.getLogger()
                .log(getClass().getName(), Level.INFO, String.valueOf(paramObject), null);
      }
    }
  }

  /**
   * Logs a message at {@link Level#INFO}.
   * 
   * @param paramObject
   *          a message.
   * @param paramThrowable
   *          a cause.
   */
  public void info(Object paramObject, Throwable paramThrowable) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.INFO, String.valueOf(paramObject),
                paramThrowable);
      }
    }
  }

  public boolean isDebugEnabled() {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        return context.getLogger().isLoggable(Level.FINE);
      }
    }
    return false;
  }

  public boolean isErrorEnabled() {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        return context.getLogger().isLoggable(Level.SEVERE);
      }
    }
    return false;
  }

  public boolean isInfoEnabled() {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        return context.getLogger().isLoggable(Level.INFO);
      }
    }
    return false;
  }

  public boolean isTraceEnabled() {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        return context.getLogger().isLoggable(Level.FINER);
      }
    }
    return false;
  }

  public boolean isWarnEnabled() {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        return context.getLogger().isLoggable(Level.WARNING);
      }
    }
    return false;
  }

  /**
   * Logs a message at {@link Level#FINER}.
   * 
   * @param paramObject
   *          a message.
   */
  public void trace(Object paramObject) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.FINER, String.valueOf(paramObject),
                null);
      }
    }
  }

  /**
   * Logs a message at {@link Level#FINER}.
   * 
   * @param paramObject
   *          a message.
   * @param paramThrowable
   *          a cause.
   */
  public void trace(Object paramObject, Throwable paramThrowable) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.FINER, String.valueOf(paramObject),
                paramThrowable);
      }
    }
  }

  /**
   * Logs a message at {@link Level#WARNING}.
   * 
   * @param paramObject
   *          a message.
   */
  public void warn(Object paramObject) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.WARNING, String.valueOf(paramObject),
                null);
      }
    }
  }

  /**
   * Logs a message at {@link Level#WARNING}.
   * 
   * @param paramObject
   *          a message.
   * @param paramThrowable
   *          a cause.
   */
  public void warn(Object paramObject, Throwable paramThrowable) {
    if (context != null) {
      Logger logger = context.getLogger();
      if (logger != null) {
        context.getLogger().log(getClass().getName(), Level.WARNING, String.valueOf(paramObject),
                paramThrowable);
      }
    }
  }
  
  /**
   * Gets the extension ClassLoader to used to locate the message digests. If this returns null,
   * then message digests will be searched for using this.class.getClassLoader().
   */
  private ClassLoader getExtensionClassLoader() {
    if (context instanceof UimaContextAdmin) {
      ResourceManager resMgr = ((UimaContextAdmin) context).getResourceManager();
      if (resMgr != null) {
        return resMgr.getExtensionClassLoader();
      }
      else {
        return null;
      }
    }
    return null;
  }
}
