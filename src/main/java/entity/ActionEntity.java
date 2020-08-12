package entity;

import logist.topology.Topology;

public class ActionEntity {
    private Topology.City destination;
    private Boolean action;

    public ActionEntity(Topology.City destination, Boolean action) {
        this.destination = destination;
        this.action = action;
    }


    public Topology.City getDestination() {
        return destination;
    }

    public void setDestination(Topology.City destination) {
        this.destination = destination;
    }

    public Boolean getAction() {
        return action;
    }

    public void setAction(Boolean action) {
        this.action = action;
    }








}
