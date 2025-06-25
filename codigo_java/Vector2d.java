package tracks.singlePlayer.evaluacion.src_TORRES_FERNANDEZ_ELENA;
import java.util.Objects;

class Vector2d implements Comparable<Vector2d> {
    double x, y;

    // constructor con parametros
    Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // constructor de copia
    public Vector2d(Vector2d position) {
        this.x = position.x;
        this.y = position.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector2d)) return false;
        Vector2d v = (Vector2d) obj;
        return x == v.x && y == v.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Vector2d o) {
        if (this.x != o.x) return Double.compare(this.x, o.x);
        return Double.compare(this.y, o.y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

