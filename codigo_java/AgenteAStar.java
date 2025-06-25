package tracks.singlePlayer.evaluacion.src_TORRES_FERNANDEZ_ELENA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Iterator;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class AgenteAStar extends AbstractPlayer{

    Vector2d fescala;
    int gridWidth;
    int gridHeight;
    int tamanioRuta;
    int num_nodos_expandidos;
    int orden_nodos;
    boolean hay_plan;
    Vector2d portal;
    char[][] mapaObstaculos;
    HashSet<Vector2d> capas_rojas = new HashSet<>();
    HashSet<Vector2d> capas_azules = new HashSet<>();
    Stack<Types.ACTIONS> plan = new Stack<>();
	
	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
        int x = 0;
        int y = 0;
        Observation obs;
        Observation recurso;

        // Inicializamos variables simples
        hay_plan = false;
        tamanioRuta = 0;
        num_nodos_expandidos = 0;
        orden_nodos = 0;

		// Calculamos el factor de escala entre mundos (pixeles -> grid)
        gridWidth  = (int) stateObs.getObservationGrid().length;
        gridHeight = (int) stateObs.getObservationGrid()[0].length;
        fescala = new Vector2d(stateObs.getWorldDimension().width / gridWidth , 
        		               stateObs.getWorldDimension().height / gridHeight);
      
        // Obtenemos las coordenadas grid del portal de la salida
        ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
        portal = new Vector2d(posiciones[0].get(0).position.x, posiciones[0].get(0).position.y);
        portal.x = Math.floor(portal.x / fescala.x);
        portal.y = Math.floor(portal.y / fescala.y);

        // Almacenamos los obstaculos en un mapa
        mapaObstaculos = new char[gridWidth][gridHeight];
        ArrayList<Observation>[] immovable = stateObs.getImmovablePositions();

        for (int i=0; i< immovable.length; i++){
            for (int j=0; j<immovable[i].size(); j++){
                obs = immovable[i].get(j);
                x = (int) Math.floor(obs.position.x / fescala.x);
                y = (int) Math.floor(obs.position.y / fescala.y);

                if (obs.itype == 3 || obs.itype == 5) { 
                    mapaObstaculos[x][y] = 'm';         // trampas y muros
                }
                else if (obs.itype == 6){               
                    mapaObstaculos[x][y] = 'r';         // muros rojos
                }
                else if (obs.itype == 7){               
                    mapaObstaculos[x][y] = 'a';         // muros azules
                }
            }
        }

        // Obtenemos posiciones de las capas azules y rojas
        ArrayList<Observation>[] recursos = stateObs.getResourcesPositions();

        for (int i=0; i< recursos.length; i++){
            for (int j=0; j<recursos[i].size(); j++){
                recurso = recursos[i].get(j);
                x = (int) Math.floor(recurso.position.x / fescala.x);
                y = (int) Math.floor(recurso.position.y / fescala.y);

                if (recurso.itype == 8) {
                    capas_rojas.add( new Vector2d(x, y) );
                }
                else if (recurso.itype == 9) {
                    capas_azules.add( new Vector2d(x, y) );
                }
            }
        }
	}

    /**
	 * Genera sucesores validos del nodo pasado como parametro
	 * @param actual NodoAStar del cual se calculan sus sucesores
     * @return ArrayList<NodoAStar> con los nodos de los sucesores
	 */
    public ArrayList<NodoAStar> generar_sucesores(NodoAStar actual){
        
        ArrayList<NodoAStar> sucesores = new ArrayList<>();
        int num_sucesores = 4;
        Vector2d newpos;
        int nuevoCoste, heu, new_x, new_y;

        // generamos hasta 4 sucesores
        for (int s=0; s<num_sucesores; s++) {

            // calculamos la nueva posicion
            switch(s){
                case 0: newpos = new Vector2d(actual.posicion.x +1, actual.posicion.y); break; // right
                case 1: newpos = new Vector2d(actual.posicion.x -1, actual.posicion.y); break; // left
                case 2: newpos = new Vector2d(actual.posicion.x, actual.posicion.y -1); break; // up
                case 3: newpos = new Vector2d(actual.posicion.x, actual.posicion.y +1); break; // down
                default: newpos = actual.posicion; break;
            }
            
            // aniadimos el nodo si no es una casilla prohibida o intransitable
            new_x = (int)newpos.x;
            new_y = (int)newpos.y;

            if ((newpos.x < 0 || newpos.x >= gridWidth || newpos.y < 0 || newpos.y >= gridHeight) ||    // bordes del mapa
                (mapaObstaculos[new_x][new_y] == 'm') ||                                                // muros grises
                (mapaObstaculos[new_x][new_y] == 'a' && !actual.capa_azul) ||                           // muro azul sin capa azul
                (mapaObstaculos[new_x][new_y] == 'r' && !actual.capa_roja) ) {                          // muro rojo sin capa roja
                continue;
            }
            else { // actualizamos el resto de atributos

                // heuristica de manhattan, coste y orden de creacion
                heu = (int) (Math.abs(new_x - portal.x) + Math.abs(new_y - portal.y));
                nuevoCoste = actual.coste + 1;
                orden_nodos = orden_nodos + 1;

                // atributos de las capas:
                // eliminamos la posicion de la capa una vez cogida
                // no se pueden tener dos capas a la vez
                if (actual.capas_rojas.contains(newpos)) {
                    HashSet<Vector2d> new_capas_rojas = new HashSet<>(actual.capas_rojas);
                    new_capas_rojas.remove(newpos);

                    // creamos sucesor
                    sucesores.add(new NodoAStar(newpos, nuevoCoste, heu, false, true,
                                                actual.capas_azules, new_capas_rojas, actual, orden_nodos));
                }
                else if (actual.capas_azules.contains(newpos)) {
                    HashSet<Vector2d> new_capas_azules = new HashSet<>(actual.capas_azules);
                    new_capas_azules.remove(newpos);

                    // creamos sucesor
                    sucesores.add(new NodoAStar(newpos, nuevoCoste, heu, true, false,
                                                new_capas_azules, actual.capas_rojas, actual, orden_nodos));
                }
                else{ 
                    // no cambia ningun atributo de las capas
                    sucesores.add(new NodoAStar(newpos, nuevoCoste, heu, actual.capa_azul, actual.capa_roja,
                                                actual.capas_azules, actual.capas_rojas, actual, orden_nodos));
                }
            }
        }

        return sucesores;
    }

    /**
     * Calcula la accion que al ser ejecutada lleva al avatar desde la pos_anterior a la pos_actual
     * @param actual Vector2d con la posicion actual
     * @param anterior Vector2d con la posicion anterior
     * @return Types.ACTIONS
     */
    Types.ACTIONS calcularAction(Vector2d pos_actual, Vector2d pos_anterior){

        Types.ACTIONS accion = Types.ACTIONS.ACTION_NIL;

        if (pos_actual.x == pos_anterior.x) {
            if (pos_actual.y > pos_anterior.y) accion = Types.ACTIONS.ACTION_DOWN;
            else accion = Types.ACTIONS.ACTION_UP;
        }
        else{
            if (pos_actual.x > pos_anterior.x) accion = Types.ACTIONS.ACTION_RIGHT;
            else accion = Types.ACTIONS.ACTION_LEFT;
        }

        return accion;
    }
	
	/**
	 * return the best action to arrive faster to the closest portal
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return best	ACTION to arrive faster to the closest portal
	 */
	@Override
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        Types.ACTIONS returned_action = Types.ACTIONS.ACTION_NIL;

        // si no hay plan, lo calculamos y devolvemos la primera accion
        if (!hay_plan)
        {
            // inicio del calculo del plan
            long tInicio = System.nanoTime();

            // creamos el nodo inicial con la posicion del avatar
            Vector2d pos_avatar = new Vector2d(stateObs.getAvatarPosition().x / fescala.x,
                                               stateObs.getAvatarPosition().y / fescala.y);       
            int heu = (int) (Math.abs((int)pos_avatar.x - portal.x) + Math.abs((int)pos_avatar.y - portal.y));                
            NodoAStar inicial = new NodoAStar(pos_avatar, 0, heu, false, false,
                                              capas_azules, capas_rojas, null, orden_nodos);

            // creamos una cola con prioridad de nodos abtos y aniadimos el nodo inicial
            // se ordena poniendo los nodos de menor f primero
            PriorityQueue<NodoAStar> abtos = new PriorityQueue<>();
            abtos.add(inicial);

            // creamos un array de cerrados vacio
            HashSet<NodoAStar> cerrados = new HashSet<>();

            NodoAStar actual, sucesor, nodo;
            ArrayList<NodoAStar> sucesores;
            Types.ACTIONS next_action;
            Iterator<NodoAStar> it;

            // mientras queden nodos por visitar
            while(!abtos.isEmpty()) {

                // elegimos el nodo de abtos con menor coste y lo metemos en cerrados
                actual = abtos.remove();
                cerrados.add(actual);
                num_nodos_expandidos = num_nodos_expandidos + 1;

                if (actual.posicion.x != portal.x || actual.posicion.y != portal.y) // no es el objetivo
                {
                    // generamos sus sucesores                    
                    sucesores = generar_sucesores(actual);

                    // para cada sucesor:
                    for (int s=0; s < sucesores.size(); s++) {
                        sucesor = sucesores.get(s);

                        // si no esta en abtos ni cerrados, se aniade a abtos
                        if (!cerrados.contains(sucesor))
                            if (!abtos.contains(sucesor))
                                abtos.add(sucesor);
                            else{
                                // si esta en abtos
                                it = abtos.iterator();
                                while (it.hasNext()) {
                                    nodo = it.next();
                                    // se actualiza si el sucesor encontrado tiene mejor coste
                                    if (nodo.equals(sucesor) && nodo.coste > sucesor.coste){
                                        //System.out.println("mejora del coste en abtos");
                                        abtos.remove(nodo);
                                        abtos.add(sucesor);
                                    }
                                }
                            }
                    }
                }
                else // el nodo actual es el objetivo
                {
                    // creamos el plan recorriendo los padres desde el nodo objetivo
                    while(actual.padre != null){
                        next_action = calcularAction(actual.posicion, actual.padre.posicion);
                        plan.push(next_action);
                        actual = actual.padre;
                        tamanioRuta = tamanioRuta + 1;
                    }

                    // obtenemos el tiempo de calculo del plan
                    long tFin = System.nanoTime();
                    long tiempoTotalms = (tFin - tInicio)/1000000;
                    System.out.println("Runtime AStar (ms): " + tiempoTotalms);

                    // imprimimos los resultados pedidos
                    System.out.println("Tamaño de la ruta calculada: " + tamanioRuta);
                    System.out.println("Número de nodos expandidos: " + num_nodos_expandidos);
                    System.out.println("Número de nodos abiertos: " + abtos.size());
                    System.out.println("Número de nodos cerrados: " + cerrados.size());

                    // cogemos la primera accion del plan
                    hay_plan = true;
                    abtos.clear();
                    returned_action = plan.pop();
                }
            }
        }
        else // si hay plan, devolvemos la siguiente accion
        {
            if (!plan.isEmpty()) returned_action = plan.pop();
        }

        return returned_action;
	}
}
