package entity;

import logist.topology.Topology;

public class State {
    public Topology.City currentCity;

    public Topology.City neighbors;

    public State(Topology.City currentCity, Topology.City neighbors) {
        this.currentCity = currentCity;
        this.neighbors = neighbors;
    }

    public State(Topology.City currentCity) {
        this.currentCity = currentCity;
        this.neighbors = null;
    }

    public Topology.City getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(Topology.City currentCity) {
        this.currentCity = currentCity;
    }

    public Topology.City getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Topology.City neighbors) {
        this.neighbors = neighbors;
    }

}
