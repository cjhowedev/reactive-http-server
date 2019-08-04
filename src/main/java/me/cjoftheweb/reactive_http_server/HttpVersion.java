/*
 * Copyright 2019 Christian Howe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.cjoftheweb.reactive_http_server;

import java.util.List;
import java.util.Optional;

public enum HttpVersion {
  HTTP_1_0("HTTP/1.0"),
  HTTP_1_1("HTTP/1.1");

  public static final int maxLength =
      List.of(values()).stream()
          .map(version -> version.versionString.length())
          .max(Integer::compare)
          .orElse(0);

  private final String versionString;

  HttpVersion(final String versionString) {
    this.versionString = versionString;
  }

  public static Optional<HttpVersion> fromVersionString(final String versionString) {
    for (var value : HttpVersion.values()) {
      if (value.versionString.equals(versionString)) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
  }

  public String getVersionString() {
    return this.versionString;
  }
}
