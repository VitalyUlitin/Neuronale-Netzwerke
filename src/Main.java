import io.Persistence;
import io.Reader;
import network.Network;
import network.NetworkFactory;
import network.TrainingResult;
import neuron.Function;
import neuron.Functions;

import java.util.Arrays;

public class Main {

    static void main() {

        Function[][] topology = {
                {Functions.ID, Functions.ID}, {Functions.SIGMOID, Functions.SIGMOID}, {Functions.ID}
        };

        Network network = NetworkFactory.create(topology);


        Reader.DataSet data = Reader.readData(
                "/training.csv", network.getInputSize(), network.getOutputSize()
        );


        network.initRandomWeights();

        TrainingResult result = network.train(data.inputs(), data.outputs(), 100000,0.01,0.00001);

        System.out.println("Epochen: " + result.epochs());
        System.out.println("Fehler: " + result.error());
        System.out.println("Konvergiert: " + result.converged());

        Persistence.saveWeights(network.getLayers(), "savedWeights.csv");
    }
}