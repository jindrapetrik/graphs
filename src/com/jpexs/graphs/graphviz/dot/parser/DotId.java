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

import com.jpexs.graphs.graphviz.dot.parser.DotLexer;
import com.jpexs.graphs.graphviz.dot.parser.DotParseException;
import com.jpexs.graphs.graphviz.dot.parser.DotParsedSymbol;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class DotId {

    final static String[] RESERVED_WORDS = new String[]{"node", "edge", "graph", "digraph", "subgraph", "strict"};
    final static Pattern RESERVED_PATTERN = Pattern.compile("^" + String.join("|", RESERVED_WORDS) + "$", Pattern.CASE_INSENSITIVE);
    final static Pattern NUMERAL_PATTERN = Pattern.compile("^[-]?(.[0-9]+ | [0-9]+(.[0-9]*)?)$");
    final static String IDENTIFIER_FIRST_CHARS = "a-zA-Z\\u0200-\\u0377";
    final static String IDENTIFIER_NEXT_CHARS = IDENTIFIER_FIRST_CHARS + "0-9";
    final static Pattern IDENTIFIER_PATTERN = Pattern.compile("^[" + IDENTIFIER_FIRST_CHARS + "][" + IDENTIFIER_NEXT_CHARS + "]*$");
    final static Pattern HTML_PATTERN = Pattern.compile("^<.+>$");

    private String value;
    private boolean isHtml;

    public DotId(String value, boolean isHtml) {
        this.value = value;
        this.isHtml = isHtml;
    }

    public static DotId fromString(String id) {
        DotLexer lex = new DotLexer(new StringReader(id));
        try {
            DotParsedSymbol symbol = lex.lex();
            if (symbol.type != DotParsedSymbol.TYPE_ID) {
                return null;
            }
            boolean isHtml = false;
            if (symbol.idtype == DotParsedSymbol.IDTYPE_HTML_STRING) {
                isHtml = true;
            }
            return new DotId(symbol.getValueAsString(), isHtml);
        } catch (IOException | DotParseException ex) {
            return null;  //TODO: maybe throw an exception?
        }
    }

    @Override
    public String toString() {
        if (isHtml) {
            return "<" + value + ">";
        }
        if (RESERVED_PATTERN.matcher(value).matches()) {
            return "\"" + value + "\"";
        }

        if (NUMERAL_PATTERN.matcher(value).matches()) {
            return value;
        }
        if (IDENTIFIER_PATTERN.matcher(value).matches()) {
            return value;
        }
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.value);
        hash = 89 * hash + (this.isHtml ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DotId other = (DotId) obj;
        if (this.isHtml != other.isHtml) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
