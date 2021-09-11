import java.util.ArrayList;

public class Elemento {
    private int label;
    private ArrayList<Integer> connectivy;

    public Elemento(int label, ArrayList<Integer> connectivy) {
        this.label = label;
        this.connectivy = connectivy;
    }

    public int getLabel() {
        return label;
    }

    public ArrayList<Integer> getConnectivy() {
        return connectivy;
    }

    @Override
    public String toString() {
        return String.valueOf(label);
    }
}
