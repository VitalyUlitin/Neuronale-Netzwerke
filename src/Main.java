import io.Reader;
import network.Network;
import network.NetworkFactory;
import neuron.Function;
import neuron.Functions;

import java.util.Arrays;

public class Main {

    static void main() {

        Function[][] topology = {
                {Functions.ID, Functions.ID}, {Functions.RELU, Functions.RELU}, {Functions.step(3)}
        };

        Network network = NetworkFactory.create(topology);


        Reader.DataSet data = Reader.readData(
                "/training.csv", network.getInputSize(), network.getOutputSize()
        );


        double[][][] weights = Reader.readWeights("/weights.csv");

        network.setWeights(weights);


        network.setInput(data.inputs()[0]);

        double[] result = network.forwardPass();

        System.out.println(Arrays.toString(result));
    }
}