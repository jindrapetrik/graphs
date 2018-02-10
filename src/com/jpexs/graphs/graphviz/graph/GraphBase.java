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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class GraphBase implements IdAware {

    protected boolean directed;
    public String id = null;
    public AttributesMap graphAttributes = new AttributesMap();
    public AttributesMap nodeAttributes = new AttributesMap();
    public AttributesMap edgeAttributes = new AttributesMap();
    public List<NodeIdToAttributes> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    public GraphBase(boolean directed) {
        this.directed = directed;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean hasId() {
        return id != null;
    }

    protected String bodyToString() {
        String NEWLINE = "\r\n";
        StringBuilder sb = new StringBuilder();
        if (hasId()) {
            sb.append(Serializer.serializeId(id));
        }
        sb.append("{");

        StringBuilder esb = new StringBuilder();
        Set<NodeId> nodesInEdges = new LinkedHashSet<>();
        for (Edge edge : edges) {
            if (edge.from instanceof NodeId) {
                nodesInEdges.add((NodeId) edge.from);
            }
            if (edge.to instanceof NodeId) {
                nodesInEdges.add((NodeId) edge.to);
            }
            if (esb.length() > 0) {
                esb.append(";").append(NEWLINE);
            }
            esb.append(edge);
        }
        StringBuilder nsb = new StringBuilder();
        for (NodeIdToAttributes entry : nodes) {
            if (nsb.length() > 0) {
                nsb.append(";").append(NEWLINE);
            }
            nsb.append(entry.toString());
        }

        sb.append(nsb);
        if (esb.length() > 0) {
            if (nsb.length() > 0) {
                sb.append(";").append(NEWLINE);
            }
            sb.append(esb);
        }
        sb.append("}");
        return sb.toString();
    }
}
