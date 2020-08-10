package entity;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.*;

public class ReactiveTemplatePablo implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private Map<State, Double> v;
	private Map<State, Action> stateAction;

	// It add the cities to an ArrayList with all the possible destinations
	// including itselves

	// It shows AllPossibleStates, that`s the unique option
	private void showNeighbors(Topology topology, List<City> cities) {
		List<State> allStates = allPossibleStates(topology, cities);
		for (State state : allStates) {
			System.out.println(state.currentCity);
			System.out.println(state.neighbors);
		}
	}

	private void showAllActions(Topology topology, List<City> cities) {
		List<ActionEntity> allStates = allPossibleActions(topology, cities);
		for (ActionEntity state : allStates) {
			System.out.println(state.getDestination());
			System.out.println(state.getAction());
		}
	}

	private List<State> allPossibleStates(Topology topology, List<City> cities) {
		List<State> allStates = new ArrayList<State>();

		for (City possibleCurrentCities : cities) {
			allStates.add(new State(possibleCurrentCities, null));
			for (City possibleNeighbors : cities) {
				if (!possibleCurrentCities.equals(possibleNeighbors)) {
					allStates.add(new State(possibleCurrentCities, possibleNeighbors));
				}
			}
		}
		return allStates;
	}

	private List<ActionEntity> allPossibleActions(Topology topology, List<City> cities) {
		List<ActionEntity> allActions = new ArrayList<>();
		allActions.add(new ActionEntity(null, ActionEntity.ActionKind.Collect));
		for (City a : cities) {
			allActions.add(new ActionEntity(a, ActionEntity.ActionKind.Move));
		}
		return allActions;
	}

	// It calculates the cost between the currentcity to the destiny if it has a
	// reward or not
	private static double calculateCost(State state, TaskDistribution td, Agent agent, ActionEntity actionEntity) {
		// it could be global....
		double costPerKm = agent.vehicles().get(0).costPerKm();

		if (actionEntity.getAction() == ActionEntity.ActionKind.Move) {
			return -1 * state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm;
		}
		// reward if it exist a task
		return td.reward(state.getCurrentCity(), state.getNeighbors())
				- (state.getCurrentCity().distanceTo(state.getNeighbors()) * costPerKm);
	}

	private double transition(State currentCity, ActionEntity action, State moveTo, TaskDistribution td) {

		// the next state should start in the destination city
		if (action.getAction() == ActionEntity.ActionKind.Collect
				&& !moveTo.getCurrentCity().equals(currentCity.getNeighbors())) {
			return 0;
		}
		// move action
		else if (action.getAction() == ActionEntity.ActionKind.Move
				&& !moveTo.getCurrentCity().equals(action.getDestination())) {
			return 0;
		}
		return td.probability(moveTo.getCurrentCity(), moveTo.getNeighbors());
	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		Double discount = agent.readProperty("discount-factor", Double.class, 0.5);

		List<Topology.City> cities = topology.cities();
		// List for states and actions
		List<State> allStates = allPossibleStates(topology, cities);
		List<ActionEntity> allActions = allPossibleActions(topology, cities);
		// Cost per km
		int costPerKim = agent.vehicles().get(0).costPerKm();
		java.util.logging.Logger.getLogger("Costo por kilometro :" + costPerKim);
		// initializes the linkedhash map for v
		v = new LinkedHashMap<>();
		stateAction = new LinkedHashMap<>();

		stateAction = new LinkedHashMap<>();

		// Initialize the vector arbitrarily
		for (State state : allStates) {
			v.put(state, new Double(0.0));
		}
		v.forEach((k, v) -> System.out.println("LLave: " + k + "valor: " + v));

		int diferencia = 1;
		double gama = 0.90;
		do {
			for (State currentState : allStates) {
				for (ActionEntity currentAction : allActions) {
					// pickup action
					double sum = 0;
					double qMax = Double.NEGATIVE_INFINITY;
					// it verifies if the action is collect or move
					if (currentAction.getAction() == ActionEntity.ActionKind.Collect
							|| currentAction.getAction() == ActionEntity.ActionKind.Collect) {
						qMax = calculateCost(currentState, td, agent, currentAction);
						for (State toState : allStates) {
							sum += transition(currentState, currentAction, toState, td);
							Double bestAction = v.get(toState);
							System.out.println(bestAction);
							if (bestAction != null) {
								Double qValue = v.get(toState);
								qMax += sum * discount * qValue;

							}
						}
						// now we have to check if a new max exists
						Double bestAction = v.get(currentState);
						System.out.println(bestAction);

					}
				}
			}
		} while (diferencia < 0.000001);

	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
}
