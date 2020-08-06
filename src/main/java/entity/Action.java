package entity;

import logist.topology.Topology;

public class Action {
    private Topology.City destination;
    private Action action;

    public Action(Topology.City destination, Action action) {
        this.destination = destination;
        this.action = action;
    }

    public Topology.City getDestination() {
        return destination;
    }

    public void setDestination(Topology.City destination) {
        this.destination = destination;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
