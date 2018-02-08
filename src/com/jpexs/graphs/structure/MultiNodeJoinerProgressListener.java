package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.MultiNode;
import com.jpexs.graphs.structure.nodes.MutableMultiNode;
import com.jpexs.graphs.structure.nodes.MutableNode;

/**
 *
 * @author JPEXS
 */
public interface MultiNodeJoinerProgressListener<T extends MutableNode> {

    public void multiNodeJoined(MutableMultiNode node);

    public void step();

}
