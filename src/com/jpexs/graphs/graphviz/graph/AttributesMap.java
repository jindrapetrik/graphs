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

import com.jpexs.graphs.graphviz.dot.parser.DotId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AttributesMap extends LinkedHashMap<DotId, DotId> {

    public AttributesMap(AttributesMap source) {
        putAll(source);
    }

    public AttributesMap() {
    }

    public void put(String key, DotId value) {
        put(new DotId(key, false), value);
    }

    public void put(String key, String value) {
        put(new DotId(key, false), new DotId(value, false));
    }

    @Override
    public DotId put(DotId key, DotId value) {
        if (value == null) {
            throw new NullPointerException("Cannot set null as value for key " + key);
        }
        return super.put(key, value);
    }

    public DotId remove(String key) {
        return remove(new DotId(key, false));
    }

    public boolean remove(String key, String value) {
        return remove(new DotId(key, false), new DotId(value, false));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!isEmpty()) {
            sb.append("[");
            List<DotId> keys = new ArrayList<>(keySet());
            for (int i = 0; i < keys.size(); i++) {
                DotId key = keys.get(i);
                DotId value = this.get(key);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(key.toString());
                sb.append("=");
                sb.append(value.toString());
            }
            sb.append("]");
        }
        return sb.toString();
    }

    @Override
    public AttributesMap clone() {
        super.clone();
        return new AttributesMap(this);
    }

    public boolean containsKey(String key) {
        return containsKey(new DotId(key, false));
    }

    public boolean containsValue(String value) {
        return containsValue(new DotId(value, false));
    }

}
