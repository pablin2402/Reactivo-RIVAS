package entity;

import logist.topology.Topology.City;;

public class State {
    public City currentCity;

    public City neighbors;

    public State(City currentCity, City neighbors) {
        this.currentCity = currentCity;
        this.neighbors = neighbors;
    }

    public State(City currentCity) {
        this.currentCity = currentCity;
        this.neighbors = null;
    }

    public City getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(City currentCity) {
        this.currentCity = currentCity;
    }

    public City getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(City neighbors) {
        this.neighbors = neighbors;
    }

    public boolean hasDestination() {
        return neighbors != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
        result = prime * result + ((neighbors == null) ? 0 : neighbors.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if (currentCity == null) {
            if (other.currentCity != null)
                return false;
        } else if (!currentCity.equals(other.currentCity))
            return false;
        if (neighbors == null) {
            if (other.neighbors != null)
                return false;
        } else if (!neighbors.equals(other.neighbors))
            return false;
        return true;
    }

}
