package entity;

import logist.topology.Topology.City;

public class ActionEntity {
    public City destination;

    private ActionType actionType;

    public enum ActionType {
        PICKUP, MOVE
    }
    public ActionEntity(City destination, ActionType actionType) {
        this.destination = destination;
        this.actionType = actionType;
    }

    public City getDestination() {
        return destination;
    }

    public void setDestination(City destination) {
        this.destination = destination;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }



}
