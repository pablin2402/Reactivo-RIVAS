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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReactiveTemplatePablo implements ReactiveBehavior {

	private Map<State, Double> v;
	private Map<ActionEntity, Double> q;
	private Map<State, ActionEntity> estrategia;

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
		for (City a : cities) {
			allActions.add(new ActionEntity(a, true));// true pickup

			allActions.add(new ActionEntity(a, false));// false = move
		}
		return allActions;
	}

	private List<ActionEntity> actionsPossible(State currentState, List<ActionEntity> actions) {
		List<ActionEntity> allActionsPossible = new ArrayList<>();
		for (ActionEntity currentAction : actions) {
			if (currentAction.getAction().equals(false)
					&& currentState.getCurrentCity().hasNeighbor(currentAction.getDestination())) {
				allActionsPossible.add(currentAction);
			} else if (currentAction.getAction().equals(true)) {
				allActionsPossible.add(currentAction);
			}
		}
		return allActionsPossible;

	}

	// It calculates the cost between the currentcity to the destiny if it has a
	// reward or not
	public static double calculateCost(State state, TaskDistribution td, Agent agent, ActionEntity actionEntity) {
		// it could be global....
		double costPerKm = agent.vehicles().get(0).costPerKm();
		if (actionEntity.getAction().equals(false)) {// move

			return ((-1) * state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm);
		}

		return (double) td.reward(state.getCurrentCity(), actionEntity.getDestination()) - (state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm);
	}

	private double transition(State currentCity, ActionEntity action, State moveTo, TaskDistribution td) {

		// the next state should start in the destination city
		if (action.getAction().equals(true) && !moveTo.getCurrentCity().equals(currentCity.getNeighbors())) {
			return 0;
		} else if (action.getAction().equals(false) && !moveTo.getCurrentCity().equals(action.getDestination())) {
			return 0;
		}

		return td.probability(moveTo.getCurrentCity(), moveTo.getNeighbors());
	}

	private void methodForReinforcementLearnig(Topology topology, TaskDistribution td, Agent agent) {
		List<Topology.City> cities = topology.cities();
		// List for states and actions
		List<State> allStates = allPossibleStates(topology, cities);
		List<ActionEntity> allActions = allPossibleActions(topology, cities);
		// Initialize linked hash maps
		v = new LinkedHashMap<>();
		q = new LinkedHashMap<>();
		estrategia = new LinkedHashMap<>();

		// Initialize the vector arbitrarily
		for (State state : allStates) {
			v.put(state, 0.0);
		}

		Map<State, Double> vAnterior;
		showAllActions(topology, cities);
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);
		double diferencia = 1;
		do {
			// copiamos los valores anteriores
			vAnterior = new HashMap<>(v);

			for (State currentState : allStates) {

				for (ActionEntity currentAction : actionsPossible(currentState, allActions)) {
					double sum;
					double sum2 = 0;
					double qMax = calculateCost(currentState, td, agent, currentAction);

					for (State toState : allStates) {

						if (!currentState.getCurrentCity().equals(toState.getCurrentCity())
								&& !currentState.getCurrentCity().hasNeighbor(currentAction.getDestination())) {

							sum2 = transition(currentState, currentAction, toState, td) * v.get(toState);
							sum = sum2 + qMax;

						}
					}
					sum2 = sum2 * discount;
					sum = sum2 + qMax;
					q.put(currentAction, sum);

					ActionEntity bestAction = null;
					double maximQ = Double.MIN_VALUE;
					for (Map.Entry<ActionEntity, Double> entradaQ : q.entrySet()) {
						if (entradaQ.getValue() > maximQ) {
							maximQ = entradaQ.getValue();
							bestAction = entradaQ.getKey();
						}
					}

					v.put(currentState, maximQ);
					estrategia.put(currentState, bestAction);
					for (State cState : allStates) {
						diferencia += v.get(cState) - vAnterior.get(cState);
					}

				}

			}

		} while (diferencia > 0.000001);

	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		methodForReinforcementLearnig(topology, td, agent);

	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		State state;
		if (availableTask == null) {
			state = new State(vehicle.getCurrentCity());
		} else {
			state = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
		}
		ActionEntity agentAction = estrategia.get(state);
		System.out.println(agentAction.getAction());
		if (agentAction.getAction() == false) {
			action = new Move(agentAction.getDestination());

		} else {
			action = new Pickup(availableTask);

		}
		return action;
	}
}
