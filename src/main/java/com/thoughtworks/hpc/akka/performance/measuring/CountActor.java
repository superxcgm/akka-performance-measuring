package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.concurrent.CountDownLatch;

public class CountActor extends AbstractBehavior<CountActor.Command> {
    public interface Command {
    }

    public static class EmptyMessage implements Command {
    }

    public static Behavior<Command> create(CountDownLatch finishLatch, int n) {
        return Behaviors.setup(context -> new CountActor(context, finishLatch, n));
    }

    private final CountDownLatch finishLatch;
    private int i;

    private CountActor(ActorContext<Command> context, CountDownLatch finishLatch, int n) {
        super(context);
        this.finishLatch = finishLatch;
        i = n;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(EmptyMessage.class, this::onEmptyMessage)
                .build();
    }

    private Behavior<Command> onEmptyMessage(EmptyMessage emptyMessage) {
        i--;
        if (i == 0) {
            finishLatch.countDown();
            return Behaviors.stopped();
        }
        return this;
    }
}
