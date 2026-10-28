
import io.Reader;
import network.Network;
import network.NetworkFactory;
import network.TrainingResult;
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

    @Test
    void networkTrainingLearnsLinearFunction() {

        Function[][] topology = {
                {Functions.ID},
                {Functions.ID}
        };

        Network network = NetworkFactory.create(topology);


        //start weights are set manually for deterministic behaviour (x -> < = 0, bias -. = 0
        double[][][] weights = {{{0.0}, {0.0}}};
        network.setWeights(weights);

        //train fnction y = 2x+1
        double[][] inputs = {{0.0}, {1.0}, {2.0}};

        double[][] expectedOutputs = {{1.0}, {3.0}, {5.0}};

        TrainingResult result =
                network.train(
                        inputs,
                        expectedOutputs,
                        10_000,
                        0.1,
                        0.00000001
                );

        //checking tolerance
        assertTrue(result.converged());

        assertTrue(result.error() <= 0.00000001);


        //real forward pass test
        for (int i = 0; i < inputs.length; i++) {
            network.setInput(inputs[i]);
            double[] actual = network.forwardPass();
            assertEquals(expectedOutputs[i][0], actual[0], 0.001);
        }
    }

}