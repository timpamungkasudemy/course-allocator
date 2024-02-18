package com.course.allocator.api;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
public class AllocatorController {

    @SuppressWarnings("unused")
    private byte[] data;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/allocate/memory")
    public String allocateMemory(@RequestParam("memory") int memoryMB, @RequestParam("duration") int durationSec) {
        // Allocate memory
        var allocateThisMany = memoryMB * 1024 * 1024;
        final var recalculatedMemoryMB = (int) (allocateThisMany / (1024 * 1024));
        if (allocateThisMany < 1) {
            allocateThisMany = Integer.MAX_VALUE;
            memoryMB = Integer.MAX_VALUE / (1024 * 1024);
        }

        data = new byte[allocateThisMany];

        // Schedule a task to free the memory after the specified duration
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(recalculatedMemoryMB + " MB memory freed after " + durationSec + " seconds");
                data = null;
                System.gc();
            }
        }, durationSec * 1000); // Convert seconds to milliseconds

        executorService.submit(() -> {
            for (int counter = durationSec; counter > 0; counter--) {
                System.out.println("Allocated " +
                        recalculatedMemoryMB + " MB of memory for "
                        + counter + " seconds");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        return "Allocated " + recalculatedMemoryMB + " MB of memory. Will be freed after "
                + durationSec + " seconds";
    }

    @GetMapping("/allocate/cpu")
    public String allocateCpu(@RequestParam(name = "cpu", required = false, defaultValue = "0") int numThreads,
            @RequestParam("duration") int durationSec)
            throws InterruptedException {
        final int numThreadsForCalculation = numThreads == 0 ? Runtime.getRuntime().availableProcessors() : numThreads;

        executorService.submit(() -> {
            try {
                performCalculation(numThreadsForCalculation, durationSec);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return "Running process on " + numThreads + " threads for " + durationSec + " seconds";
    }

    private void performCalculation(int numThreads, int durationSec) throws InterruptedException {
        LongAdder counter = new LongAdder();

        List<CalculationThread> runningCalcs = new ArrayList<>();
        List<Thread> runningThreads = new ArrayList<>();

        System.out.printf("Starting %d threads\n", numThreads);

        for (int i = 0; i < numThreads; i++) {
            CalculationThread r = new CalculationThread(counter);
            Thread t = new Thread(r);
            runningCalcs.add(r);
            runningThreads.add(t);
            t.start();
        }

        for (int i = 0; i < durationSec; i++) {
            counter.reset();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }

            System.out.printf("[%d] Calculations per second: %s (%s per thread)\n",
                    i,
                    NumberFormat.getInstance().format(counter.longValue()),
                    NumberFormat.getInstance().format((double) (counter.longValue()) / numThreads));
        }

        for (int i = 0; i < runningCalcs.size(); i++) {
            runningCalcs.get(i).stop();
            runningThreads.get(i).join();
        }
    }

}
