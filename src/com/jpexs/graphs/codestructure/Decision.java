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

import com.jpexs.graphs.codestructure.nodes.Node;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class Decision<T extends Node> {

    private T ifNode;
    private int branchNum;

    public Decision(T ifNode, int branchNum) {
        this.ifNode = ifNode;
        this.branchNum = branchNum;
    }

    public int getBranchNum() {
        return branchNum;
    }

    public T getIfNode() {
        return ifNode;
    }

    @Override
    public String toString() {
        return ifNode.toString() + "|" + branchNum;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.ifNode);
        hash = 47 * hash + this.branchNum;
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
        final Decision other = (Decision) obj;
        if (!Objects.equals(this.ifNode, other.ifNode)) {
            return false;
        }
        if (this.branchNum != other.branchNum) {
            return false;
        }
        return true;
    }

}
