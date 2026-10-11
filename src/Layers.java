import java.util.ArrayList;
import java.util.Arrays;

public class Layers {
    /*private ArrayList<double[]> layers;

    public Layers() {
        layers = new ArrayList<double[]>();
    }

    public void setLayers(ArrayList<double[]> inputList) {
        layers = inputList;
    }

    public ArrayList<double[]> getLayers() {

        return layers;
    }

    public void addLayer(double[] layer) {
        layers.add(layer);
    }

    public String output() {
        return Arrays.toString(layers.getLast());
    }*/
    private Neuron[] neurons;
    private double[][] weightMatrix;

    public double[][] getWeightMatrix() {
        return weightMatrix;
    }

    public Neuron[] getNeurons() {
        return neurons;
    }
}
