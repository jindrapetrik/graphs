/*
 * Copyright (C) 2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.graphs.graphviz.dot.parser;

/**
 *
 * @author JPEXS
 */
public class DotParsedSymbol {

    public int type;
    public int idtype = IDTYPE_NONE;

    public int line;

    private Object value;

    public static final int IDTYPE_NONE = 0;

    public static final int IDTYPE_IDENTIFIER = 1;
    public static final int IDTYPE_NUMERAL = 2;
    public static final int IDTYPE_DOUBLE_QUOTED = 3;
    public static final int IDTYPE_HTML_STRING = 4;

    public static final int TYPE_KEYWORD_STRICT = 0;
    public static final int TYPE_KEYWORD_GRAPH = 1;
    public static final int TYPE_KEYWORD_DIGRAPH = 2;
    public static final int TYPE_KEYWORD_NODE = 3;
    public static final int TYPE_KEYWORD_EDGE = 4;
    public static final int TYPE_KEYWORD_SUBGRAPH = 5;
    public static final int TYPE_SEMICOLON = 6;
    public static final int TYPE_COMMA = 7;
    public static final int TYPE_BRACE_OPEN = 8;
    public static final int TYPE_BRACE_CLOSE = 9;
    public static final int TYPE_EQUAL = 10;
    public static final int TYPE_BRACKET_OPEN = 11;
    public static final int TYPE_BRACKET_CLOSE = 12;
    public static final int TYPE_COLON = 13;
    public static final int TYPE_MINUSMINUS = 14;
    public static final int TYPE_ARROW = 15;

    public static final int TYPE_NUMERAL = 16;
    public static final int TYPE_ID = 17;

    public static final int TYPE_EOF = 18;
    public static final int TYPE_INVALID_SYMBOL = -1;

    public DotParsedSymbol(int line, int type, Object value) {
        this.line = line;
        this.type = type;
        this.value = value;
    }

    public DotParsedSymbol(int line, int type, int idtype, Object value) {
        this.line = line;
        this.type = type;
        this.value = value;
        this.idtype = idtype;
    }

    public DotParsedSymbol(int line, int type) {
        this.line = line;
        this.type = type;
    }

    public String getValueAsString() {
        return "" + value;
    }
}
