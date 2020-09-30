package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;

import java.util.concurrent.CountDownLatch;

public class PingThroughputActor extends AbstractBehavior<PingThroughputActor.Command> {

    public interface Command {

    }

    @AllArgsConstructor
    public static class PingThroughputMessage implements Command {
        ActorRef<Command> sender;
    }

    public static Behavior<Command> create(CountDownLatch finishLatch, int n) {
        return Behaviors.setup(context -> new PingThroughputActor(context, finishLatch, n));
    }

    private int i;
    private final CountDownLatch finishLatch;

    public PingThroughputActor(ActorContext<Command> context, CountDownLatch finishLatch, int n) {
        super(context);
        this.finishLatch = finishLatch;
        this.i = n;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PingThroughputMessage.class, this::onPingThroughputMessage)
                .build();
    }

    private Behavior<Command> onPingThroughputMessage(PingThroughputMessage pingThroughputMessage) {
        if (i > 0 && pingThroughputMessage.sender != null) {
            ActorRef<Command> newSender = null;
            if (i > 1) {
                newSender = getContext().getSelf();
            }
            PingThroughputMessage newMessage = new PingThroughputMessage(newSender);
            pingThroughputMessage.sender.tell(newMessage);
        }
        i--;
        if (i == 0) {
            finishLatch.countDown();
            return Behaviors.stopped();
        }
        return this;
    }

}
