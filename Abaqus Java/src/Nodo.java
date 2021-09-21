import java.util.ArrayList;

public class Nodo {
    private int label;
    private double coordx;
    private double coordy;

    private double coordxMain;

    public Nodo(int label, double coordx, double coordy) {
        this.label = label;
        this.coordx = coordx;
        this.coordy = coordy;
    }

    public double getCoordxMain() {
        return coordxMain;
    }

    public void setCoordxMain(double coordxMain) {
        this.coordxMain = coordxMain;
    }

    public int getLabel() {
        return label;
    }

    public double getCoordx() {
        return coordx;
    }

    public double getCoordy() {
        return coordy;
    }

    @Override
    public String toString() {
        return "Nodo{" +
                "label=" + label +
                ", coordx=" + coordx +
                ", coordy=" + coordy +
                '}';
    }
}
