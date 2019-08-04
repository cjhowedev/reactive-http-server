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

import me.cjoftheweb.reactive_http_server.HttpMethod;
import me.cjoftheweb.reactive_http_server.HttpVersion;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.cjoftheweb.reactive_http_server.HttpMethod.GET;
import static me.cjoftheweb.reactive_http_server.HttpMethod.POST;
import static me.cjoftheweb.reactive_http_server.HttpVersion.HTTP_1_0;
import static me.cjoftheweb.reactive_http_server.HttpVersion.HTTP_1_1;
import static org.junit.jupiter.api.Assertions.*;

class HttpRequestLineParserTest {
  private void assertInvalidParser(final HttpRequestLineParser httpRequestLineParser) {
    assertFalse(httpRequestLineParser.isValid());

    assertThrows(
        InvalidParserException.class, () -> httpRequestLineParser.offer(ByteBuffer.allocate(0)));
    assertThrows(InvalidParserException.class, httpRequestLineParser::getMethod);
    assertThrows(InvalidParserException.class, httpRequestLineParser::getRequestTarget);
    assertThrows(InvalidParserException.class, httpRequestLineParser::getVersion);
  }

  @Test
  void testHttpMethods() throws Exception {
    for (var method : HttpMethod.values()) {
      var httpRequestLineParser = new HttpRequestLineParser();
      var byteBuffer =
          ByteBuffer.wrap(String.format("%s / HTTP/1.1\r\n", method.toString()).getBytes());

      httpRequestLineParser.offer(byteBuffer);

      assertFalse(byteBuffer.hasRemaining());
      assertTrue(httpRequestLineParser.isDone());
      assertTrue(httpRequestLineParser.isValid());

      assertEquals(method, httpRequestLineParser.getMethod());
      assertEquals("/", httpRequestLineParser.getRequestTarget());
      assertEquals(HTTP_1_1, httpRequestLineParser.getVersion());
    }
  }

  @Test
  void testShortInvalidHttpMethod() {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("FAIL / HTTP/1.1\r\n".getBytes());

    var ex =
        assertThrows(UnsupportedHttpMethod.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(ex.getHttpMethod(), "FAIL");

    assertInvalidParser(httpRequestLineParser);

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());
  }

  @Test
  void testLongInvalidHttpMethod() {
    var httpRequestLineParser = new HttpRequestLineParser();
    var badMethod = "BADMETHODISTOOLONG";
    var byteBuffer = ByteBuffer.wrap(String.format("%s / HTTP/1.1\r\n", badMethod).getBytes());

    var ex =
        assertThrows(UnsupportedHttpMethod.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(badMethod.substring(0, HttpMethod.maxLength + 1), ex.getHttpMethod());

    assertInvalidParser(httpRequestLineParser);

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());

    // ensure it didn't parse past the longest possible HTTP method
    assertEquals(HttpMethod.maxLength + 1, byteBuffer.position());
  }

  @Test
  void testHttpVersions() throws Exception {
    for (var version : HttpVersion.values()) {
      var httpRequestLineParser = new HttpRequestLineParser();
      var byteBuffer =
          ByteBuffer.wrap(String.format("GET / %s\r\n", version.getVersionString()).getBytes());

      httpRequestLineParser.offer(byteBuffer);

      assertFalse(byteBuffer.hasRemaining());
      assertTrue(httpRequestLineParser.isDone());
      assertTrue(httpRequestLineParser.isValid());

      assertEquals(GET, httpRequestLineParser.getMethod());
      assertEquals("/", httpRequestLineParser.getRequestTarget());
      assertEquals(version, httpRequestLineParser.getVersion());
    }
  }

  @Test
  void testShortInvalidHttpVersion() {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("GET / HTTP/1.2\r\n".getBytes());

    var ex =
        assertThrows(UnsupportedHttpVersion.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals("HTTP/1.2", ex.getHttpVersion());

    assertInvalidParser(httpRequestLineParser);

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());
  }

  @Test
  void testLongInvalidHttpVersion() {
    var badVersion = "HTTP/1.1.1.1.1";
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap(String.format("GET / %s\r\n", badVersion).getBytes());

    var ex =
        assertThrows(UnsupportedHttpVersion.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(badVersion.substring(0, HttpVersion.maxLength + 1), ex.getHttpVersion());

    assertInvalidParser(httpRequestLineParser);

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());

    // ensure it didn't parse past the longest possible HTTP version
    assertEquals(HttpVersion.maxLength + 7, byteBuffer.position());
  }

  @Test
  void testLongRequestTarget() {
    var maxRequestTargetLength = 8000;
    var httpRequestLineParser = new HttpRequestLineParser(maxRequestTargetLength);

    var requestTarget =
        IntStream.range(0, maxRequestTargetLength * 2)
            .boxed()
            .map(i -> "/")
            .collect(Collectors.joining());
    var byteBuffer =
        ByteBuffer.wrap(String.format("GET %s HTTP/1.1\r\n", requestTarget).getBytes());

    var ex =
        assertThrows(RequestTargetTooLong.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(maxRequestTargetLength, ex.getMaxRequestTargetLength());

    assertInvalidParser(httpRequestLineParser);

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());

    // ensure it didn't parse past the longest possible request target
    assertEquals(maxRequestTargetLength + 5, byteBuffer.position());
  }

  @Test
  void testRequestWithoutVersion() {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("GET /\r\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(5, ex.getOffset());

    assertInvalidParser(httpRequestLineParser);

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());
  }

  @Test
  void testRequestExtraSpace() {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("GET / HTTP/1.1 \r\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(14, ex.getOffset());

    assertInvalidParser(httpRequestLineParser);

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());
  }

  @Test
  void testRequestNoCarriageReturn() {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("GET / HTTP/1.1\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(14, ex.getOffset());

    assertInvalidParser(httpRequestLineParser);

    assertFalse(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());
  }

  @Test
  void testRequestNoCharacterAfterCarriageReturn() {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("GET / HTTP/1.1\rc".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpRequestLineParser.offer(byteBuffer));
    assertEquals(15, ex.getOffset());

    assertInvalidParser(httpRequestLineParser);

    assertFalse(byteBuffer.hasRemaining());
    assertFalse(httpRequestLineParser.isDone());
  }

  @Test
  void testRequestExtraCharacters() throws Exception {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("GET / HTTP/1.1\r\ntest".getBytes());

    httpRequestLineParser.offer(byteBuffer);

    assertTrue(byteBuffer.hasRemaining());
    assertTrue(httpRequestLineParser.isDone());
    assertTrue(httpRequestLineParser.isValid());

    assertEquals(GET, httpRequestLineParser.getMethod());
    assertEquals("/", httpRequestLineParser.getRequestTarget());
    assertEquals(HTTP_1_1, httpRequestLineParser.getVersion());
  }

  @Test
  void testReset() throws Exception {
    var httpRequestLineParser = new HttpRequestLineParser();
    var byteBuffer = ByteBuffer.wrap("GET / HTTP/1.1\r\n".getBytes());

    httpRequestLineParser.offer(byteBuffer);

    assertFalse(byteBuffer.hasRemaining());
    assertTrue(httpRequestLineParser.isDone());
    assertTrue(httpRequestLineParser.isValid());

    assertEquals(GET, httpRequestLineParser.getMethod());
    assertEquals("/", httpRequestLineParser.getRequestTarget());
    assertEquals(HTTP_1_1, httpRequestLineParser.getVersion());

    httpRequestLineParser.reset();

    byteBuffer = ByteBuffer.wrap("POST /test HTTP/1.0\r\n".getBytes());

    httpRequestLineParser.offer(byteBuffer);

    assertFalse(byteBuffer.hasRemaining());
    assertTrue(httpRequestLineParser.isDone());
    assertTrue(httpRequestLineParser.isValid());

    assertEquals(POST, httpRequestLineParser.getMethod());
    assertEquals("/test", httpRequestLineParser.getRequestTarget());
    assertEquals(HTTP_1_0, httpRequestLineParser.getVersion());
  }
}
