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
package com.jpexs.graphs.structure.nodes;

/**
 *
 * @author JPEXS
 */
public interface EditableNode extends Node {

    public void addNext(Node node);

    public void addNext(int index, Node node);

    public void addPrev(Node node);

    public void addPrev(int index, Node node);

    public void removePrev(Node node);

    public void removeNext(Node node);

    public void setPrev(int index, Node node);

    public void setNext(int index, Node node);
}
