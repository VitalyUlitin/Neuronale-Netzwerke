
import io.Reader;
import network.Network;
import network.NetworkFactory;
import neuron.BiasNeuron;
import neuron.Function;
import neuron.Functions;
import neuron.LayerNeuron;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {

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
    void testForwardPass() {
        Function[][] topology = {{Functions.ID, Functions.ID}, {Functions.RELU, Functions.RELU}, {Functions.ID}};

        Network network = NetworkFactory.create(topology);

        Reader.DataSet data = Reader.readData("/training.csv", network.getInputSize(), network.getOutputSize());

        network.setWeights(Reader.readWeights("/weights.csv"));

        for (int i = 0; i < data.inputs().length; i++) {
            network.setInput(data.inputs()[i]);


            double[] actual = network.forwardPass();

            assertArrayEquals(data.outputs()[i], actual);
        }

    }

}