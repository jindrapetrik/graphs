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
package com.jpexs.graphs.graphviz.graph;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class Serializer {

    final static String[] RESERVED_WORDS = new String[]{"node", "edge", "graph", "digraph", "subgraph", "strict"};
    final static Pattern RESERVED_PATTERN = Pattern.compile("^" + String.join("|", RESERVED_WORDS) + "$", Pattern.CASE_INSENSITIVE);
    final static Pattern NUMERAL_PATTERN = Pattern.compile("^[-]?(.[0-9]+ | [0-9]+(.[0-9]*)?)$");
    final static String IDENTIFIER_FIRST_CHARS = "a-zA-Z\\u0200-\\u0377";
    final static String IDENTIFIER_NEXT_CHARS = IDENTIFIER_FIRST_CHARS + "0-9";
    final static Pattern IDENTIFIER_PATTERN = Pattern.compile("^[" + IDENTIFIER_FIRST_CHARS + "][" + IDENTIFIER_NEXT_CHARS + "]*$");

    public static String serializeId(String id) {
        if (Arrays.asList(RESERVED_WORDS).contains(id)) {
            return "\"" + id + "\"";
        }

        if (NUMERAL_PATTERN.matcher(id).matches()) {
            return id;
        }
        if (IDENTIFIER_PATTERN.matcher(id).matches()) {
            return id;
        }
        return "\"" + id.replace("\"", "\\\"") + "\"";
    }
}
