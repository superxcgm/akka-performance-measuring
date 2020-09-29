package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MinimalActor extends AbstractBehavior<MinimalActor.Command> {
    public interface Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(MinimalActor::new);
    }

    public MinimalActor(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().build();
    }
}
