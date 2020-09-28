package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class RootActor extends AbstractBehavior<RootActor.Command> {

    private final Logger logger;

    public interface Command {
    }

    @AllArgsConstructor
    public static class HandleEnqueueing implements Command {
        private final int n;
        private CountDownLatch finish;
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
                .onMessage(HandleEnqueueing.class, this::onHandleQueueing)
                .build();
    }

    private Behavior<Command> onHandleQueueing(HandleEnqueueing handleEnqueueing) {
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
        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            logger.warn(e.toString());
        }

        System.out.println("Queueing:");
        System.out.printf("\t%d ops\n", handleEnqueueing.n);
        System.out.printf("\t%d ns\n", spentTime);
        System.out.printf("\t%d ops/s\n", handleEnqueueing.n * 1000_000_000L / spentTime);
        handleEnqueueing.finish.countDown();
        return this;
    }

}
