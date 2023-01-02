/*
 *  Copyright 2023 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package dev.morling.demos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockingQueueExample {

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService workerExecutor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(20);

    public static void main(String[] args) throws Exception {
        System.out.println(new File(".").toPath().toAbsolutePath());
        new BlockingQueueExample().run();
    }

    private void run() throws Exception {

        Runnable workerA = new Runnable() {

            private int scheduled = 0;

            @Override
            public void run() {
                try {
                    queue.put("A");
                    System.out.println("Scheduled: " + scheduled++);
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        };

        scheduler.scheduleAtFixedRate(workerA, 0, 50, TimeUnit.MILLISECONDS);

        workerExecutor.execute(() -> {
            while (running.get()) {
                List<String> work = new ArrayList<>();
                queue.drainTo(work, 10);
                System.out.println("Processing " + (work.size() + " items"));
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                    return;
                }
            }
        });

        Thread.sleep(5000);

        running.set(false);

        workerExecutor.shutdownNow();
        scheduler.shutdownNow();
    }
}
