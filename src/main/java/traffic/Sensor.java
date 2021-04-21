package traffic;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.time.Duration;
import java.util.Random;

public class Sensor {

    private final ActorContext<Fire> ctx;
    private final Orientation orientation;
    private final ActorRef<TrafficLights.Command> trafficLights;

    private Sensor(ActorContext<Fire> ctx, Orientation orientation, ActorRef<TrafficLights.Command> trafficLights) {
        this.ctx = ctx;
        this.orientation = orientation;
        this.trafficLights = trafficLights;
    }

    public static Behavior<Fire> create(Orientation orientation, ActorRef<TrafficLights.Command> trafficLights) {
        return Behaviors.setup(ctx -> {
            scheduleRandomFire(ctx);
            return new Sensor(ctx, orientation, trafficLights).on();
        });
    }

    private Behavior<Fire> on() {
        return Behaviors.receive(Fire.class)
                .onMessage(Fire.class, msg -> {
                    ctx.getLog().info("Vehicle detected, orientation={}", orientation);
                    if (orientation == Orientation.NORTH_SOUTH) {
                        trafficLights.tell(new TrafficLights.TurnNorthSouthGreen());
                    } else {
                        trafficLights.tell(new TrafficLights.TurnEastWestGreen());
                    }
                    scheduleRandomFire(ctx);
                    return on();
                })
                .build();
    }

    private static void scheduleRandomFire(ActorContext<Fire> ctx) {
        final Duration timeUntilNextFire = Duration.ofSeconds(new Random().nextInt(240));
        ctx.scheduleOnce(timeUntilNextFire, ctx.getSelf(), new Fire());
    }

    static class Fire {

    }

    enum Orientation {
        NORTH_SOUTH,
        EAST_WEST
    }
}
