package reactive.template;

import java.util.*;

import entity.ActionEntity;
import entity.State;
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
				if (possibleCurrentCities != possibleNeighbors) {
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

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) throws NullPointerException {
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
			v.put(state, 0.0);
		}
		v.forEach((k, v) -> System.out.println("LLave: " + k + "valor: " + v));

		int diferencia = 1;
		double gama = 0.90;
		do {
			for (State currentState : allStates) {
				for (ActionEntity currentAction : allActions) {

				}
			}
		} while (diferencia < 0.000001);

		// -----------------------------------------------------------------
		Double discount = agent.readProperty("discount-factor", Double.class, 0.5);
		this.random = new Random();
		this.pPickup = discount;
	}

	private void probabilityfromCity(Topology topology, TaskDistribution td) {
		List<City> cities = topology.cities();

		int numberOfCities = cities.size();
		for (int i = 0; i < numberOfCities; i++) {
			City cityFrom = cities.get(i);
			double cityProb = 0.0;
			for (int j = 0; j < numberOfCities; j++) {
				City cityTo = cities.get(j);
				System.out.println("Probability for task from " + cityFrom.name + " to" + cityTo.name + " = "
						+ td.probability(cityFrom, cityTo));
				System.out.println("Reward for task from " + cityFrom.name + " to" + cityTo.name + " = "
						+ td.reward(cityFrom, cityTo));

				cityProb += td.probability(cityFrom, cityTo);
			}
			System.out.println("----------------------------");
			System.out.println("Total Prob for " + cityFrom.name + " = " + cityProb);
			System.out
					.println("Prob of NO TASK: calc=" + (1 - cityProb) + " obtained=" + td.probability(cityFrom, null));
			System.out.println();
		}
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
