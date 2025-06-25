package tracks.singlePlayer.evaluacion.src_TORRES_FERNANDEZ_ELENA;

import core.player.AbstractPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Stack;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class AgenteLRTAStar extends AbstractPlayer {

    Vector2d fescala;
    int gridWidth;
    int gridHeight;
    int tamanioRuta;
    int num_nodos_expandidos;
    int orden_nodos;
    Vector2d portal;
    char[][] mapaObstaculos;
    HashMap<Nodo, Integer> heu_dinamicas;
    HashSet<Vector2d> capas_rojas = new HashSet<>();
    HashSet<Vector2d> capas_azules = new HashSet<>();
    Stack<Types.ACTIONS> plan = new Stack<>();
    long tInicio;
    Nodo actual;
	
	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteLRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
        int x = 0;
        int y = 0;
        Observation obs;
        Observation recurso;

        // Inicializamos las variables simples
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

        // Almacenamos obstaculos en varios mapas
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

        // inicializamos el hashmap de las heuristicas dinamicas (vacio)
        heu_dinamicas = new HashMap<Nodo, Integer>();

        // Iniciamos el tiempo en el constructor
        tInicio = System.nanoTime();

        // Inicializamos el nodo actual con la posicion de partida del avatar y lo incluimos en el hashmap
        Vector2d pos_avatar = new Vector2d(stateObs.getAvatarPosition().x / fescala.x,
                                            stateObs.getAvatarPosition().y / fescala.y);                         
        actual = new Nodo(pos_avatar, 0, false, false, capas_azules, capas_rojas, null, orden_nodos);
        heu_dinamicas.put(actual, (int) (Math.abs(actual.posicion.x - portal.x) + Math.abs(actual.posicion.y - portal.y)));
	}

    /**
	 * Genera sucesores validos del nodo pasado como parametro
	 * @param actual Nodo del cual se calculan sus sucesores
     * @return ArrayList<Nodo> con los nodos de los sucesores
	 */
    public ArrayList<Nodo> generar_sucesores(Nodo actual){
        
        ArrayList<Nodo> sucesores = new ArrayList<>();
        int num_sucesores = 4;
        Vector2d newpos;
        int new_x, new_y;
        boolean new_capa_azul;
        boolean new_capa_roja;
        HashSet<Vector2d> new_capas_rojas;
        HashSet<Vector2d> new_capas_azules;
        
        // generamos hasta 4 sucesores
        for (int s=0; s<num_sucesores; s++) {

            // calculamos la nueva posicion
            switch(s){
                case 0: newpos = new Vector2d(actual.posicion.x +1, actual.posicion.y); break; // right
                case 1: newpos = new Vector2d(actual.posicion.x -1, actual.posicion.y); break; // left
                case 2: newpos = new Vector2d(actual.posicion.x, actual.posicion.y -1); break; // up
                case 3: newpos = new Vector2d(actual.posicion.x, actual.posicion.y +1); break; // down
                default: newpos = new Vector2d(actual.posicion); break;
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
                new_capa_azul = actual.capa_azul;
                new_capa_roja = actual.capa_roja;
                new_capas_rojas = new HashSet<>(actual.capas_rojas);
                new_capas_azules = new HashSet<>(actual.capas_azules);

                // eliminamos la posicion de la capa una vez cogida
                // no se pueden tener dos capas a la vez
                if (new_capas_rojas.remove(newpos)) {
                    new_capa_roja = true;
                    new_capa_azul = false;
                }
                if (new_capas_azules.remove(newpos)){
                    new_capa_azul = true;
                    new_capa_roja = false;
                }
            
                // aniadimos el nodo sucesor
                orden_nodos = orden_nodos +1;
                sucesores.add(new Nodo(newpos, actual.coste + 1, new_capa_azul, new_capa_roja,
                                       new_capas_azules, new_capas_rojas, actual, orden_nodos));
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
        ArrayList<Nodo> sucesores;
        ArrayList<Integer> f_sucesores = new ArrayList<>();
        ArrayList<Integer> f_sucesores_ordenado = new ArrayList<>();
        int idxMin, heuMin, heuActual;

        f_sucesores.clear();
        f_sucesores_ordenado.clear();
        num_nodos_expandidos = num_nodos_expandidos + 1;

        if (!actual.posicion.equals(portal))
        {
            // generamos los sucesores si el nodo actual no es el objetivo
            sucesores = generar_sucesores(actual);
            Nodo sucesor;

            // por cada sucesor, calculamos su f (estrategia de movimiento)
            for (int i=0; i<sucesores.size(); i++){
                sucesor = sucesores.get(i);

                // si el sucesor no esta en la tabla hash lo aniadimos con el valor de la distancia de manhattan
                if (heu_dinamicas.get(sucesor) == null){
                    heu_dinamicas.put(sucesor, (int) (Math.abs(sucesor.posicion.x - portal.x)
                                                    + Math.abs(sucesor.posicion.y - portal.y)));
                }

                // calculamos la f del sucesor
                f_sucesores.add(heu_dinamicas.get(sucesores.get(i)) + 1);
                f_sucesores_ordenado.add(i);
            }

            // ordenamos la f de los sucesores
            f_sucesores_ordenado.sort(Comparator.comparingInt(f_sucesores::get));

            // determinamos el (primer) minimo de sucesores y actualizamos el valor heuristico del nodo actual
            // (regla de aprendizaje)
            heuActual = heu_dinamicas.get(actual);
            idxMin = f_sucesores_ordenado.get(0);
            heuMin = heu_dinamicas.get(sucesores.get(idxMin));
            
            if (heuMin +1 > heuActual)
                heu_dinamicas.replace(actual, heuActual, heuMin +1);

            // nos movemos al mejor vecino
            actual = sucesores.get(idxMin);

            // calculamos la siguiente accion
            returned_action = calcularAction(actual.posicion, actual.padre.posicion);
            tamanioRuta = tamanioRuta + 1;

            // si hemos llegado al objetivo
            if (actual.posicion.equals(portal))
            {
                // obtenemos el tiempo de calculo del plan
                long tFin = System.nanoTime();
                long tiempoTotalms = (tFin - tInicio)/1000000;
                System.out.println("Runtime LRTAStar (ms): " + tiempoTotalms);

                // imprimimos los resultados pedidos
                System.out.println("Tamaño de la ruta calculada: " + tamanioRuta);
                System.out.println("Número de nodos expandidos: " + num_nodos_expandidos);
            }
        }
   
        return returned_action;
	}
}
