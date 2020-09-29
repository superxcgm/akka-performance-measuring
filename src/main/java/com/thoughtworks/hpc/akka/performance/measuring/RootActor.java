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
import java.util.concurrent.CountDownLatch;

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
                .build();
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
