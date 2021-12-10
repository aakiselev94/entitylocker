package ru.akiselev.entitylocker;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityLockerTest {

    @Test
    public void lock() {
        var entityLocker = new ConcurrentMapEntityLocker<Integer>();
        entityLocker.unlock(1);
    }

    @Test
    public void lockByTimeout() throws InterruptedException, ExecutionException {
        var entityLocker = new ConcurrentMapEntityLocker<Integer>();
        var executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = executor.submit(() -> {
            entityLocker.lock(1, 1000L);
            entityLocker.unlock(1);
            return true;
        });
        future.get();

        assertThat(future.isDone()).isTrue();
        assertThat(future.get()).isTrue();
    }

    @Test
    public void reentrantLock() throws InterruptedException, ExecutionException {
        var entityLocker = new ConcurrentMapEntityLocker<Integer>();
        var executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = executor.submit(() -> {
            entityLocker.lock(1, 1000L);
            entityLocker.lock(1, 1000L);
            entityLocker.unlock(1);
            entityLocker.unlock(1);
            return true;
        });
        future.get();

        assertThat(future.isDone()).isTrue();
        assertThat(future.get()).isTrue();
    }

}
