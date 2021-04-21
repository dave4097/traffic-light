package traffic;

import akka.actor.Cancellable;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.time.Duration;
import java.time.Instant;

public class TrafficLights {

    private final ActorContext<Command> ctx;

    private TrafficLights(ActorContext<Command> ctx) {
        this.ctx = ctx;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ctx -> new TrafficLights(ctx).off());
    }

    private Behavior<Command> off() {
        return Behaviors.receive(Command.class)
                .onMessage(TurnEastWestGreen.class, msg -> {
                    ctx.getLog().info("East/west traffic lights green");
                    return turnEastWestGreen(Duration.ofMinutes(2));
                })
                .onMessage(TurnNorthSouthGreen.class, msg -> {
                    ctx.getLog().info("North/south traffic lights green");
                    return turnNorthSouthGreen(Duration.ofMinutes(2));
                })
                .build();
    }

    private Behavior<Command> northSouthGreen(Instant changeTime, Cancellable nextInvocation) {
        return Behaviors.receive(Command.class)
                .onMessage(TurnEastWestGreen.class, msg -> {
                    final long secondsSinceChange = Duration.between(changeTime, Instant.now()).getSeconds();
                    if (secondsSinceChange < 30) {
                        ctx.getLog().info("North/south traffic lights staying green");
                        return northSouthGreen(changeTime, nextInvocation);
                    } else {
                        ctx.getLog().info("East/west traffic lights turing green");
                        if (!nextInvocation.isCancelled()) {
                            nextInvocation.cancel();
                        }
                        return turnEastWestGreen(Duration.ofMinutes(2));
                    }
                })
                .onMessage(TurnNorthSouthGreen.class, msg -> {
                    ctx.getLog().info("North/south traffic lights staying green");
                    return northSouthGreen(changeTime, nextInvocation);
                })
                .build();
    }

    private Behavior<Command> eastWestGreen(Instant changeTime, Cancellable nextInvocation) {
        return Behaviors.receive(Command.class)
                .onMessage(TurnNorthSouthGreen.class, msg -> {
                    final long secondsSinceChange = Duration.between(changeTime, Instant.now()).getSeconds();
                    if (secondsSinceChange < 30) {
                        ctx.getLog().info("East/west traffic lights staying green");
                        return eastWestGreen(changeTime, nextInvocation);
                    } else {
                        ctx.getLog().info("North/south traffic lights turing green");
                        if (!nextInvocation.isCancelled()) {
                            nextInvocation.cancel();
                        }
                        return turnNorthSouthGreen(Duration.ofMinutes(2));
                    }
                })
                .onMessage(TurnEastWestGreen.class, msg -> {
                    ctx.getLog().info("East/west traffic lights staying green");
                    return eastWestGreen(changeTime, nextInvocation);
                })
                .build();
    }

    private Behavior<Command> turnNorthSouthGreen(Duration duration) {
        final Cancellable nextInvocation = ctx.scheduleOnce(duration, ctx.getSelf(), new TurnEastWestGreen());
        return northSouthGreen(Instant.now(), nextInvocation);
    }

    private Behavior<Command> turnEastWestGreen(Duration duration) {
        final Cancellable nextInvocation = ctx.scheduleOnce(duration, ctx.getSelf(), new TurnNorthSouthGreen());
        return eastWestGreen(Instant.now(), nextInvocation);
    }

    interface Command {}

    static class TurnNorthSouthGreen implements Command {

    }

    static class TurnEastWestGreen implements Command {

    }
}
