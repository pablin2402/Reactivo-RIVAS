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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entity.ActionEntity.ActionKind;

public class ReactiveTemplatePablo implements ReactiveBehavior {
	private Random random;
	private double pPickup;
	private Map<State, Double> v;
	private Map<State, ActionEntity> stateAction;

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

	// It add the cities to an ArrayList with all the possible destinations
	// including itselv
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
		allActions.add(new ActionEntity(null, ActionEntity.ActionKind.PICKUP));
		for (City a : cities) {
			allActions.add(new ActionEntity(a, ActionEntity.ActionKind.MOVE));
		}
		return allActions;
	}

	private List<ActionEntity> actionsPossible(State currentState, List<ActionEntity> actions) {
		List<ActionEntity> allActionsPossible = new ArrayList<>();
		for (ActionEntity currentAction : actions) {
			if (currentAction.getAction() == ActionKind.MOVE) {
				allActionsPossible.add(currentAction);
			} else if (currentAction.getAction() == ActionKind.PICKUP) {
				allActionsPossible.add(currentAction);

			}
		}
		return allActionsPossible;

	}

	// It calculates the cost between the currentcity to the destiny if it has a
	// reward or not
	public static double calculateCost(State state, State toState, TaskDistribution td, Agent agent,
			ActionEntity actionEntity) {
		// it could be global....
		double costPerKm = agent.vehicles().get(0).costPerKm();
		if (actionEntity.getAction() == ActionEntity.ActionKind.MOVE) {
			// System.out.println("Costo si el vehiculo se mueve: "+-1 *
			// state.getCurrentCity().distanceTo(actionEntity.getDestination()) *
			// costPerKm);
			return (double) ((-1) * state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm);
		}
		// System.out.println(td.reward(state.getCurrentCity(), state.getNeighbors())
		// - (state.getCurrentCity().distanceTo(toState.getCurrentCity()) * costPerKm));

		return (double) td.reward(state.getCurrentCity(), state.getNeighbors())
				- (state.getCurrentCity().distanceTo(toState.getCurrentCity()) * costPerKm);
	}

	private double transition(State currentCity, ActionEntity action, State moveTo, TaskDistribution td) {

		// the next state should start in the destination city
		if (action.getAction() == ActionEntity.ActionKind.PICKUP
				&& !moveTo.getCurrentCity().equals(currentCity.getNeighbors())) {
			return 0;
		}
		// move action
		else if (action.getAction() == ActionEntity.ActionKind.MOVE
				&& !moveTo.getCurrentCity().equals(action.getDestination())) {
			return 0;
		}
		// System.out.println("PROBABILIDAD DE " + currentCity.getCurrentCity() + "A" +
		// moveTo.getCurrentCity());
		// System.out.println(td.probability(moveTo.getCurrentCity(),
		// moveTo.getNeighbors()));

		return td.probability(moveTo.getCurrentCity(), moveTo.getNeighbors());
	}

	private void methodForReinforcementLearnig(Topology topology, TaskDistribution td, Agent agent, Double discount) {
		List<Topology.City> cities = topology.cities();
		// List for states and actions
		List<State> allStates = allPossibleStates(topology, cities);
		List<ActionEntity> allActions = allPossibleActions(topology, cities);
		// Cost per km
		int costPerKm = agent.vehicles().get(0).costPerKm();
		v = new LinkedHashMap<>();
		stateAction = new LinkedHashMap<>();

		// Initialize the vector arbitrarily
		for (State state : allStates) {
			v.put(state, 0.0);
		}
		// v.forEach((k, v) -> System.out.println("LLave: " + k + "valor: " + v));

		double diferencia = 1;
		do {
			System.out.println("caca");
			for (State currentState : allStates) {
				ActionEntity newStateBestAction = null;
				double q = Double.MIN_VALUE;

				for (ActionEntity currentAction : actionsPossible(currentState, allActions)) {
					double sum = 0;

					for (State toState : allStates) {

						if (!currentState.getCurrentCity().equals(toState.getCurrentCity())
								&& !currentState.getCurrentCity().hasNeighbor(currentAction.getDestination())) {

							double qMax = calculateCost(currentState, toState, td, agent, currentAction);
							sum += discount * transition(currentState, currentAction, toState, td) * v.get(toState);
							sum = sum + qMax;
						}
					}
					if (sum > q) {
						q = sum;
						newStateBestAction = currentAction;
						if (sum != 0) {
							diferencia = (sum - q) / q;
						}
					}
					v.put(currentState, q);
					stateAction.put(currentState, currentAction);
				}

			}

		} while (diferencia > 0.000001);

	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);
		methodForReinforcementLearnig(topology, td, agent, discount);

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
