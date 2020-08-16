package rivas;

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
import rivas.ActionEntity.ActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReactiveTemplatePablo implements ReactiveBehavior {

	private Map<State, Double> v = new HashMap<>(); // best value for each state
	private Map<State, ActionEntity> estrategy = new HashMap<>();// best action for each state

	// It add the cities to an ArrayList with all the possible destinations
	// including itselv
	private List<State> allPossibleStates(List<City> cities) {
		List<State> allStates = new LinkedList<>();

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

	private List<ActionEntity> allPossibleActions(List<City> cities) {
		List<ActionEntity> allActions = new ArrayList<>();
		// allActions.add(new ActionEntity(null, true));// true pickup

		for (City a : cities) {
			allActions.add(new ActionEntity(a, ActionEntity.ActionType.PICKUP));

			allActions.add(new ActionEntity(a, ActionEntity.ActionType.MOVE));
		}
		return allActions;
	}

	private List<ActionEntity> actionsPossible(State currentState, List<ActionEntity> actions) {
		List<ActionEntity> allActionsPossible = new ArrayList<>();
		for (ActionEntity currentAction : actions) {
			if (Objects.equals(currentAction.getActionType(), ActionEntity.ActionType.MOVE)
					&& currentState.getCurrentCity().hasNeighbor(currentAction.getDestination())) {
				allActionsPossible.add(currentAction);
			} else if (Objects.equals(currentAction.getActionType(), ActionEntity.ActionType.PICKUP)) {
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

		double costForMoving = 0;
		if (Objects.equals(actionEntity.getActionType(), ActionEntity.ActionType.MOVE)) {

			costForMoving = (-1) * state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm;
			return costForMoving;
		}
		return td.reward(state.getCurrentCity(), actionEntity.getDestination())
				- (state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm);

	}

	private double transition(State state, ActionEntity action, State moveTo, TaskDistribution td) {

		// the next state should start in the destination city

		if (state.getCurrentCity().equals(action.getDestination())
				|| state.getCurrentCity().equals(state.getNeighbors())
				|| state.getCurrentCity().equals(moveTo.getCurrentCity())
				|| !moveTo.getCurrentCity().equals(action.getDestination())) {
			return 0.0;
		} else {
			return td.probability(moveTo.getCurrentCity(), moveTo.getNeighbors());

		}

	}

	private void methodForReinforcementLearnig(Topology topology, TaskDistribution td, Agent agent) {
		List<Topology.City> cities = topology.cities();
		// List for states and actions
		List<State> allStates = allPossibleStates(cities);
		List<ActionEntity> allActions = allPossibleActions(cities);
		// store last v
		Map<State, Double> vAnterior;

		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);
		double diference;

		// Initialize the vector arbitrarily
		for (State statesCurrent : allStates) {
			v.put(statesCurrent, 0.0);
		}

		do {
			// copiamos los valores anteriores
			vAnterior = new HashMap<>(v);

			for (State currentState : allStates) {
				Map<ActionEntity, Double> q = new HashMap<>();// reward for each action

				for (ActionEntity currentAction : actionsPossible(currentState, allActions)) {
					double sum = 0;

					double qMax = calculateCost(currentState, td, agent, currentAction);
					for (State toState : allStates) {

						sum = transition(currentState, currentAction, toState, td) * v.get(toState) * discount;

					}

					sum += qMax;
					q.put(currentAction, sum);

				}
				// best action
				ActionEntity bestAction = null;
				double maximQ = Double.MIN_VALUE;
				for (Map.Entry<ActionEntity, Double> entradaQ : q.entrySet()) {
					if (entradaQ.getValue() > maximQ) {
						maximQ = entradaQ.getValue();
						bestAction = entradaQ.getKey();
					}
				}
				v.put(currentState, maximQ);
				estrategy.put(currentState, bestAction);

			}
			diference = 0.00;
			for (State cState : allStates) {
				diference += v.get(cState) - vAnterior.get(cState);
			}
		} while (diference > 0.00000001);

	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		methodForReinforcementLearnig(topology, td, agent);

	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		City currentcity = vehicle.getCurrentCity();

		if (Objects.equals(availableTask, null)) {
			ActionEntity actionate = estrategy
					.get(new State(currentcity, availableTask == null ? null : availableTask.deliveryCity));

			action = new Move(actionate.getDestination());
		} else {
			action = new Pickup(availableTask);
		}

		return action;

	}
}
