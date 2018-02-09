package com.jpexs.graphs.structure.factories.operations;

import com.jpexs.graphs.structure.nodes.EditableMultiNode;
import com.jpexs.graphs.structure.nodes.EditableNode;

/**
 *
 * @author JPEXS
 */
public interface MultiNodeJoinerProgressListener<T extends EditableNode> {

    public void multiNodeJoined(EditableMultiNode node);

    public void step();

}
