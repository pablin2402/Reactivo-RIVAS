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

import java.util.*;

public class ReactiveTemplatePablo implements ReactiveBehavior {

	Map<State, Double> v;// best value for each state
	Map<State, ActionEntity> estrategia = new HashMap<>();// best action for each state


	// It shows AllPossibleStates, that`s the unique option
	private void showNeighbors(Topology topology, List<City> cities) {
		List<State> allStates = allPossibleStates(cities);
		for (State state : allStates) {
			System.out.println(state.getClass());
			System.out.println(state.getNeighbors());
		}
	}

	private void showAllActions(Topology topology, List<City> cities) {
		List<ActionEntity> allStates = allPossibleActions(cities);
		for (ActionEntity state : allStates) {
			System.out.println(state.getDestination());
			System.out.println(state.getAction());
		}
	}

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
			allActions.add(new ActionEntity(a, true));// true pickup

			allActions.add(new ActionEntity(a, false));// false = move
		}
		return allActions;
	}

	private List<ActionEntity> actionsPossible(State currentState, List<ActionEntity> actions) {
		List<ActionEntity> allActionsPossible = new ArrayList<>();
		for (ActionEntity currentAction : actions) {
			if (currentAction.getAction() == false
					&& currentState.getCurrentCity().hasNeighbor(currentAction.getDestination())) {
				allActionsPossible.add(currentAction);
			} else if (currentAction.getAction() == true) {
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

		if (actionEntity.getAction() == false) {// move

			return ((-1) * state.getCurrentCity().distanceTo(actionEntity.getDestination()) * costPerKm);
		}

		return td.reward(state.getCurrentCity(), actionEntity.getDestination());
	}

	private double transition(State currentCity, ActionEntity action, State moveTo, TaskDistribution td) {

		// the next state should start in the destination city
		if (action.getAction() == true && !moveTo.getCurrentCity().equals(currentCity.getNeighbors())) {
			return 0;
		} else if (action.getAction() == false && !moveTo.getCurrentCity().equals(action.getDestination())) {
			return 0;
		}

		return td.probability(moveTo.getCurrentCity(), moveTo.getNeighbors());
	}
	private void methodForReinforcementLearnig(Topology topology, TaskDistribution td, Agent agent) {
		List<Topology.City> cities = topology.cities();
		// List for states and actions
		List<State> allStates = allPossibleStates(cities);
		List<ActionEntity> allActions = allPossibleActions(cities);
		// Initialize linked hash maps
		v = new HashMap<>();

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



			}
			diferencia = 0.0;
			for (State cState : allStates) {
				diferencia = v.get(cState) - vAnterior.get(cState);

			}

		} while (diferencia > 0.000001);
		System.out.println("Vector V:");
		v.values().forEach(System.out::println);
		System.out.println("\nEstategia:");
		estrategia.entrySet().forEach(e -> System.out.println("S=" + e.getKey() + "  A=" + e.getValue()));
	}


	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		methodForReinforcementLearnig(topology, td, agent);
		this.random = new Random();


	}
	private Random random;


	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		City currentCity = vehicle.getCurrentCity();
		State nowState = new State(currentCity, availableTask.deliveryCity);
		System.out.println(currentCity);

		ActionEntity agentAction =  estrategia.get(nowState);

		if (agentAction.getAction() == false) {
			action = new Move(agentAction.getDestination());
		} else {
			action = new Pickup(availableTask);

		}
		return action;
	}
}
