package reactive.template;

import java.util.*;

import entity.ActionEntity;
import entity.State;
import entity.ActionEntity.ActionKind;
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

public class ReactiveTemplate implements ReactiveBehavior {
	private Map<State, Double> v;
	private Map<State, Double> q;

	// Basic template
	private Random random;
	private double pPickup;

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

	// It calculates the cost between the currentcity to the destiny
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

	private double transition(State currentCity, ActionEntity action, State moveTo, TaskDistribution td){

			if (action.getAction() == ActionEntity.ActionKind.Collect && !moveTo.getCurrentCity().equals(currentCity.getNeighbors()))
			{
				return 0;
			}	
			// move action
			else if(action.getAction()==ActionEntity.ActionKind.Move&&action.getDestination().equals(moveTo.getCurrentCity())
			{
				return 0;
			}
			return td.probability(moveTo.getCurrentCity(),moveTo.getNeighbors());
	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) throws NullPointerException {
		int costPerKim = agent.vehicles().get(0).costPerKm();

		System.out.println("Costo por kilometro :" + costPerKim);
		v = new LinkedHashMap<State, Double>();
		List<Topology.City> cities = topology.cities();

		List<State> allStates = allPossibleStates(topology, cities);
		List<ActionEntity> allActions = allPossibleActions(topology, cities);
		showAllActions(topology, cities);
		// It shows all the cities
		// allPossibleStates(topology, cities);
		// showNeighbors(topology, cities);

		// probabilityfromCity(topology, td);

		// Initialize the vector arbitrarily
		for (State state : allStates) {
			v.put(state, new Double(0.0));
		}
		v.forEach((k, v) -> System.out.println("LLave: " + k + "valor: " + v));

		int diferencia = 1;
		double gama = 0.90;
		do {
			double qMax;
			for (State currentState : allStates) {
				for (ActionEntity currentAction : allActions) {
					// pickup action
					double sum = 0;

					if (currentState.getCurrentCity() != null
							&& currentAction.getAction() == ActionEntity.ActionKind.Collect) {
						for (State toState : allStates) {
							qMax = calculateCost(currentState, td, agent, currentAction);
							sum += transition(currentState, currentAction, toState, td) * v.getOrDefault(toState, 0.0);
						}
					}
					// moveaction
					else {

					}
				}
			}
		} while (diferencia < 0.000001);

		// -----------------------------------------------------------------
		Double discount = agent.readProperty("discount-factor", Double.class, 0.5);
		this.random = new Random();
		this.pPickup = discount;
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
			// System.out.println(availableTask);
		} else {
			action = new Pickup(availableTask);
			// System.out.println();

		}
		return action;
	}
}
