
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {

    private final String source = new File(Network.class.getResource("/testSet.csv").toURI()).getPath();

    public ProjectTest() throws URISyntaxException {
    }

    @Test
    void testReadOutput() {
        double[] output = Reader.readOutput(source);

        assertNotNull(output);
        assertTrue(output.length > 0);
    }

    @Test
    void testReadLayers() {
        Layers[] layers = Reader.readLayers(source);

        assertNotNull(layers);
        assertTrue(layers.length > 0);
    }

    @Test
    void testReadLayersFirstInit() {
        Layers[] layers = Reader.readLayersFirstInit(source);

        assertNotNull(layers);
        assertTrue(layers.length > 0);
    }

    @Test
    void testBiasNeuron() {
        BiasNeuron bias = new BiasNeuron();

        assertEquals(1.0, bias.getValue(), 0.0001);
    }

    @Test
    void testLayerNeuron() {
        LayerNeuron neuron = new LayerNeuron(value -> value * 2);
        neuron.setValue(3);

        assertEquals(6.0, neuron.getValue(), 0.0001);
    }
    @Test
    void testFirstInit() {
        Network network = new Network(source);

    }

    @Test
    void testForwardPass() {
        Network network = new Network(source);

        double[] expected = {1};
        assertArrayEquals(expected, network.forwardPass());
    }

}