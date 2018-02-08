package com.jpexs.graphs.structure;

import com.jpexs.graphs.structure.nodes.MutableNode;

/**
 *
 * @author Jindra
 */
public interface CodeStructureChangerProgressListener<T extends MutableNode> extends CodeStructureDetectorProgressListener<T>, MultiNodeJoinerProgressListener<T>, EnfIfNodeInjectorProgressListener {

}
