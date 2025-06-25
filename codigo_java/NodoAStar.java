package tracks.singlePlayer.evaluacion.src_TORRES_FERNANDEZ_ELENA;

//import java.util.ArrayList;
import java.util.Objects;
import java.util.HashSet;

public class NodoAStar implements Comparable<NodoAStar>{

    Vector2d posicion;
    int coste;
    int heu;
    boolean capa_azul;
    boolean capa_roja;
    HashSet<Vector2d> capas_azules;
    HashSet<Vector2d> capas_rojas;
    NodoAStar padre;
    int orden;

    /**
     * Constructor por defecto
     */
    public NodoAStar(){
        this.posicion = new Vector2d(-1, -1);
        this.coste = Integer.MAX_VALUE;
        this.heu = Integer.MAX_VALUE;
        this.capa_azul = false;
        this.capa_roja = false;
        this.capas_azules = new HashSet<>();
        this.capas_rojas  = new HashSet<>();
        this.padre = null;
        this.orden = -1;
    }

    /**
     * Constructor de copia
     */
    public NodoAStar(NodoAStar otro_nodo){
        this.posicion = new Vector2d(otro_nodo.posicion);
        this.coste = otro_nodo.coste;
        this.heu = otro_nodo.heu;
        this.capa_azul = otro_nodo.capa_azul;
        this.capa_roja = otro_nodo.capa_roja;
        this.capas_azules = new HashSet<>(otro_nodo.capas_azules);
        this.capas_rojas  = new HashSet<>(otro_nodo.capas_rojas);
        this.padre = otro_nodo.padre;
        this.orden = otro_nodo.orden;
    }

    /**
     * Constructor con parametros
     * @param pos posicion en coordenadas grid
     * @param c_azul true si el avatar lleva la capa azul, false en caso contrario
     * @param c_roja true si el avatar lleva la capa roja, false en caso contrario
     * @param father padre del nodo
     * @param coste coste del nodo
     * @param heu heuristica para el nodo, es la distancia Manhattan
     * @param capas_rojas HashSet de posiciones que tienen capas rojas actualmente
     * @param capas_azules HashSet de posiciones que tienen capas azules actualmente
     * @param orden orden de creacion del nodo
     */
    public NodoAStar(Vector2d pos, int coste, int heu, boolean c_azul, boolean c_roja,
                     HashSet<Vector2d> capas_azules, HashSet<Vector2d> capas_rojas,
                     NodoAStar father, int orden){
        this.posicion = new Vector2d(pos);
        this.coste = coste;
        this.heu = heu;
        this.capa_azul = c_azul;
        this.capa_roja = c_roja;
        this.capas_azules = new HashSet<>(capas_azules);
        this.capas_rojas  = new HashSet<>(capas_rojas);
        this.padre = father;
        this.orden = orden;
    }

    /**
     * Ordena los nodos por el atributo coste+heuristica = f; en caso de empate, por g
     * Se usa en la PriorityQueue<NodoAStar> abtos, de donde se van sacando los nodos de menor f
     */
    @Override
    public int compareTo(NodoAStar otro) {

        int comparacion = Integer.compare(this.coste + this.heu, otro.coste + otro.heu);    // por f
        if (comparacion == 0) comparacion = Integer.compare(this.coste, otro.coste);        // por coste
        if (comparacion == 0) comparacion = Integer.compare(this.orden, otro.orden);        // por orden de creacion

        return comparacion;
    }

    /**
     * Un nodo es igual a otro si tienen la misma posicion, (la misma heuristica), los mismos valores en capa_azul y
     * capa_roja, y los mismos vectores de posiciones que tienen capas azules y rojas.
     * La heuristica solo depende de la posicion (si la pos es diferente, la heu tb)
     * Pueden tener distinto coste, distinto padre y distinto orden 
     * Se usa en la llamada al metodo contains desde el HashSet<NodoAStar> cerrados
     * @param obj Objeto con el que se compara la igualdad
     * @return true si son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {

        boolean iguales = true;
        NodoAStar otro_nodo = (NodoAStar) obj;

        if (!this.posicion.equals(otro_nodo.posicion)) iguales = false;
        else if (otro_nodo.capa_azul != this.capa_azul) iguales = false;
        else if (otro_nodo.capa_roja != this.capa_roja) iguales = false;
        else if (!otro_nodo.capas_azules.equals(this.capas_azules)) iguales = false;
        else if (!otro_nodo.capas_rojas.equals(this.capas_rojas)) iguales = false;

        return iguales;
    }

    /**
     * Sobreescribimos el metodo hashCode para usarlo dentro de la estrucutra HashSet<NodoAStar> visitados
     * @return entero que representa de forma unica al objeto
     */
    @Override
    public int hashCode() {
        return Objects.hash(posicion, capa_azul, capa_roja, capas_azules, capas_rojas);
    }
}

