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
package org.apache.uima.fit.validation;

import static java.lang.String.format;
import static org.apache.uima.fit.validation.CasValidationResult.Severity.DEBUG;
import static org.apache.uima.fit.validation.CasValidationResult.Severity.ERROR;
import static org.apache.uima.fit.validation.CasValidationResult.Severity.INFO;
import static org.apache.uima.fit.validation.CasValidationResult.Severity.TRACE;
import static org.apache.uima.fit.validation.CasValidationResult.Severity.WARN;

import java.util.Objects;

/**
 * Individual result from a CAS validation check.
 */
public class CasValidationResult {

  public enum Severity {

    ERROR(5), WARN(4), INFO(3), DEBUG(2), TRACE(1);

    private final int level;

    private Severity(int level) {
      this.level = level;
    }

    public boolean isEquallyOrMoreSevereThan(Severity other) {
      return level >= other.level;
    }
  }

  private final Severity severity;
  private final String source;
  private final String message;

  public CasValidationResult(Object source, Severity severity, String format, Object... args) {

    super();

    if (source instanceof String) {
      this.source = (String) source;
    } else if (source instanceof Class) {
      this.source = ((Class<?>) source).getSimpleName();
    } else {
      this.source = source != null ? source.getClass().getSimpleName() : null;
    }

    this.severity = severity;
    message = format(format, args);
  }

  public String getSource() {
    return source;
  }

  public String getMessage() {
    return message;
  }

  public Severity getSeverity() {
    return severity;
  }

  public static CasValidationResult error(Object source, String format, Object... args) {
    return new CasValidationResult(source, ERROR, format, args);
  }

  public static CasValidationResult warn(Object source, String format, Object... args) {
    return new CasValidationResult(source, WARN, format, args);
  }

  public static CasValidationResult info(Object source, String format, Object... args) {
    return new CasValidationResult(source, INFO, format, args);
  }

  public static CasValidationResult debug(Object source, String format, Object... args) {
    return new CasValidationResult(source, DEBUG, format, args);
  }

  public static CasValidationResult trace(Object source, String format, Object... args) {
    return new CasValidationResult(source, TRACE, format, args);
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof CasValidationResult)) {
      return false;
    }
    CasValidationResult castOther = (CasValidationResult) other;
    return Objects.equals(severity, castOther.severity) && Objects.equals(source, castOther.source)
            && Objects.equals(message, castOther.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(severity, source, message);
  }

  @Override
  public String toString() {
    return String.format("[%s] %s", source != null ? source : "<unknown>", message);
  }
}
