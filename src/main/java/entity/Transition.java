package entity;

public class Transition {
    private State state;
    private  ActionEntity action;
    public Transition(State state, ActionEntity action) {
        this.state = state;
        this.action = action;
    }
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ActionEntity getAction() {
        return action;
    }

    public void setAction(ActionEntity action) {
        this.action = action;
    }


}
