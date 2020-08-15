# Explicación del Agente Reactivo

Para este proyecto se crearon dos entidades: **ESTADO** y **ACCION**, la primera es ESTADO que contiene la ciudad
actual y la ciudad destino. Después ACCIÓN tiene un destino y se uso ENUM que es una lista de constantes con
nombre que define un nuevo tipo de datos. Las constantes de enumeración declaradas fueron _MOVE_ Y _PICK UP_.

### Métodos implementados

##### allPossibleStates

Añade las ciudades a un LISTA ENLAZADA con todos los posibles destinos,
verificando que la misma ciudad que este en CIUDAD ACTUAL no sea la CIUDAD DESTINO.

#### allPossibleActions

Añade las ciudades a un LISTA ENLAZADA con todas las posibles acciones: PICK UP y MOVE.

#### actionsPossible

Añade las acciones a una LISTA ENLAZADA, es como una doble verificación, aparte que tambien verifica si la ciudad ingresada tiene como vecino a la ciudad destino usando el metodo has Neigbor de LOGIST.

#### calculateCost

Devuelve un double y calcula el costo dependiendo la acción que se de, ya sea PICK UP o MOVE, esta misma quita dinero cuando la acción es **MOVE** viendo la distancia de una ciudad a otra multiplicandola por el costo por km. Para **PICK UP** calcula la recompensa usando **reward** menos la distancia de la ciudad a la ciudad destino multiplicando por el costo por km.

#### transition

Devuelve un double y ve la probabilidad que hay de una ciudad a otra, aparte de ver ciertos factores que hagan que la probabilidad sea 0

#### methodForReinforcementLearnig

El método sigue la siguiente estructura:
![Image of Example](https://github.com/pablin2402/Reactivo-RIVAS/blob/master/images/template.png)

#### Action act(Vehicle vehicle, Task availableTask)

El código Action que implemente es el siguiente:

```java
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
```

Lo que hace es bastante simple, ve si la tarea disponible es falsa y si es asi se mueve, antes de eso se llama al objeto Acción donde llamo a **estrategia.get(new State(currentcity, availableTask == null ? null : availableTask.deliveryCity))** para recuperar la mejor accion a realizar. Dicha accion recuperada va a MOVE y le dice donde ir, caso contrario que _availabletask_ no sea null simplemente lo recoge.

# Resultados

Los resultados son los siguientes:

![Image of Example 2](https://github.com/pablin2402/Reactivo-RIVAS/blob/master/images/graph_095.png)

Para ejecutar este proyecto basta con ejecutar el siguiente comando:

    gradlew run

on Windows, or

     ./gradlew run

on a UNIX operating system.

The command line arguments can be edited in the `build.gradle` file:

    run.setArgsString('-a config/agents.xml config/reactive.xml reactive-random')

New agents should be added to the `config/agents.xml` file, and the agent's
participation should be triggered by adding the name of the agent to the end
of the argument list.
