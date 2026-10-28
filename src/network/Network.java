package network;

import neuron.Neuron;

public class Network {

    private final Layer[] layers;

    Network(Layer[] layers) {
        this.layers = layers;
    }

    public int getInputSize() {
        return layers[0].getNormalNeuronCount();
    }

    public int getOutputSize() {
        return layers[layers.length - 1].getNormalNeuronCount();
    }

    public void setInput(double[] input) {
        layers[0].input(input);
    }

    public void setWeights(double[][][] weights) {
        if (weights.length != layers.length - 1) {
            throw new IllegalArgumentException(
                    "Anzahl der Gewichtsmatrizen passt nicht zum Netzwerk"
            );
        }

        for (int i = 0; i < weights.length; i++) {
            int expectedSources = layers[i].getNeurons().length;

            int expectedTargets = layers[i + 1].getNormalNeuronCount();

            if (weights[i].length != expectedSources) {
                throw new IllegalArgumentException(
                        "Falsche Zeilenanzahl in Gewichtsmatrix "
                                + (i + 1)
                );
            }

            for (double[] row : weights[i]) {
                if (row.length != expectedTargets) {
                    throw new IllegalArgumentException(
                            "falsche Spltenanzahl in Gewichtsmatrix " + (i + 1)
                    );
                }
            }

            layers[i].setWeightMatrix(weights[i]);
        }
    }

    public void initRandomWeights() {
        for (int i = 0; i < layers.length - 1; i++) {
            layers[i].generateRandomWeightMatrix(layers[i + 1].getNormalNeuronCount());
        }
    }

    public double[] forwardPass() {
        for (int i = 0; i < layers.length - 1; i++) {
            double[] next = MatrixOperations.matrixmultiplication(layers[i]);
            layers[i + 1].input(next);
        }

        return getOutput();
    }

    public double[] getOutput() {
        Neuron[] neurons = layers[layers.length - 1].getNeurons();

        double[] output = new double[neurons.length];

        for (int i = 0; i < neurons.length; i++) {
            output[i] = neurons[i].getValue();
        }

        return output;
    }

    public Layer[] getLayers() {
        return layers;
    }
}