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

enum HttpHeaderParserState {
  PARSING_NAME("while parsing a header field name"),
  PARSING_VALUE("while parsing a header field value"),
  AWAITING_LINE_FEED("after parsing the first carriage return"),
  DONE("after parsing the final line feed"),
  ERROR("after an error occured");

  private final String friendlyStatusReport;

  HttpHeaderParserState(String friendlyStatusReport) {
    this.friendlyStatusReport = friendlyStatusReport;
  }

  HttpHeaderParserState next() {
    switch (this) {
      case PARSING_NAME:
        return PARSING_VALUE;
      case PARSING_VALUE:
        return AWAITING_LINE_FEED;
      case AWAITING_LINE_FEED:
        return DONE;
      default:
        return ERROR;
    }
  }

  String getFriendlyStatusReport() {
    return this.friendlyStatusReport;
  }
}
