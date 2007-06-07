package com.enea.jcarder.analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * A LockNode instance represents a lock in a graph.
 */
@NotThreadSafe
class LockNode {
    enum CycleType { NO_CYCLE, SINGLE_THREADED_CYCLE, CYCLE };

    private final int mLockId;
    private Map<LockEdge, LockEdge> mOutgoingEdges;
    private CycleType mCycleType = CycleType.NO_CYCLE;

    LockNode(final int lockId) {
        mLockId = lockId;
        mOutgoingEdges = new HashMap<LockEdge, LockEdge>();
    }

    int getLockId() {
        return mLockId;
    }

    CycleType getCycleType() {
        return mCycleType;
    }

    void raiseCycleType(CycleType newCycleype) {
        if (newCycleype.compareTo(mCycleType) > 0) {
            mCycleType = newCycleype;
        }
    }

    void addOutgoingEdge(LockEdge newEdge) {
        LockEdge existingEdge = mOutgoingEdges.get(newEdge);
        if (existingEdge == null) {
            mOutgoingEdges.put(newEdge, newEdge);
        } else {
            existingEdge.merge(newEdge);
        }
    }

    void populateContextIdTranslationMap(Map<Integer, Integer> translationMap) {
        for (LockEdge edge : mOutgoingEdges.values()) {
            translationMap.put(edge.getSourceLockingContextId(),
                               edge.getSourceLockingContextId());
            translationMap.put(edge.getTargetLockingContextId(),
                               edge.getTargetLockingContextId());
        }
    }

    void translateContextIds(Map<Integer, Integer> translation) {
        Map<LockEdge, LockEdge> oldEdges = mOutgoingEdges;
        mOutgoingEdges = new HashMap<LockEdge, LockEdge>(oldEdges.size());
        for (LockEdge edge : oldEdges.values()) {
            edge.translateContextIds(translation);
            addOutgoingEdge(edge);
        }
    }

    Set<LockEdge> getOutgoingEdges() {
        return mOutgoingEdges.keySet();
    }

    @Override
    public String toString() {
        return "L_" + mLockId;
    }

    long numberOfUniqueEdges() {
        return mOutgoingEdges.size();
    }

    long numberOfDuplicatedEdges() {
        long numberOfDuplicatedEdges = 0;
        for (LockEdge edge : mOutgoingEdges.values()) {
            numberOfDuplicatedEdges += edge.getDuplicates();
        }
        return numberOfDuplicatedEdges;
    }

    boolean alike(LockNode other, ContextReaderIfc ras) {
        // TODO Maybe introduce some kind of cache to improve performance?
        String thisClassName = ras.readLock(mLockId).getClassName();
        String otherClassName = ras.readLock(other.mLockId).getClassName();
        return thisClassName.equals(otherClassName);
    }
}
