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
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class NodeId implements ConnectableObject {

    private final DotId id;
    public String portId;
    public String compassPt;

    public NodeId(DotId id) {
        this.id = id;
    }

    @Override
    public DotId getId() {
        return id;
    }

    @Override
    public boolean hasId() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id.toString());
        if (portId != null) {
            sb.append(":").append(portId);
        }
        if (compassPt != null) {
            sb.append(":").append(compassPt);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.portId);
        hash = 97 * hash + Objects.hashCode(this.compassPt);
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
        final NodeId other = (NodeId) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.portId, other.portId)) {
            return false;
        }
        if (!Objects.equals(this.compassPt, other.compassPt)) {
            return false;
        }
        return true;
    }

}
