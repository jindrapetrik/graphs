package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.EditableNode;

/**
 *
 * @author JPEXS
 */
public interface CodeStructureChangerProgressListener<T extends EditableNode> extends CodeStructureDetectorProgressListener<T>, MultiNodeJoinerProgressListener<T>, EnfIfNodeInjectorProgressListener {

}
