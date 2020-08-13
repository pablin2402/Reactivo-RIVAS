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

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import entity.ActionEntity.ActionType;

public class ReactiveTemplatePablo implements ReactiveBehavior {

	private Map<State, Double> v = new HashMap<>(); // best value for each state
	private Map<State, ActionEntity> estrategia = new HashMap<>();// best action for each state

	// It shows AllPossibleStates, that`s the unique option
	private void showNeighbors(Topology topology, List<City> cities) {
		List<State> allStates = allPossibleStates(cities);
		for (State state : allStates) {
			System.out.println(state.getCurrentCity());
			System.out.println(state.getNeighbors());
		}
	}

	private void showAllActions(Topology topology, List<City> cities) {
		List<ActionEntity> allStates = allPossibleActions(cities);
		for (ActionEntity state : allStates) {
			System.out.println(state.getDestination());
			System.out.println(state.getActionType());
		}
	}

	// It add the cities to an ArrayList with all the possible destinations
	// including itselv
	private List<State> allPossibleStates(List<City> cities) {
		List<State> allStates = new LinkedList<>();

		for (City possibleCurrentCities : cities) {
			allStates.add(new State(possibleCurrentCities, null));
			for (City possibleNeighbors : cities) {
				if (possibleCurrentCities != possibleNeighbors) {
					allStates.add(new State(possibleCurrentCities, possibleNeighbors));
				}
			}
		}
		return allStates;
	}

	private List<ActionEntity> allPossibleActions(List<City> cities) {
		List<ActionEntity> allActions = new ArrayList<>();
		// allActions.add(new ActionEntity(null, true));// true pickup

		for (City a : cities) {
			allActions.add(new ActionEntity(a, ActionEntity.ActionType.PICKUP));// true pickup

			allActions.add(new ActionEntity(a, ActionEntity.ActionType.MOVE));// false = move
		}
		return allActions;
	}

	private List<ActionEntity> actionsPossible(State currentState, List<ActionEntity> actions) {
		List<ActionEntity> allActionsPossible = new ArrayList<>();
		for (ActionEntity currentAction : actions) {
			if (currentAction.getActionType() == ActionEntity.ActionType.MOVE
			/*
			 * && currentState.getCurrentCity().hasNeighbor(currentAction.getDestination())
			 */) {
				allActionsPossible.add(currentAction);
			} else if (currentAction.getActionType() == ActionEntity.ActionType.PICKUP) {
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

		double costForMoving;
		if (actionEntity.getActionType() == ActionEntity.ActionType.MOVE) {

			costForMoving = (-1) * state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm;
			return costForMoving;
		}

		return td.reward(state.getCurrentCity(), actionEntity.getDestination());
	}

	private double transition(State currentCity, ActionEntity action, State moveTo, TaskDistribution td) {

		// the next state should start in the destination city

		if (action.getActionType() == ActionType.PICKUP
				&& !moveTo.getCurrentCity().equals(currentCity.getNeighbors())) {
			return 0;
		} else if (action.getActionType() == ActionType.MOVE
				&& !moveTo.getCurrentCity().equals(action.getDestination())) {
			return 0;
		}

		return td.probability(currentCity.getCurrentCity(), moveTo.getCurrentCity());
	}

	private void methodForReinforcementLearnig(Topology topology, TaskDistribution td, Agent agent) {
		List<Topology.City> cities = topology.cities();
		// List for states and actions
		List<State> allStates = allPossibleStates(cities);
		List<ActionEntity> allActions = allPossibleActions(cities);
		// Initialize linked hash maps
		// v = new HashMap<>();
		// Initialize the vector arbitrarily
		for (State statesCurrent : allStates) {
			v.put(statesCurrent, 0.0);
		}

		Map<State, Double> vAnterior;

		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);
		double diferencia;
		do {
			// copiamos los valores anteriores
			vAnterior = new HashMap<>(v);

			for (State currentState : allStates) {
				Map<ActionEntity, Double> q = new HashMap<>();// reward for each action

				for (ActionEntity currentAction : actionsPossible(currentState, allActions)) {
					double sum;
					double sum2 = 0;

					double qMax = calculateCost(currentState, td, agent, currentAction);
					for (State toState : allStates) {

						sum2 = transition(currentState, currentAction, toState, td) * v.get(toState) * discount;
						sum = sum2;
					}

					sum = +qMax;
					q.put(currentAction, sum);

				}
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
				ActionEntity actionate = estrategia.get(currentState);
				// System.out.println("a" + actionate.getDestination() +
				// actionate.getActionType());
			}
			diferencia = 0.0;
			for (State cState : allStates) {
				diferencia += v.get(cState) - vAnterior.get(cState);
			}
		} while (diferencia > 0.0001);

	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		methodForReinforcementLearnig(topology, td, agent);

	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		City currentCity = vehicle.getCurrentCity();

		State currState = new State(null, null);
		currState.setCurrentCity(vehicle.getCurrentCity());
		if (availableTask != null) {
			currState.setNeighbors(availableTask.deliveryCity);
		}
		// estrategia.values().removeIf(st -> st.getActionType() == null);

		ActionEntity actionate = estrategia.get(currState);
		System.out.print(actionate.getActionType());

		if (Objects.equals(actionate.getActionType(), ActionEntity.ActionType.MOVE)) {
			action = new Move(actionate.getDestination());
		} else {
			action = new Pickup(availableTask);
		}

		return action;

	}
}
