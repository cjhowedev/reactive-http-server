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

package me.cjoftheweb.reactive_http_server.syntax;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestHelpers {
  private TestHelpers() {}

  static void assertContainsInsensitive(final String expected, final String actual) {
    assertTrue(
        actual.toLowerCase().contains(expected.toLowerCase()),
        String.format("Expected %s to contain %s", actual, expected));
  }
}
