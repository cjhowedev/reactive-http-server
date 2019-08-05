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

import static me.cjoftheweb.reactive_http_server.syntax.HttpHeaderParserState.*;
import static me.cjoftheweb.reactive_http_server.syntax.TestHelpers.assertContainsInsensitive;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpHeaderParserStateTest {
  @Test
  void testNext() {
    assertEquals(PARSING_NAME.next(), PARSING_VALUE);
    assertEquals(PARSING_VALUE.next(), AWAITING_LINE_FEED);
    assertEquals(AWAITING_LINE_FEED.next(), DONE);
    assertEquals(DONE.next(), ERROR);
    assertEquals(ERROR.next(), ERROR);
  }

  @Test
  void testFriendlyStatusReport() {
    assertContainsInsensitive("header field name", PARSING_NAME.getFriendlyStatusReport());
    assertContainsInsensitive("header field value", PARSING_VALUE.getFriendlyStatusReport());
    assertContainsInsensitive("carriage return", AWAITING_LINE_FEED.getFriendlyStatusReport());
    assertContainsInsensitive("line feed", DONE.getFriendlyStatusReport());
    assertContainsInsensitive("error", ERROR.getFriendlyStatusReport());
  }
}
