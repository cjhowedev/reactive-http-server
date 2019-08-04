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

enum RequestLineParserState {
  PARSING_METHOD("while parsing the HTTP method"),
  PARSING_REQUEST_TARGET("while parsing the HTTP request target"),
  PARSING_VERSION("while parsing the HTTP version"),
  AWAITING_LINE_FEED("after parsing the first carriage return"),
  DONE("after parsing the final line feed"),
  ERROR("after an error occured");

  private final String friendlyStatusReport;

  RequestLineParserState(String friendlyStatusReport) {
    this.friendlyStatusReport = friendlyStatusReport;
  }

  RequestLineParserState next() {
    switch (this) {
      case PARSING_METHOD:
        return PARSING_REQUEST_TARGET;
      case PARSING_REQUEST_TARGET:
        return PARSING_VERSION;
      case PARSING_VERSION:
        return AWAITING_LINE_FEED;
      case AWAITING_LINE_FEED:
        return DONE;
      default:
        return ERROR;
    }
  }

  String friendlyStatusReport() {
    return this.friendlyStatusReport;
  }
}
