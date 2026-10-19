package network;

import neuron.BiasNeuron;
import neuron.LayerNeuron;
import neuron.Neuron;

import java.util.Random;

public class Layer {

    private final Neuron[] neurons;
    private double[][] weightMatrix;

    public Layer(int neuronCount) {
        neurons = new Neuron[neuronCount];
    }

    public Neuron[] getNeurons() {
        return neurons;
    }

    public double[][] getWeightMatrix() {
        return weightMatrix;
    }

    public void setWeightMatrix(double[][] weightMatrix) {
        this.weightMatrix = weightMatrix;
    }

    public int getNormalNeuronCount() {
        int count = 0;

        for (Neuron neuron : neurons) {
            if (!(neuron instanceof BiasNeuron)) {
                count++;
            }
        }

        return count;
    }

    public void input(double[] inputs) {

        if (inputs.length != getNormalNeuronCount()) {
            throw new IllegalArgumentException(
                    "Input passt nicht zum layer"
            );
        }

        int inputIndex = 0;

        for (Neuron neuron : neurons) {
            if (neuron instanceof LayerNeuron layerNeuron) {
                layerNeuron.setValue(inputs[inputIndex++]);
            }
        }
    }

    public void generateRandomWeightMatrix(int targetNeuronCount) {

        Random random = new Random();
        weightMatrix = new double[neurons.length][targetNeuronCount];

        for (int i = 0; i < weightMatrix.length; i++) {
            for (int j = 0;
                 j < weightMatrix[i].length; j++) {
                weightMatrix[i][j] = random.nextDouble(-1, 1);
            }
        }
    }
}