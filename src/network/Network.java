package network;

import neuron.BiasNeuron;
import neuron.LayerNeuron;
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

    public void backpropagation(double learningRate, double[] expectedOutput) {
        forwardPass();

        int lastLayerIndex = layers.length - 1;
        Neuron[] outputNeurons = layers[lastLayerIndex].getNeurons();

        for (int i = 0; i < outputNeurons.length; i++) {
            LayerNeuron neuron = (LayerNeuron) outputNeurons[i];
            double error = neuron.getValue() - expectedOutput[i];
            neuron.setDelta(error * neuron.getActivationDerivative());
        }

        for (int layerIndex = lastLayerIndex - 1; layerIndex > 0; layerIndex--) {
            Neuron[] currentNeurons = layers[layerIndex].getNeurons();
            Neuron[] nextNeurons = layers[layerIndex + 1].getNeurons();
            double[][] weights = layers[layerIndex].getWeightMatrix();

            for (int neuronIndex = 0; neuronIndex < currentNeurons.length; neuronIndex++) {
                if (currentNeurons[neuronIndex] instanceof BiasNeuron) {
                    continue;
                }

                LayerNeuron neuron = (LayerNeuron) currentNeurons[neuronIndex];
                double error = 0.0;

                for (int nextIndex = 0; nextIndex < nextNeurons.length; nextIndex++) {
                    if (!(nextNeurons[nextIndex] instanceof BiasNeuron)) {
                        LayerNeuron nextNeuron = (LayerNeuron) nextNeurons[nextIndex];
                        error += weights[neuronIndex][nextIndex] * nextNeuron.getDelta();
                    }
                }

                neuron.setDelta(error * neuron.getActivationDerivative());
            }
        }

        for (int layerIndex = 0; layerIndex < lastLayerIndex; layerIndex++) {
            Neuron[] currentNeurons = layers[layerIndex].getNeurons();
            Neuron[] nextNeurons = layers[layerIndex + 1].getNeurons();
            double[][] weights = layers[layerIndex].getWeightMatrix();

            for (int nextIndex = 0; nextIndex < nextNeurons.length; nextIndex++) {
                if (nextNeurons[nextIndex] instanceof BiasNeuron) {
                    continue;
                }

                LayerNeuron nextNeuron = (LayerNeuron) nextNeurons[nextIndex];

                for (int currentIndex = 0; currentIndex < currentNeurons.length; currentIndex++) {
                    weights[currentIndex][nextIndex] -= learningRate
                            * nextNeuron.getDelta()
                            * currentNeurons[currentIndex].getValue();
                }
            }
        }
    }

    public TrainingResult train(double[][] inputs, double[][] expectedOutputs,
                                int maxEpochs, double learningRate, double tolerance) {
        double totalError = Double.POSITIVE_INFINITY;

        for (int epoch = 1; epoch <= maxEpochs; epoch++) {
            for (int i = 0; i < inputs.length; i++) {
                setInput(inputs[i]);
                backpropagation(learningRate, expectedOutputs[i]);
            }

            totalError = calculateTotalError(inputs, expectedOutputs);

            if (totalError <= tolerance) {
                return new TrainingResult(epoch, totalError, true);
            }
        }
        return new TrainingResult(maxEpochs, totalError, false);
    }

    private double calculateTotalError(double[][] inputs, double[][] expectedOutputs) {
        double totalError = 0.0;

        for (int i = 0; i < inputs.length; i++) {
            setInput(inputs[i]);
            double[] actualOutput = forwardPass();

            for (int j = 0; j < actualOutput.length; j++) {

                double difference = expectedOutputs[i][j] - actualOutput[j];
                totalError += 0.5 * difference * difference;
            }
        }

        return totalError;
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