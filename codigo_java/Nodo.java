package tracks.singlePlayer.evaluacion.src_TORRES_FERNANDEZ_ELENA;

import java.util.HashSet;
import java.util.Objects;

public class Nodo implements Comparable<Nodo>{

    Vector2d posicion;
    int coste;
    boolean capa_azul;
    boolean capa_roja;
    HashSet<Vector2d> capas_azules;
    HashSet<Vector2d> capas_rojas;
    Nodo padre;
    int orden;

    /**
     * Constructor por defecto
     */
    public Nodo(){
        this.posicion = new Vector2d(-1, -1);
        this.coste = Integer.MAX_VALUE;
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
    public Nodo(Nodo otro){
        this.posicion = new Vector2d(otro.posicion);
        this.coste = otro.coste;
        this.capa_azul = otro.capa_azul;
        this.capa_roja = otro.capa_roja;
        this.capas_azules = new HashSet<>(otro.capas_azules);
        this.capas_rojas  = new HashSet<>(otro.capas_rojas);
        this.padre = otro.padre;
        this.orden = otro.orden;
    }

    /**
     * Constructor con parametros
     * @param pos posicion en coordenadas grid
     * @param coste coste del nodo
     * @param c_azul true si el avatar lleva la capa azul, false en caso contrario
     * @param c_roja true si el avatar lleva la capa roja, false en caso contrario
     * @param capas_azules HashSet de posiciones que tienen capas azules actualmente
     * @param capas_rojas HashSet de posiciones que tienen capas rojas actualmente
     * @param father padre del nodo
     * @param orden orden de creacion del nodo
     */
    public Nodo(Vector2d pos, int coste, boolean c_azul, boolean c_roja,
                HashSet<Vector2d> capas_azules, HashSet<Vector2d> capas_rojas,
                Nodo father, int orden){
        this.posicion = new Vector2d(pos);
        this.coste = coste;
        this.capa_azul = c_azul;
        this.capa_roja = c_roja;        
        this.capas_rojas = new HashSet<>(capas_rojas);
        this.capas_azules = new HashSet<>(capas_azules);
        this.padre = father;
        this.orden = orden;
    }

    /**
     * Ordena los nodos por el atributo coste, si empatan se sigue el orden de creacion del nodo.
     * Se usa en la PriorityQueue<Nodo> no_visitados, de donde se van sacando los nodos de mejor coste.
     * @param otro Nodo con el que compara segun el atributo coste
     * @return entero: negativo si es menor, positivo si es mayor (nunca seran iguales)
     */
    @Override
    public int compareTo(Nodo otro) {

        int comparacion = Integer.compare(this.coste, otro.coste);                      // por coste
        if (comparacion == 0) comparacion = Integer.compare(this.orden, otro.orden);    // por orden de creacion

        return comparacion;
    }

    /**
     * Un nodo es igual a otro si tienen la misma posicion, capa_azul, capa_roja, capas_azules y capas_rojas.
     * Pueden tener distinto padre, distinto coste y distinto orden.
     * Se usa en la llamada al metodo contains desde el HashSet<Nodo> visitados.
     * @param obj Objeto con el que se compara la igualdad
     * @return true si son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {

        boolean iguales = true;
        Nodo otro_nodo = (Nodo) obj;

        // si alguna condicion es cierta, no son iguales y devuelve false
        if (!this.posicion.equals(otro_nodo.posicion)) iguales = false;
        else if (otro_nodo.capa_azul != this.capa_azul) iguales = false;
        else if (otro_nodo.capa_roja != this.capa_roja) iguales = false;
        else if (!otro_nodo.capas_azules.equals(this.capas_azules)) iguales = false;
        else if (!otro_nodo.capas_rojas.equals(this.capas_rojas)) iguales = false;
        
        return iguales;
    }

    /**
     * Sobreescribimos el metodo hashCode para usarlo dentro de la estrucutra HashSet<Nodo> visitados
     * @return entero que representa de forma unica al objeto
     */
    @Override
    public int hashCode() {
        return Objects.hash(posicion, capa_azul, capa_roja, capas_azules, capas_rojas);
    }
}
