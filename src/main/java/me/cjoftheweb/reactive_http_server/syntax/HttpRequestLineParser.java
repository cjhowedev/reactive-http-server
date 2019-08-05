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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static me.cjoftheweb.reactive_http_server.syntax.HttpRequestLineParserState.*;

class HttpRequestLineParser implements Parser {
  private final int maxRequestTargetLength;
  private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
  private int offset = 0;
  private HttpMethod method = null;
  private String requestTarget = null;
  private HttpVersion version = null;
  private HttpRequestLineParserState state = PARSING_METHOD;

  HttpRequestLineParser() {
    this.maxRequestTargetLength = 0;
  }

  HttpRequestLineParser(final int maxRequestTargetLength) {
    this.maxRequestTargetLength = maxRequestTargetLength;
  }

  private void ensureValid() {
    if (state == ERROR) {
      throw new InvalidParserException(HttpRequestLineParser.class);
    }
  }

  private void throwParseException(final String message) throws ParseException {
    state = ERROR;
    throw new ParseException(
        String.format("%s %s", message, state.getFriendlyStatusReport()), offset);
  }

  @Override
  public void offer(ByteBuffer buffer)
      throws ParseException, UnsupportedHttpVersion, UnsupportedHttpMethod,
          HttpRequestTargetTooLong {
    ensureValid();

    while (buffer.hasRemaining()) {
      if (state == DONE) {
        return;
      }

      byte nextByte = buffer.get();

      switch (nextByte) {
        case ' ':
          switch (state) {
            case PARSING_METHOD:
              final String httpMethod = byteArrayOutputStream.toString();
              if (httpMethod.isEmpty()) {
                throwParseException("Empty method");
              }

              method =
                  HttpMethod.fromMethodString(httpMethod)
                      .orElseThrow(
                          () -> {
                            state = ERROR;
                            return new UnsupportedHttpMethod(httpMethod);
                          });
              break;
            case PARSING_REQUEST_TARGET:
              requestTarget = byteArrayOutputStream.toString();
              if (requestTarget.isEmpty()) {
                throwParseException("Empty request target");
              }
              break;
            default:
              throwParseException("Unexpected space");
          }
          byteArrayOutputStream.reset();
          state = state.next();
          break;
        case '\r':
          if (state != PARSING_VERSION) {
            throwParseException("Unexpected carriage return");
          } else {
            final String httpVersion = byteArrayOutputStream.toString();
            if (httpVersion.isEmpty()) {
              throwParseException("Empty version");
            }

            version =
                HttpVersion.fromVersionString(httpVersion)
                    .orElseThrow(
                        () -> {
                          state = ERROR;
                          return new UnsupportedHttpVersion(httpVersion);
                        });
            state = state.next();
          }
          break;
        case '\n':
          if (state != AWAITING_LINE_FEED) {
            throwParseException("Unexpected line feed");
          } else {
            state = state.next();
          }
          break;
        default:
          switch (state) {
            case PARSING_METHOD:
              byteArrayOutputStream.write(nextByte);
              if (byteArrayOutputStream.size() > HttpMethod.maxLength) {
                final String unsupportedMethod = byteArrayOutputStream.toString();
                state = ERROR;
                throw new UnsupportedHttpMethod(unsupportedMethod);
              }
              break;
            case PARSING_REQUEST_TARGET:
              byteArrayOutputStream.write(nextByte);
              if (maxRequestTargetLength > 0
                  && byteArrayOutputStream.size() > maxRequestTargetLength) {
                state = ERROR;
                throw new HttpRequestTargetTooLong(maxRequestTargetLength);
              }
              break;
            case PARSING_VERSION:
              byteArrayOutputStream.write(nextByte);
              if (byteArrayOutputStream.size() > HttpVersion.maxLength) {
                final String unsupportedVersion = byteArrayOutputStream.toString();
                state = ERROR;
                throw new UnsupportedHttpVersion(unsupportedVersion);
              }
              break;
            default:
              throwParseException(String.format("Unexpected character %c", nextByte));
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
    state = PARSING_METHOD;
    byteArrayOutputStream.reset();
    method = null;
    requestTarget = null;
    version = null;
  }

  HttpMethod getMethod() {
    ensureValid();
    return method;
  }

  String getRequestTarget() {
    ensureValid();
    return requestTarget;
  }

  HttpVersion getVersion() {
    ensureValid();
    return version;
  }
}
