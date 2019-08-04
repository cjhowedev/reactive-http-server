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

class RequestLineParserStateTest {
  private void assertContainsInsensitive(final String expected, final String actual) {
    assertTrue(
        actual.toLowerCase().contains(expected.toLowerCase()),
        String.format("Expected %s to contain %s", actual, expected));
  }

  @Test
  void testNext() {
    var requestLineParserState = RequestLineParserState.PARSING_METHOD;
    assertEquals(requestLineParserState.next(), RequestLineParserState.PARSING_REQUEST_TARGET);

    requestLineParserState = RequestLineParserState.PARSING_REQUEST_TARGET;
    assertEquals(requestLineParserState.next(), RequestLineParserState.PARSING_VERSION);

    requestLineParserState = RequestLineParserState.PARSING_VERSION;
    assertEquals(requestLineParserState.next(), RequestLineParserState.AWAITING_LINE_FEED);

    requestLineParserState = RequestLineParserState.AWAITING_LINE_FEED;
    assertEquals(requestLineParserState.next(), RequestLineParserState.DONE);

    requestLineParserState = RequestLineParserState.DONE;
    assertEquals(requestLineParserState.next(), RequestLineParserState.ERROR);
  }

  @Test
  void testFriendlyStatusReport() {
    assertContainsInsensitive(
        "method", RequestLineParserState.PARSING_METHOD.friendlyStatusReport());

    assertContainsInsensitive(
        "request target", RequestLineParserState.PARSING_REQUEST_TARGET.friendlyStatusReport());

    assertContainsInsensitive(
        "version", RequestLineParserState.PARSING_VERSION.friendlyStatusReport());

    assertContainsInsensitive(
        "carriage return", RequestLineParserState.AWAITING_LINE_FEED.friendlyStatusReport());

    assertContainsInsensitive("line feed", RequestLineParserState.DONE.friendlyStatusReport());

    assertContainsInsensitive("error", RequestLineParserState.ERROR.friendlyStatusReport());
  }
}
