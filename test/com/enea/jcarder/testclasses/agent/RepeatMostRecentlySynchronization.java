package com.enea.jcarder.testclasses.agent;

import com.enea.jcarder.agent.LockEvent;
import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class RepeatMostRecentlySynchronization implements SynchronizationTestIfc {
    private final Object mSync0 = new Object();
    private final Object mSync1 = new Object();

    public void go() {
        synchronized (mSync0) {
            assertFalse(Thread.holdsLock(mSync1));
            synchronized (mSync1) {
                assertTrue(Thread.holdsLock(mSync1));
                // The following synchronization should be ignored.
                // There is no point in adding an additional
                // mSync0 -> mSync1 or a mSync1 -> mSync1 transition.
                synchronized (mSync1) {
                    assertTrue(Thread.holdsLock(mSync1));
                }
            }
        }
    }

    public LockEvent[] getExpectedLockEvents() {
        final Lock lockSync0 = new Lock(mSync0);
        final Lock lockSync1 = new Lock(mSync1);
        final String threadName = Thread.currentThread().getName();
        final String method = getClass().getName() + ".go()";
        LockingContext contextSync0 = new LockingContext(threadName,
                                                         getClass().getName() + ".mSync0",
                                                         method);
        LockingContext contextSync1 = new LockingContext(threadName,
                                                         getClass().getName() + ".mSync1",
                                                         method);
        return new LockEvent[] {
                new LockEvent(lockSync1, contextSync1, lockSync0, contextSync0)
        };
    }
}