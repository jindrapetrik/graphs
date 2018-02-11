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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AttributesMap {

    private Map<DotId, DotId> values = new LinkedHashMap<>();

    public AttributesMap(AttributesMap source) {
        values.putAll(source.values);
    }

    public AttributesMap(Map<String, String> source) {
        for (Map.Entry<String, String> entry : source.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public AttributesMap() {
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean containsKey(DotId key) {
        return values.containsKey(key);
    }

    public boolean containsKey(String key) {
        return containsKey(new DotId(key, false));
    }

    public boolean containsValue(DotId value) {
        return values.containsValue(value);
    }

    public DotId get(DotId key) {
        return values.get(key);
    }

    public String get(String key) {
        DotId ret = values.get(new DotId(key, false));
        if (ret == null) {
            return null;
        }
        return ret.toString();
    }

    public DotId put(DotId key, DotId value) {
        return values.put(key, value);
    }

    public DotId put(String key, String value) {
        return values.put(new DotId(key, false), new DotId(value, false));
    }

    public DotId remove(DotId key) {
        return values.remove(key);
    }

    public String remove(String key) {
        DotId ret = values.remove(new DotId(key, false));
        if (ret == null) {
            return null;
        }
        return ret.toString();
    }

    public void putAll(Map<? extends DotId, ? extends DotId> m) {
        values.putAll(m);
    }

    public void putAll(AttributesMap a) {
        values.putAll(a.values);
    }

    public void clear() {
        values.clear();
    }

    public Set<DotId> keySet() {
        return values.keySet();
    }

    public Set<String> stringKeySet() {
        Set<String> ret = new LinkedHashSet<>();
        for (DotId value : values.keySet()) {
            ret.add(value.toString());
        }
        return ret;
    }

    public Collection<DotId> values() {
        return values.values();
    }

    public Collection<String> stringValues() {
        List<String> ret = new ArrayList<>();
        for (DotId value : values.values()) {
            ret.add(value.toString());
        }
        return ret;
    }

    public Set<Map.Entry<DotId, DotId>> entrySet() {
        return values.entrySet();
    }

    public Set<Map.Entry<String, String>> stringEntrySet() {
        Set<Map.Entry<String, String>> ret = new LinkedHashSet<>();
        for (Map.Entry<DotId, DotId> entry : values.entrySet()) {
            final String stringKey = entry.getKey().toString();
            ret.add(new Map.Entry<String, String>() {
                @Override
                public String getKey() {
                    return stringKey;
                }

                @Override
                public String getValue() {
                    return get(stringKey);
                }

                @Override
                public String setValue(String value) {
                    String oldValue = get(stringKey);
                    put(stringKey, value);
                    return oldValue;
                }
            });
        }
        return ret;
    }

    public AttributesMap clone() {
        return new AttributesMap(this);
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
}
