package ru.akiselev.entitylocker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentMapEntityLocker<ID> implements EntityLocker<ID> {

    private final ConcurrentMap<ID, ReentrantLock> locks;

    public ConcurrentMapEntityLocker() {
        locks = new ConcurrentHashMap<>();
    }

    @Override
    public void lock(final ID entityId) {
        lock(entityId, -1);
    }

    @Override
    public boolean lock(final ID entityId, long timestamp) {
        try {
            long timeToLockIn = System.currentTimeMillis() + timestamp;
            boolean lockCatched = false;
            while (!lockCatched) {
                ReentrantLock lock = getLockById(entityId);
                if (timestamp < 0) {
                    lock.lock();
                } else {
                    if (!lock.tryLock(timeToLockIn - System.currentTimeMillis(), TimeUnit.NANOSECONDS)) {
                        return false;
                    }
                }
                if (lock == getLockById(entityId)) {
                    lockCatched = true;
                } else {
                    lock.unlock();
                }
            }
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException("Can't catch a lock.", e);
        }
    }

    private ReentrantLock getLockById(final ID entityId) {
        return locks.computeIfAbsent(entityId, (id) -> new ReentrantLock());
    }

    @Override
    public void unlock(final ID entityId) {
        ReentrantLock lock = locks.get(entityId);
        if (lock != null) {
            if (!lock.isHeldByCurrentThread()) {
                throw new IllegalMonitorStateException("Current thread doesn't hold the lock.");
            }
            if (!lock.hasQueuedThreads()) {
                locks.remove(entityId);
            }
            lock.unlock();
        }
    }
}
