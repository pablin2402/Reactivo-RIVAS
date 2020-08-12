package entity;

import logist.topology.Topology;

public class ActionEntity {
    public Topology.City destination;
    public boolean action;

    public ActionEntity(Topology.City destination, boolean action) {
        this.destination = destination;
        this.action = action;
    }

    public Topology.City getDestination() {
        return destination;
    }

    public void setDestination(Topology.City destination) {
        this.destination = destination;
    }

    public boolean getAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }

}
