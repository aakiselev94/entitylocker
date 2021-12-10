package ru.akiselev.entitylocker;

public interface EntityLocker<ID> {

    void lock(final ID entityId);

    boolean lock(final ID entityId, long timestamp);

    void unlock(final ID entityId);

}
