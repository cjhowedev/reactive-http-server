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
  private final int maxHeaderKeySize;
  private final int maxHeaderValueSize;
  private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
  private HttpHeaderParserState state = PARSING_KEY;
  private String key = null;
  private String value = null;

  public HttpHeaderParser(final int maxHeaderKeySize, final int maxHeaderValueSize) {
    this.maxHeaderKeySize = maxHeaderKeySize;
    this.maxHeaderValueSize = maxHeaderValueSize;
  }

  @Override
  public void offer(ByteBuffer buffer) {}

  @Override
  public boolean isValid() {
    return state == ERROR;
  }

  @Override
  public boolean isDone() {
    return state == DONE;
  }

  @Override
  public void reset() {
    state = PARSING_KEY;
    byteArrayOutputStream.reset();
    key = null;
    value = null;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
