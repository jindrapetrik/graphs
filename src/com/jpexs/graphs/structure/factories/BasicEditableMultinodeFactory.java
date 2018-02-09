package com.jpexs.graphs.structure.factories;

import com.jpexs.graphs.structure.BasicEditableMultiNode;
import com.jpexs.graphs.structure.nodes.EditableMultiNode;

/**
 *
 * @author JPEXS
 */
public class BasicEditableMultinodeFactory implements EditableMultinodeFactory {

    @Override
    public EditableMultiNode create(String id) {
        return new BasicEditableMultiNode(id);
    }

}
