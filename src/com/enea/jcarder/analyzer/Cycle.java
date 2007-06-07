package com.enea.jcarder.analyzer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * An instance of this class represents a single cycle of edges. The edges
 * must form a single circle witout any alternative paths. If there are
 * alternative paths in a graph-cycle, those cycles shall be splitted into
 * separate Cycle objects.
 *
 * TODO write basic tests.
 */
class Cycle {
    final HashSet<LockEdge> mEdgesInCycle = new HashSet<LockEdge>();

    Cycle(Collection<LockEdge> edgesInTheCycle) {
        mEdgesInCycle.addAll(edgesInTheCycle);
        assert mEdgesInCycle.size() >= 2;
    }

    HashSet<LockEdge> getEdges() {
        return mEdgesInCycle;
    }

    HashSet<LockNode> getNodes() {
        HashSet<LockNode> nodes = new HashSet<LockNode>();
        for (LockEdge edge : mEdgesInCycle) {
            // All sources will be included if we get all the targets, since
            // it is a cycle.
            nodes.add(edge.getTarget());
        }
        return nodes;
    }

    void updateNodeCycleStatus() {
        final LockNode.CycleType type;
        if (isSingleThreaded()) {
            type = LockNode.CycleType.SINGLE_THREADED_CYCLE;
        } else {
            type = LockNode.CycleType.CYCLE;
        }
        for (LockEdge edge : mEdgesInCycle) {
            edge.getSource().raiseCycleType(type);
            edge.getTarget().raiseCycleType(type);
        }
    }

    boolean isSingleThreaded() {
        // TODO Cache the result to improve performance?
        final Iterator<LockEdge> iter = mEdgesInCycle.iterator();
        if (iter.hasNext()) {
            final long firstThreadId = iter.next().getThreadId();
            while (iter.hasNext()) {
                if (firstThreadId != iter.next().getThreadId()) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean alike(Cycle other, ContextReaderIfc ras) {
        if (this.equals(other)) {
            return true;
        }
        if (mEdgesInCycle.size() != other.mEdgesInCycle.size()) {
            return false;
        }
        if (isSingleThreaded() != other.isSingleThreaded()) {
            return false;
        }
        LinkedList<LockEdge> otherEdges = new LinkedList<LockEdge>(other.mEdgesInCycle);
        // TODO Refactor the following code?
        outerLoop:
        for (LockEdge edge : mEdgesInCycle) {
            Iterator<LockEdge> iter = otherEdges.iterator();
            while (iter.hasNext()) {
                if (edge.alike(iter.next(), ras)) {
                    iter.remove();
                    continue outerLoop;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            Cycle other = (Cycle) obj;
            return mEdgesInCycle.equals(other.mEdgesInCycle);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mEdgesInCycle.hashCode();
    }


    @Override
    public String toString() {
        return mEdgesInCycle.toString();
    }
}
