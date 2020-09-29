package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class RootActor extends AbstractBehavior<RootActor.Command> {

    private final Logger logger;

    public interface Command {
    }

    @AllArgsConstructor
    public static class HandleEnqueueing implements Command {
        private final int n;
        private final CountDownLatch finish;
    }

    @AllArgsConstructor
    public static class HandleDequeueing implements Command {
        private final int n;
        private final CountDownLatch finish;
    }

    @AllArgsConstructor
    public static class HandleInitiation implements Command {
        private final int n;
        private final CountDownLatch finish;
    }

    @AllArgsConstructor
    public static class HandleSingleProducerSending implements Command {
        private final int n;
        private final CountDownLatch finish;
    }

    @AllArgsConstructor
    public static class HandleMultiProducerSending implements Command {
        private final int n;
        private final CountDownLatch finish;
    }

    @AllArgsConstructor
    public static class HandleMaxThroughput implements Command {
        private final int n;
        private final CountDownLatch finish;
    }

    private RootActor(ActorContext<Command> context) {
        super(context);
        logger = getContext().getLog();
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(RootActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(HandleEnqueueing.class, this::onHandleEnqueueing)
                .onMessage(HandleDequeueing.class, this::onHandleDequeueing)
                .onMessage(HandleInitiation.class, this::onHandleInitiation)
                .onMessage(HandleMultiProducerSending.class, this::onHandleMultiProducerSending)
                .onMessage(HandleSingleProducerSending.class, this::onHandleSingleProducerSending)
                .onMessage(HandleMaxThroughput.class, this::onHandleMaxThroughput)
                .build();
    }

    private Behavior<Command> onHandleMaxThroughput(HandleMaxThroughput handleMaxThroughput) throws BrokenBarrierException, InterruptedException {
        int parallelism = 10;
        int n = roundToParallelism(handleMaxThroughput.n, parallelism);
        CountDownLatch finishLatch = new CountDownLatch(parallelism);

        CyclicBarrier barrier = new CyclicBarrier(parallelism + 1);
        ArrayList<Thread> threads = new ArrayList<>(parallelism);
        CountActor.EmptyMessage emptyMessage = new CountActor.EmptyMessage();
        int times = n / parallelism;
        for (int i = 0; i < parallelism; i++) {
            ActorRef<CountActor.Command> actor = getContext().spawnAnonymous(CountActor.create(finishLatch, times));
            Thread thread = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception e) {
                    logger.error(e.toString());
                }
                for (int j = 0; j < times; j++) {
                    actor.tell(emptyMessage);
                }
            });
            thread.start();
            threads.add(thread);
        }

        long start = System.nanoTime();
        barrier.await();
        finishLatch.await();
        long spentTime = System.nanoTime() - start;

        System.out.println("Max throughput:");
        System.out.printf("\t%d ops\n", n);
        System.out.printf("\t%d ns\n", spentTime);
        System.out.printf("\t%d ops/s\n", n * 1000_000_000L / spentTime);
        handleMaxThroughput.finish.countDown();
        return this;
    }

    private int roundToParallelism(int n, int parallelism) {
        return (n / parallelism) * parallelism;
    }

    private Behavior<Command> onHandleMultiProducerSending(HandleMultiProducerSending handleMultiProducerSending) throws BrokenBarrierException, InterruptedException {
        CountDownLatch finishLatch = new CountDownLatch(1);
        int parallelism = 10;
        int n = roundToParallelism(handleMultiProducerSending.n, parallelism);
        ActorRef<CountActor.Command> actor = getContext().spawnAnonymous(CountActor.create(finishLatch, n));
        CyclicBarrier barrier = new CyclicBarrier(parallelism + 1);
        List<Thread> threads = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
            Thread thread = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception e) {
                    logger.error(e.toString());
                }
                int messageCount = n / parallelism;
                CountActor.EmptyMessage emptyMessage = new CountActor.EmptyMessage();
                for (int i1 = 0; i1 < messageCount; i1++) {
                    actor.tell(emptyMessage);
                }
            });
            thread.start();
            threads.add(thread);
        }

        long start = System.nanoTime();
        barrier.await();
        finishLatch.await();
        long spentTime = System.nanoTime() - start;

        System.out.println("Multi-producer sending:");
        System.out.printf("\t%d ops\n", n);
        System.out.printf("\t%d ns\n", spentTime);
        System.out.printf("\t%d ops/s\n", n * 1000_000_000L / spentTime);
        handleMultiProducerSending.finish.countDown();
        return this;
    }

    private Behavior<Command> onHandleSingleProducerSending(HandleSingleProducerSending handleSingleProducerSending) throws InterruptedException {
        CountDownLatch finishLatch = new CountDownLatch(1);
        ActorRef<CountActor.Command> actor = getContext().spawnAnonymous(CountActor.create(finishLatch, handleSingleProducerSending.n));

        long start = System.nanoTime();
        CountActor.EmptyMessage emptyMessage = new CountActor.EmptyMessage();
        for (int i = 0; i < handleSingleProducerSending.n; i++) {
            actor.tell(emptyMessage);
        }
        finishLatch.await();
        long spentTime = System.nanoTime() - start;

        System.out.println("Single-producer sending:");
        System.out.printf("\t%d ops\n", handleSingleProducerSending.n);
        System.out.printf("\t%d ns\n", spentTime);
        System.out.printf("\t%d ops/s\n", handleSingleProducerSending.n * 1000_000_000L / spentTime);
        handleSingleProducerSending.finish.countDown();
        return this;
    }

    private Behavior<Command> onHandleInitiation(HandleInitiation handleInitiation) {
        List<ActorRef<MinimalActor.Command>> actors = new ArrayList<>(handleInitiation.n);

        long start = System.nanoTime();
        for (int i = 0; i < handleInitiation.n; i++) {
            ActorRef<MinimalActor.Command> actorRef = getContext().spawnAnonymous(MinimalActor.create());
            actors.add(actorRef);
        }
        long spentTime = System.nanoTime() - start;

        // tear down
        for (ActorRef<MinimalActor.Command> actor : actors) {
            getContext().stop(actor);
        }

        System.out.println("Initiation:");
        System.out.printf("\t%d ops\n", handleInitiation.n);
        System.out.printf("\t%d ns\n", spentTime);
        System.out.printf("\t%d ops/s\n", handleInitiation.n * 1000_000_000L / spentTime);
        handleInitiation.finish.countDown();
        return this;
    }

    private Behavior<Command> onHandleDequeueing(HandleDequeueing handleDequeueing) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        ActorRef<BlockableCountActor.Command> actor = getContext().spawnAnonymous(BlockableCountActor.create(startLatch, finishLatch, handleDequeueing.n));

        BlockableCountActor.EmptyMessage message = new BlockableCountActor.EmptyMessage();
        for (int i = 0; i < handleDequeueing.n; i++) {
            actor.tell(message);
        }

        long start = System.nanoTime();
        startLatch.countDown();
        finishLatch.await();
        long spentTime = System.nanoTime() - start;

        System.out.println("Dequeueing:");
        System.out.printf("\t%d ops\n", handleDequeueing.n);
        System.out.printf("\t%d ns\n", spentTime);
        System.out.printf("\t%d ops/s\n", handleDequeueing.n * 1000_000_000L / spentTime);
        handleDequeueing.finish.countDown();
        return this;
    }

    private Behavior<Command> onHandleEnqueueing(HandleEnqueueing handleEnqueueing) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        ActorRef<BlockableCountActor.Command> actor = getContext().spawnAnonymous(BlockableCountActor.create(startLatch, finishLatch, handleEnqueueing.n));

        long start = System.nanoTime();
        BlockableCountActor.EmptyMessage message = new BlockableCountActor.EmptyMessage();
        for (int i = 0; i < handleEnqueueing.n; i++) {
            actor.tell(message);
        }
        long spentTime = System.nanoTime() - start;

        startLatch.countDown();
        finishLatch.await();

        System.out.println("Enqueueing:");
        System.out.printf("\t%d ops\n", handleEnqueueing.n);
        System.out.printf("\t%d ns\n", spentTime);
        System.out.printf("\t%d ops/s\n", handleEnqueueing.n * 1000_000_000L / spentTime);
        handleEnqueueing.finish.countDown();
        return this;
    }

}
