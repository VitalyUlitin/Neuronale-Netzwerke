import java.util.ArrayList;
import java.util.Arrays;

public class Layers {
    private Neuron[] neurons;
    private double[][] weightMatrix;

    public Layers(int neuronNumbers, double[][] weightMatrix) {
        neurons = new Neuron[neuronNumbers];
        this.weightMatrix = weightMatrix;
    }

    public Layers(double[][] weightMatrix, int neuronNumbers) {
        this.weightMatrix = weightMatrix;
        neurons = new Neuron[neuronNumbers];
    }

    public double[][] getWeightMatrix() {
        return weightMatrix;
    }

    public void setWeightMatrix(double[][] weightMatrix) {
        this.weightMatrix = weightMatrix;
    }

    public Neuron[] getNeurons() {
        return neurons;
    }

    //receives a input from the layer above and inserts the values into the neurons
    public void input(double[] inputs) {
        if (inputs.length != neurons.length) {
            throw new IllegalArgumentException("Ungueltiges Input");
        }

        for (int i = 0; i < inputs.length; i++) {
            neurons[i].setValue(inputs[i]);
        }
    }
}
