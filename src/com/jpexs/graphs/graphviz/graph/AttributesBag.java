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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AttributesBag extends LinkedHashMap<String, String> {

    public AttributesBag(AttributesBag source) {
        putAll(source);
    }

    public AttributesBag() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!isEmpty()) {
            sb.append("[");
            List<String> keys = new ArrayList<>(keySet());
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = this.get(key);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(Serializer.serializeId(key));
                sb.append("=");
                sb.append(Serializer.serializeId(value));
            }
            sb.append("]");
        }
        return sb.toString();
    }

    @Override
    public AttributesBag clone() {
        super.clone();
        return new AttributesBag(this);
    }

}
