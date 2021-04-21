## Akka Traffic Light System

A simple example of how to use akka for a traffic light system.
The traffic lights will change when:
1. A sensor detects a car approaching and the traffic lights last changed more than 30 seconds ago,
2. No car has approached and 5 minutes have passed since the lights changed.