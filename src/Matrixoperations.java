public class Matrixoperations {
    public static double[] matrixmultiplication(Layers layer, int nextInputLength) {
        double[] nextNeuronInputs = new double[nextInputLength];
        //retrieve the current neuron vector and weightMatrix
        Neuron[] neurons = layer.getNeurons();
        double[] neuronValues = new double[neurons.length];
        double[][] weightMatrix = layer.getWeightMatrix();

        //fill neuronValues with the corresponding values
        for (int i = 0; i < neuronValues.length; ++i) {
            neuronValues[i] = neurons[i].getValue();
        }

        if (weightMatrix.length == 0 || weightMatrix[0].length != neuronValues.length) {
            throw new IllegalArgumentException(
                    "Gewichtsmatrix und Layer haben unterschiedlichen Laengen"
            );
        }

        for (int i = 0; i < weightMatrix.length; i++) {
            for (int j = 0; j < neuronValues.length; j++) {
                nextNeuronInputs[i] = nextNeuronInputs[i] + (neuronValues[j] * weightMatrix[i][j]);
            }
        }

        return nextNeuronInputs;
    }
}

