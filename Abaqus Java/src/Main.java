import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();

        HashMap<Integer, Double> hfl = new HashMap<>();
        HashMap<Integer, Double> nt11 = new HashMap<>();
        HashMap<Integer, Elemento> elementos = new HashMap<>();
        ArrayList<Integer> elementosAire = new ArrayList<>();
        ArrayList<Integer> nodosAire = new ArrayList<>();
        ArrayList<Nodo> nodos = new ArrayList<>();

        double topTemperature = 400;
        double bottomTemperature = 25;
        int nodeSetLimit = 20;

        int spacing = 50;

        double maxQ = -999;
        double minQ = 999;

        double maxT = -999;
        double minT = 999;

        double minY = 999;
        double maxY = -999;
        double minX = 999;
        double maxX = -999;

        Scanner scanner = new Scanner(new File("hfl.txt"));
        while(scanner.hasNext()){
            String[] line = scanner.nextLine().split(" ");
            int elementlabel = Integer.parseInt(line[0]);
            double value = Double.parseDouble(line[1]);
            if(value > maxQ){
                maxQ = value;
            }
            if(value < minQ){
                minQ = value;
            }
            hfl.put(elementlabel, value);
        }

        System.out.println("maxQ: "+maxQ);
        System.out.println("minQ: "+minQ);

        scanner = new Scanner(new File("nt11.txt"));
        while(scanner.hasNext()){
            String[] line = scanner.nextLine().split(" ");
            int nodelabel = Integer.parseInt(line[0]);
            double value = Double.parseDouble(line[1]);
            if(value > maxT){
                maxT = value;
            }
            if(value < minT){
                minT = value;
            }
            nt11.put(nodelabel, value);
        }

        System.out.println("maxT: "+maxT);
        System.out.println("minT: "+minT);

        scanner = new Scanner(new File("elements.txt"));
        while(scanner.hasNext()){
            String[] line = scanner.nextLine().split(" ");
            int label = Integer.parseInt(line[0]);
            ArrayList<Integer> connectivy = new ArrayList<>();
            for(int i=1; i<line.length; i++){
                int j = Integer.parseInt(line[i].replace(",","").replace("(","").replace(")",""));
                connectivy.add(j);
            }
            elementos.put(label, new Elemento(label, connectivy));
        }

        System.out.println("elements size: "+ elementos.size());

        scanner = new Scanner(new File("nodes.txt"));
        while(scanner.hasNext()){
            String[] line = scanner.nextLine().replaceAll(" +", " ").split(" ");
            int label = Integer.parseInt(line[0]);
            double coordx = Double.parseDouble(line[1].replace(",","").replace("[","").replace("]",""));
            double coordy = Double.parseDouble(line[2].replace(",","").replace("[","").replace("]",""));
            nodos.add(new Nodo(label, coordx, coordy));
        }

        System.out.println("nodes size: "+ nodos.size());

        scanner = new Scanner(new File("airelems.txt"));
        while(scanner.hasNext()){
            String[] line = scanner.nextLine().split(" ");
            int elemento = Integer.parseInt(line[0]);
            elementosAire.add(elemento);
        }

        for (Integer integer : elementosAire) {
            Elemento elemento = elementos.get(integer);
            nodosAire.addAll(elemento.getConnectivy());
        }

        HashMap<Double, ArrayList<Nodo>> nodeSetList = new HashMap<>();

        for(Elemento elemento : elementos.values()){
            ArrayList<Integer> nodesElement = elemento.getConnectivy();
            Nodo nodoTake = nodos.get(nodesElement.get(0)-1);
            double coordx = nodoTake.getCoordx();
            double coordy = nodoTake.getCoordy();

            if(coordy > maxY){
                maxY = coordy;
            }
            if(coordy < minY){
                minY = coordy;
            }
            if(coordx > maxX){
                maxX = coordx;
            }
            if(coordx < minX){
                minX = coordx;
            }

            nodeSetList.computeIfAbsent(coordx, k -> new ArrayList<>());

            if(!nodeSetList.get(coordx).contains(nodoTake)){
                nodeSetList.get(coordx).add(nodoTake);
            }
        }

        System.out.println("min X: "+minX);
        System.out.println("max X: "+maxX);
        System.out.println("min Y: "+minY);
        System.out.println("max Y: "+maxY);

        int contador = 0;
        double spaceOf = (maxX-minX)/spacing;

        System.out.println("spaces of: "+spaceOf);
        System.out.println("number of sets found: "+nodeSetList.keySet().size());


        HashMap<Space, ArrayList<Nodo>> nodeSetListNew = new HashMap<>();
        double initx;
        double endx;

        for(int i=0; i<spacing; i++){
            initx = minX + i*spaceOf;
            endx = minX + (i+1)*spaceOf;

            nodeSetListNew.put(new Space(initx, endx), new ArrayList<>());
        }

        ArrayList<Integer> values = new ArrayList<>();
        ArrayList<Space> spacesList = new ArrayList<>(nodeSetListNew.keySet());

        for(Double coordX: nodeSetList.keySet()) {
            for(int i=0; i<spacesList.size(); i++){
                if(i == spacesList.size()-1){
                    if(coordX >= spacesList.get(i).getInitx() && coordX <= spacesList.get(i).getFinalx()){
                        nodeSetListNew.get(spacesList.get(i)).addAll(nodeSetList.get(coordX));
                    }
                }
                else{
                    if(coordX >= spacesList.get(i).getInitx() && coordX < spacesList.get(i).getFinalx()){
                        nodeSetListNew.get(spacesList.get(i)).addAll(nodeSetList.get(coordX));
                    }
                }
            }
        }

        for(Space spaceX: nodeSetListNew.keySet()) {
            values.add(nodeSetListNew.get(spaceX).size());
        }

        //System.out.println(values);
        System.out.println("max in sets: "+max(values));
        System.out.println("min in sets: "+min(values));

        System.out.println("number recommended to set limit: mode: " + mode(values)+ ", median: "+median(values) + ", mean: "+mean(values));
        System.out.println("node sets selected: "+nodeSetLimit);

        HashMap<Space, ArrayList<Elemento>> elementSetList = new HashMap<>();

        HashMap<Space, Double> coordYSet = new HashMap<>();
        HashMap<Space, Double> deltaTSet = new HashMap<>();

        ArrayList<Double> hflMagnitudes = new ArrayList<>();
        ArrayList<Double> hflMagnitudesY = new ArrayList<>();
        ArrayList<Double> hflMagnitudesYDT = new ArrayList<>();

        for(Space space: nodeSetListNew.keySet()){
            ArrayList<Nodo> nodeSet = nodeSetListNew.get(space);
            ArrayList<Integer> nodeSetIntegers = new ArrayList<>();
            for(Nodo value : nodeSet) {
                nodeSetIntegers.add(value.getLabel());
            }

            double minYI = 999;
            double maxYI = -999;

            if(nodeSet.size() > nodeSetLimit){
                double maxTemp = -9999;
                double minTemp = 9999;

                for(Nodo nodo: nodeSet){
                    double coordy = nodo.getCoordy();
                    if(coordy > maxYI){
                        maxYI = coordy;
                    }
                    if(coordy < minYI){
                        minYI = coordy;
                    }
                    if(nt11.get(nodo.getLabel()) > maxTemp){
                        maxTemp = nt11.get(nodo.getLabel());
                    }
                    if(nt11.get(nodo.getLabel()) < minTemp){
                        minTemp = nt11.get(nodo.getLabel());
                    }
                }

                if((maxYI - minYI)*1000000 > 0){
                    if(maxTemp != minTemp){
                        contador++;
                        for(Elemento elementoCoordX: elementos.values()){
                            //if(!elementosAire.contains(elementoCoordX.getLabel())){
                                boolean isin = false;
                                for(int k=0; k<elementoCoordX.getConnectivy().size(); k++){
                                    if (nodeSetIntegers.contains(elementoCoordX.getConnectivy().get(k)) && !nodosAire.contains(elementoCoordX.getConnectivy().get(k))) {
                                        isin = true;
                                        break;
                                    }
                                }
                                if(isin){
                                    elementSetList.computeIfAbsent(space, k -> new ArrayList<>());
                                    if(!elementSetList.get(space).contains(elementoCoordX)){
                                        elementSetList.get(space).add(elementoCoordX);
                                        coordYSet.put(space, maxYI-minYI);
                                        deltaTSet.put(space, maxTemp-minTemp);
                                    }
                                }
                            //}
                        }
                    }
                }
            }
        }

        for(Space coordXElement: elementSetList.keySet()){
            ArrayList<Elemento> elementSet = elementSetList.get(coordXElement);
            int countHFL = 0;
            int countPos = 0;
            double valueHFL = 0;

            for(Elemento elementInSet: elementSet){
                double hflvalue = hfl.get(elementInSet.getLabel());
                countHFL++;
                valueHFL = valueHFL + hflvalue;

//                if(hflvalue <= 0){
//                    countHFL++;
//                    valueHFL = valueHFL + hflvalue;
//                }
//                else{
//                    countPos++;
//                }
            }

            //System.out.println("Positivos: "+countPos);
            valueHFL = valueHFL / countHFL;
            double hflMagnitude = -valueHFL;
            double hflMagnitudeY = -valueHFL * coordYSet.get(coordXElement);
            double hflMagnitudeYDT = -valueHFL * coordYSet.get(coordXElement) / deltaTSet.get(coordXElement);

            hflMagnitudes.add(hflMagnitude);
            hflMagnitudesY.add(hflMagnitudeY);
            hflMagnitudesYDT.add(hflMagnitudeYDT);
        }

        System.out.println("node sets processed: "+contador);

        double ky = 0;
        double ky2 = 0;
        double ky3 = 0;

        for(Double hflMagnitude : hflMagnitudes) {
            ky = ky + hflMagnitude;
        }
        ky = ky/(double)hflMagnitudes.size() * (maxY - minY) / (topTemperature - bottomTemperature);

        for(Double hflMagnitude : hflMagnitudesY) {
            ky2 = ky2 + hflMagnitude;
        }
        ky2 = ky2/(double)hflMagnitudesY.size() / (topTemperature - bottomTemperature);

        for(Double hflMagnitude : hflMagnitudesYDT) {
            ky3 = ky3 + hflMagnitude;
        }
        ky3 = ky3/(double)hflMagnitudesYDT.size();

        //System.out.println("Ky: " + ky);
        //System.out.println("Ky2: " + ky2);
        System.out.println("Ky Method 3: " + ky3);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("time elapsed: " + (double)elapsedTime/1000 + "s");
    }

    public static int min(ArrayList<Integer> a){
        int min = 999;
        for (Integer integer : a) {
            if (min > integer) {
                min = integer;
            }
        }
        return min;
    }

    public static int max(ArrayList<Integer> a){
        int max = -999;
        for (Integer integer : a) {
            if (max < integer) {
                max = integer;
            }
        }
        return max;
    }

    public static int mode(ArrayList<Integer> a) {
        int maxValue = -1, maxCount = -1;

        for (int i = 0; i < a.size(); ++i) {
            int count = 0;
            for (Integer integer : a) {
                if (integer.equals(a.get(i))) ++count;
            }
            if (count > maxCount) {
                maxCount = count;
                maxValue = a.get(i);
            }
        }

        return maxValue;
    }

    public static double median(ArrayList<Integer> m) {
        int middle = m.size()/2;
        if (m.size()%2 == 1) {
            return m.get(middle);
        } else {
            return (m.get(middle-1) + m.get(middle)) / 2.0;
        }
    }

    public static double mean(ArrayList<Integer> m) {
        double sum = 0;
        for (Integer integer : m) {
            sum += integer;
        }
        return sum / m.size();
    }
}
