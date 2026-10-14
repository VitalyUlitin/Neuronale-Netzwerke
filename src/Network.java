public class Network {
    private String source;
    private Layers[] layers;
    private double[] expectedOutput;

    public Network(String source) {
        this.source = source;
    }

    public Network() {
        this.source = "";
    }

    public double[] forwardPass() {
        layers = Reader.readLayers;
        expectedOutput = Reader.readOutput;

        double[] output = expectedOutput.length;

        for (int i = 0; i < layers.length - 1; i++) {
            layers[i + 1].input(Matrixoperations.matrixmultiplication(
                    layers[i],layers[i + 1.].length;
            ))
        }

        Neuron[] lastLayerNeurons = layers[layers.length - 1].getNeurons();

        for (int i = 0; i < output.length; i++) {
            output[i] = lastLayerNeurons[i].getValue();
        }

        return output;
    }

    public void firstInit() {

    }
}