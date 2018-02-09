package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.EditableNode;

/**
 *
 * @author Jindra
 */
public interface CodeStructureChangerProgressListener<T extends EditableNode> extends CodeStructureDetectorProgressListener<T>, MultiNodeJoinerProgressListener<T>, EnfIfNodeInjectorProgressListener {

}
