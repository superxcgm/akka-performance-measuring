package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class BlockableCountActor extends AbstractBehavior<BlockableCountActor.Command> {
    public interface Command {
    }

    public static class EmptyMessage implements Command{}

    public static Behavior<Command> create(CountDownLatch startLatch, CountDownLatch finishLatch, int n) {
        return Behaviors.setup(context -> new BlockableCountActor(context, startLatch, finishLatch, n));
    }

    private boolean blocked = true;
    private int i;
    private final CountDownLatch startLatch;
    private final CountDownLatch finishLatch;
    private final Logger logger;

    public BlockableCountActor(ActorContext<Command> context, CountDownLatch startLatch, CountDownLatch finishLatch, int n) {
        super(context);
        this.startLatch = startLatch;
        this.finishLatch = finishLatch;
        i = n - 1;
        logger = getContext().getLog();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(EmptyMessage.class, this::onEmptyMessage)
                .build();
    }

    private Behavior<Command> onEmptyMessage(EmptyMessage msg) {
        if (blocked) {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                logger.warn(e.toString());
            }
            blocked = false;
        } else {
            i--;
            if (i == 0) {
                finishLatch.countDown();
                return Behaviors.stopped();
            }
        }
        return this;
    }
}
