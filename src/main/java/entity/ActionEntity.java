package entity;

import logist.topology.Topology;

public class ActionEntity {
    private Topology.City destination;
    private ActionKind action;

    public enum ActionKind {
        Move, Collect
    }

    public ActionEntity(Topology.City destination, ActionKind action) {
        this.destination = destination;
        this.action = action;
    }

    public Topology.City getDestination() {
        return destination;
    }

    public void setDestination(Topology.City destination) {
        this.destination = destination;
    }

    public ActionKind getAction() {
        return action;
    }

    public void setAction(ActionKind action) {
        this.action = action;
    }

}
