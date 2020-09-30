package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;

import java.util.concurrent.CountDownLatch;

public class PingLatencyActor extends AbstractBehavior<PingLatencyActor.Command> {
    private final LatencyHistogram latencyHistogram;

    public interface Command {
    }

    @AllArgsConstructor
    public static class PingLatencyMessage implements Command {
        ActorRef<Command> sender;
    }

    public static Behavior<Command> create(CountDownLatch finishLatch, int n, LatencyHistogram latencyHistogram) {
        return Behaviors.setup(context -> new PingLatencyActor(context, finishLatch, n, latencyHistogram));
    }

    private final CountDownLatch finishLatch;
    private int i;

    public PingLatencyActor(ActorContext<Command> context, CountDownLatch finishLatch, int n, LatencyHistogram latencyHistogram) {
        super(context);
        this.finishLatch = finishLatch;
        this.latencyHistogram = latencyHistogram;
        this.i = n;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PingLatencyMessage.class, this::onPingLatencyMessage)
                .build();
    }

    private Behavior<Command> onPingLatencyMessage(PingLatencyMessage pingLatencyMessage) {
        latencyHistogram.record();
        if (i > 0 && pingLatencyMessage.sender != null) {
            ActorRef<Command> newSender = null;
            if (i > 1) {
                newSender = getContext().getSelf();
            }
            PingLatencyMessage newMessage = new PingLatencyMessage(newSender);
            pingLatencyMessage.sender.tell(newMessage);
        }
        i--;
        if (i == 0) {
            finishLatch.countDown();
            return Behaviors.stopped();
        }
        return this;
    }
}
