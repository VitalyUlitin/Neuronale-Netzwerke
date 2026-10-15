public class Network {
    private final String source;
    private Layers[] layers;
    private double[] expectedOutput;

    public Network(String source) {
        this.source = source;
    }

    public Network() {
        this.source = "";
    }

    public double[] forwardPass() {
        //gets the networktopology and initiates the network with their respective neuron count & weightMatrixes
        layers = Reader.readLayers(source);
        expectedOutput = Reader.readOutput(source);



        //iterates through the network with the matrixmultiplications
        for (int i = 0; i < layers.length - 1; i++) {
            layers[i + 1].input(Matrixoperations.matrixmultiplication(
                    layers[i],layers[i + 1].getNeurons().length
            ));
        }

        return getOutput();
    }

    public double[] firstInit() {
        layers = Reader.readLayersFirstInit(source);
        expectedOutput = Reader.readOutput(source);

        double[] output = new double[expectedOutput.length];

        for (int i = 0; i < layers.length - 1; i++) {
            layers[i].generateRandomWeightMatrix(layers[i + 1].getNeurons().length);
        }

        for (int i = 0; i < layers.length - 1; i++) {
            layers[i + 1].input(Matrixoperations.matrixmultiplication(
                    layers[i],layers[i + 1].getNeurons().length
            ));
        }

        return output;
    }

    private double[] getOutput() {
        double[] output = new double[expectedOutput.length];

        Neuron[] lastLayerNeurons = layers[layers.length - 1].getNeurons();

        for (int i = 0; i < output.length; i++) {
            output[i] = lastLayerNeurons[i].getValue();
        }

        return output;
    }
}