package network;

import neuron.BiasNeuron;
import neuron.Function;
import neuron.LayerNeuron;

public final class NetworkFactory {

    private NetworkFactory() {}

    public static Network create(Function[][] topology) {

        if (topology == null || topology.length < 2) {
            throw new IllegalArgumentException(
                    "Ein Netzwerk braucht mindestens Input- und Output-Layer"
            );
        }

        Layer[] layers = new Layer[topology.length];

        for (int i = 0; i < topology.length; i++) {
            if (topology[i] == null || topology[i].length == 0) {
                throw new IllegalArgumentException(
                        "Layer " + i + " besitzt keine Neuronen."
                );
            }

            boolean hasBias = i < topology.length - 1;

            int neuronCount = topology[i].length + (hasBias ? 1 : 0);

            layers[i] = new Layer(neuronCount);

            for (int j = 0; j < topology[i].length; j++) {
                if (topology[i][j] == null) {
                    throw new IllegalArgumentException(
                            "Aktivierungsfunktion fehlt bei Layer " + i + ", Neuron " + j
                    );
                }

                layers[i].getNeurons()[j] = new LayerNeuron(topology[i][j]);
            }
            if (hasBias) {
                layers[i].getNeurons()[neuronCount - 1] = new BiasNeuron();
            }
        }

        return new Network(layers);
    }
}