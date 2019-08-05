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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static me.cjoftheweb.reactive_http_server.syntax.HttpHeaderParserState.*;

class HttpHeaderParser implements Parser {
  private final int maxHeaderNameSize;
  private final int maxHeaderValueSize;
  private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
  private int offset = 0;
  private HttpHeaderParserState state = PARSING_NAME;
  private String key = null;
  private String value = null;

  HttpHeaderParser() {
    this.maxHeaderNameSize = 0;
    this.maxHeaderValueSize = 0;
  }

  HttpHeaderParser(final int maxHeaderNameSize, final int maxHeaderValueSize) {
    this.maxHeaderNameSize = maxHeaderNameSize;
    this.maxHeaderValueSize = maxHeaderValueSize;
  }

  static String trimTrailingSpace(final String string) {
    for (var i = string.length() - 1; i >= 0; i--) {
      char c = string.charAt(i);
      if (c != ' ' && c != '\t') {
        return string.substring(0, i + 1);
      }
    }
    return "";
  }

  private void ensureValid() {
    if (state == ERROR) {
      throw new InvalidParserException(HttpHeaderParser.class);
    }
  }

  private void throwParseException(final String message) throws ParseException {
    state = ERROR;
    throw new ParseException(
        String.format("%s %s", message, state.getFriendlyStatusReport()), offset);
  }

  @Override
  public void offer(ByteBuffer buffer)
      throws ParseException, HttpHeaderNameTooLong, HttpHeaderValueTooLong {
    ensureValid();

    while (buffer.hasRemaining()) {
      if (state == DONE) {
        return;
      }

      byte nextByte = buffer.get();

      switch (nextByte) {
        case ' ':
        case '\t':
          if (state == PARSING_VALUE) {
            if (byteArrayOutputStream.size() > 0) {
              byteArrayOutputStream.write(nextByte);
            }
          } else {
            throwParseException(String.format("Unexpected %s", nextByte == ' ' ? "space" : "tab"));
          }
          break;
        case ':':
          if (state == PARSING_NAME) {
            if (byteArrayOutputStream.size() <= 0) {
              throwParseException("Empty header field name");
            }

            key = byteArrayOutputStream.toString();
            byteArrayOutputStream.reset();
            state = state.next();
          } else {
            throwParseException("Unexpected semicolon");
          }
          break;
        case '\r':
          if (state == PARSING_VALUE) {
            if (byteArrayOutputStream.size() <= 0) {
              throwParseException("Empty header field value");
            }

            value = trimTrailingSpace(byteArrayOutputStream.toString());
            byteArrayOutputStream.reset();
            state = state.next();
          } else {
            throwParseException("Unexpected carriage return");
          }
          break;
        case '\n':
          if (state == AWAITING_LINE_FEED) {
            state = state.next();
          } else {
            throwParseException("Unexpected line feed");
          }
          break;
        default:
          byteArrayOutputStream.write(nextByte);
          switch (state) {
            case PARSING_NAME:
              if (maxHeaderNameSize > 0 && byteArrayOutputStream.size() > maxHeaderNameSize) {
                state = ERROR;
                throw new HttpHeaderNameTooLong(maxHeaderNameSize);
              }
              break;
            case PARSING_VALUE:
              if (maxHeaderValueSize > 0 && byteArrayOutputStream.size() > maxHeaderValueSize) {
                state = ERROR;
                throw new HttpHeaderValueTooLong(maxHeaderValueSize);
              }
              break;
          }
      }

      offset++;
    }
  }

  @Override
  public boolean isValid() {
    return state != ERROR;
  }

  @Override
  public boolean isDone() {
    return state == DONE;
  }

  @Override
  public void reset() {
    state = PARSING_NAME;
    byteArrayOutputStream.reset();
    key = null;
    value = null;
  }

  public String getKey() {
    ensureValid();
    return key;
  }

  public String getValue() {
    ensureValid();
    return value;
  }
}
