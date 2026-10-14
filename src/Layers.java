import java.util.Random;

public class Layers {
    private Neuron[] neurons;
    private double[][] weightMatrix;

    public Layers(int neuronNumbers) {
        neurons = new Neuron[neuronNumbers];
    }

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

    public void generateRandomWeightMatrix(int nextNeuronLength) {
        Random rand = new Random();

        weightMatrix = new double[nextNeuronLength][neurons.length];

        for (int i = 0; i < weightMatrix.length; i++) {
            for (int j = 0; j < weightMatrix[0].length; j++) {
                weightMatrix[i][j] = rand.nextDouble(-1, 1);
            }
        }
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
            if (!(neurons[i] instanceof BiasNeuron)) {
                neurons[i].setValue(inputs[i]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Neuronen:\n[");

        for (Neuron neuron : neurons) {
            out.append(neuron.getValue()).append(",");
        }

        out.append("]\n\nGewichte:\n");

        for (int i = 0; i < weightMatrix.length; i++) {
            out.append("[");
            for (int j = 0; j < weightMatrix[i].length; j++) {
                out.append(weightMatrix[i][j]).append(",");
            }
            out.append("]");
        }

        return out.toString();
    }
}
