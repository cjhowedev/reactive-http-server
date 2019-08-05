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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpRequestLineParserStateTest {
  private void assertContainsInsensitive(final String expected, final String actual) {
    assertTrue(
        actual.toLowerCase().contains(expected.toLowerCase()),
        String.format("Expected %s to contain %s", actual, expected));
  }

  @Test
  void testNext() {
    var requestLineParserState = HttpRequestLineParserState.PARSING_METHOD;
    assertEquals(requestLineParserState.next(), HttpRequestLineParserState.PARSING_REQUEST_TARGET);

    requestLineParserState = HttpRequestLineParserState.PARSING_REQUEST_TARGET;
    assertEquals(requestLineParserState.next(), HttpRequestLineParserState.PARSING_VERSION);

    requestLineParserState = HttpRequestLineParserState.PARSING_VERSION;
    assertEquals(requestLineParserState.next(), HttpRequestLineParserState.AWAITING_LINE_FEED);

    requestLineParserState = HttpRequestLineParserState.AWAITING_LINE_FEED;
    assertEquals(requestLineParserState.next(), HttpRequestLineParserState.DONE);

    requestLineParserState = HttpRequestLineParserState.DONE;
    assertEquals(requestLineParserState.next(), HttpRequestLineParserState.ERROR);
  }

  @Test
  void testFriendlyStatusReport() {
    assertContainsInsensitive(
        "method", HttpRequestLineParserState.PARSING_METHOD.getFriendlyStatusReport());

    assertContainsInsensitive(
        "request target",
        HttpRequestLineParserState.PARSING_REQUEST_TARGET.getFriendlyStatusReport());

    assertContainsInsensitive(
        "version", HttpRequestLineParserState.PARSING_VERSION.getFriendlyStatusReport());

    assertContainsInsensitive(
        "carriage return", HttpRequestLineParserState.AWAITING_LINE_FEED.getFriendlyStatusReport());

    assertContainsInsensitive(
        "line feed", HttpRequestLineParserState.DONE.getFriendlyStatusReport());

    assertContainsInsensitive("error", HttpRequestLineParserState.ERROR.getFriendlyStatusReport());
  }
}
