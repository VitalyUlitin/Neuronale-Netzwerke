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

    public void forwardPass() {
        layers = Reader.readLayers;
        expectedOutput = Reader.readOutput;
    }

    public void firstInit() {

    }
}