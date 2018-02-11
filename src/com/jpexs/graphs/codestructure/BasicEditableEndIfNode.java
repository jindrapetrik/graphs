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
package com.jpexs.graphs.codestructure;

import com.jpexs.graphs.codestructure.nodes.EditableEndIfNode;
import com.jpexs.graphs.codestructure.nodes.Node;

/**
 *
 * @author JPEXS
 */
public class BasicEditableEndIfNode extends BasicEditableNode implements EditableEndIfNode {

    private Node ifNode;

    public BasicEditableEndIfNode(Node ifNode) {
        super(ENDIF_ID_PREFIX + ifNode.getId());
        this.ifNode = ifNode;
    }

    @Override
    public Node getIfNode() {
        return ifNode;
    }

    @Override
    public String getIdPrefix() {
        return ENDIF_ID_PREFIX;
    }

    @Override
    public String getOriginalId() {
        return ifNode.getId();
    }

}
