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

public class HttpHeaderNameTooLong extends Exception {
  private final int maxHeaderNameLength;

  HttpHeaderNameTooLong(final int maxHeaderNameLength) {
    super(
        String.format(
            "Header field name was longer than maximum length of %d", maxHeaderNameLength));
    this.maxHeaderNameLength = maxHeaderNameLength;
  }

  public int getMaxHeaderNameLength() {
    return maxHeaderNameLength;
  }
}
