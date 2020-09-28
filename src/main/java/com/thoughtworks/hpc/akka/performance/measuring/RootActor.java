package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;

public class RootActor extends AbstractBehavior<RootActor.Command> {

    private final Logger logger;

    public interface Command {
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
        return null;
    }
}
