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

import java.nio.ByteBuffer;

import static me.cjoftheweb.reactive_http_server.syntax.HttpHeaderParser.trimTrailingSpace;
import static org.junit.jupiter.api.Assertions.*;

class HttpHeaderParserTest {
  private void assertInvalidParser(final HttpHeaderParser httpHeaderParser) {
    assertFalse(httpHeaderParser.isValid());

    assertThrows(InvalidParserException.class, httpHeaderParser::getKey);
    assertThrows(InvalidParserException.class, httpHeaderParser::getValue);
  }

  @Test
  void testParse() throws Exception {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key: Value\r\n".getBytes());

    httpHeaderParser.offer(byteBuffer);

    assertFalse(byteBuffer.hasRemaining());
    assertTrue(httpHeaderParser.isDone());
    assertTrue(httpHeaderParser.isValid());

    assertEquals("Key", httpHeaderParser.getKey());
    assertEquals("Value", httpHeaderParser.getValue());
  }

  @Test
  void testParseExtraBytes() throws Exception {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key: Value\r\nextra".getBytes());

    httpHeaderParser.offer(byteBuffer);

    assertTrue(byteBuffer.hasRemaining());
    assertEquals(5, byteBuffer.remaining());
    assertTrue(httpHeaderParser.isDone());
    assertTrue(httpHeaderParser.isValid());

    assertEquals("Key", httpHeaderParser.getKey());
    assertEquals("Value", httpHeaderParser.getValue());
  }

  @Test
  void testParseValueWithWhitespace() throws Exception {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key: Value With Space \r\n".getBytes());

    httpHeaderParser.offer(byteBuffer);

    assertFalse(byteBuffer.hasRemaining());
    assertTrue(httpHeaderParser.isDone());
    assertTrue(httpHeaderParser.isValid());

    assertEquals("Key", httpHeaderParser.getKey());
    assertEquals("Value With Space", httpHeaderParser.getValue());
  }

  @Test
  void testReset() throws Exception {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key1: Value1\r\n".getBytes());

    httpHeaderParser.offer(byteBuffer);

    assertFalse(byteBuffer.hasRemaining());
    assertTrue(httpHeaderParser.isDone());
    assertTrue(httpHeaderParser.isValid());

    assertEquals("Key1", httpHeaderParser.getKey());
    assertEquals("Value1", httpHeaderParser.getValue());

    httpHeaderParser.reset();

    byteBuffer = ByteBuffer.wrap("Key2: Value2\r\n".getBytes());

    httpHeaderParser.offer(byteBuffer);

    assertFalse(byteBuffer.hasRemaining());
    assertTrue(httpHeaderParser.isDone());
    assertTrue(httpHeaderParser.isValid());

    assertEquals("Key2", httpHeaderParser.getKey());
    assertEquals("Value2", httpHeaderParser.getValue());
  }

  @Test
  void testEmptyName() {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap(": Value\r\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(0, ex.getOffset());

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }

  @Test
  void testLongName() {
    var httpHeaderParser = new HttpHeaderParser(10, 0);
    var byteBuffer = ByteBuffer.wrap("superlongname: Value\r\n".getBytes());

    var ex = assertThrows(HttpHeaderNameTooLong.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(10, ex.getMaxHeaderNameLength());

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }

  @Test
  void testEmptyValue() {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key: \r\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(5, ex.getOffset());

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }

  @Test
  void testLongValue() {
    var httpHeaderParser = new HttpHeaderParser(0, 10);
    var byteBuffer = ByteBuffer.wrap("Key: superlongvalue\r\n".getBytes());

    var ex = assertThrows(HttpHeaderValueTooLong.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(10, ex.getMaxHeaderValueLength());

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }

  @Test
  void testTrimTrailingSpace() {
    assertEquals("", trimTrailingSpace("  "));
    assertEquals("test value", trimTrailingSpace("test value \t "));
    assertEquals("test with other space \n", trimTrailingSpace("test with other space \n \t"));
  }

  @Test
  void testExtraSemicolon() {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key::\r\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(4, ex.getOffset());

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }

  @Test
  void testKeyWithSpace() {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key : Value\r\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(3, ex.getOffset());

    assertTrue(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }

  @Test
  void testEarlyCarriageReturn() {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key\r".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(3, ex.getOffset());

    assertFalse(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }

  @Test
  void testEarlyLineFeed() {
    var httpHeaderParser = new HttpHeaderParser();
    var byteBuffer = ByteBuffer.wrap("Key: Value\n".getBytes());

    var ex = assertThrows(ParseException.class, () -> httpHeaderParser.offer(byteBuffer));
    assertEquals(10, ex.getOffset());

    assertFalse(byteBuffer.hasRemaining());
    assertFalse(httpHeaderParser.isDone());

    assertInvalidParser(httpHeaderParser);
  }
}
