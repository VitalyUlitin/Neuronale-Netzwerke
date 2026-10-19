package network;

import neuron.Neuron;

public final class MatrixOperations {

    public static double[] matrixmultiplication(Layer layer) {
        Neuron[] neurons = layer.getNeurons();

        double[][] weights = layer.getWeightMatrix();

        if (weights == null) {
            throw new IllegalStateException(
                    "Keine Gewichtsmatrix!"
            );
        }

        if (weights.length != neurons.length) {
            throw new IllegalArgumentException(
                    "Gewichtsmatrix passt nicht zum layer!"
            );
        }

        int targetCount = weights[0].length;

        double[] output = new double[targetCount];

        for (int source = 0; source < neurons.length; source++) {
            if (weights[source].length != targetCount) {
                throw new IllegalArgumentException(
                        "gewichtsmatrix nicht rechteckig"
                );
            }

            for (int target = 0; target < targetCount; target++) {

                output[target] += neurons[source].getValue() * weights[source][target];
            }
        }

        return output;
    }
}