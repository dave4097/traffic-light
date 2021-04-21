package traffic;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class TrafficControlSystem {

    private final ActorContext<NotUsed> context;

    private TrafficControlSystem(ActorContext<NotUsed> context) {
        this.context = context;
    }

    private static Behavior<NotUsed> create() {
        return Behaviors.setup(context -> new TrafficControlSystem(context).behavior());
    }

    public static void main(String[] args) {
        ActorSystem.create(TrafficControlSystem.create(), "TrafficControlSystem");
    }

    private Behavior<NotUsed> behavior() {
        final ActorRef<TrafficLights.Command> trafficLights = context.spawn(TrafficLights.create(), "TrafficLights");
        trafficLights.tell(new TrafficLights.TurnNorthSouthGreen());
        context.spawn(Sensor.create(Sensor.Orientation.NORTH_SOUTH, trafficLights), "NorthSensor");
        context.spawn(Sensor.create(Sensor.Orientation.NORTH_SOUTH, trafficLights), "SouthSensor");
        context.spawn(Sensor.create(Sensor.Orientation.EAST_WEST, trafficLights), "EastSensor");
        context.spawn(Sensor.create(Sensor.Orientation.EAST_WEST, trafficLights), "WestSensor");
        return Behaviors.empty();
    }
}
