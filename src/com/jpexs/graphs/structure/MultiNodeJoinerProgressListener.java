package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.MultiNode;
import com.jpexs.graphs.structure.nodes.EditableNode;
import com.jpexs.graphs.structure.nodes.EditableMultiNode;

/**
 *
 * @author JPEXS
 */
public interface MultiNodeJoinerProgressListener<T extends EditableNode> {

    public void multiNodeJoined(EditableMultiNode node);

    public void step();

}
