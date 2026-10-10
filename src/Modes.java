import java.util.Random;

public class Modes {
    public static void firstInit(String source) {
        Layers layers = new Layers();
        Weights weights = new Weights();

        //gets input that is also the first layer
        double[] firstLayer = Reader.readInput(source);
        layers.addLayer(firstLayer);
        /*although you could just skip this step, as proof of concept
          the input is calculated with the unit matrix to the next layer
         */
        weights.addWeights(Weights.unitMatrix(firstLayer.length));
        layers.addLayer(Matrixoperations.matrixmultiplication(
                firstLayer, weights.getWeights().getLast()
        ));

        Random rand = new Random();
        //multiplicates through the layers
        for (int i = 0; i < 6; i++) {
            /*this is a proof of concept, usually the neurons and the output should be known and
              not randomized
            */
            int nextNeurons = rand.nextInt(1,6);
            weights.addWeights(Weights.initWeight(nextNeurons, layers.getLayers().getLast().length));
            layers.addLayer(Matrixoperations.matrixmultiplication(
                    layers.getLayers().getLast(), weights.getWeights().getLast()
            ));
        }

        //also not necessary, but again proof of concept
        weights.addWeights(Weights.unitMatrix(layers.getLayers().getLast().length));
        layers.addLayer(Matrixoperations.matrixmultiplication(
                layers.getLayers().getLast(), weights.getWeights().getLast()
        ));

        IO.println(layers.output());
    }

    public static void calculate(String source) {
        Layers layers = new Layers();
        Weights weights = new Weights();

        //gets weights and the input
        weights.setWeights(Reader.getWeights(source));
        layers.addLayer(Reader.readInput(source));

        //simply calculates through the layers to the output
        for (int i = 0; i < weights.getWeights().size(); i++) {
            layers.addLayer(Matrixoperations.matrixmultiplication(
                    layers.getLayers().getLast(), weights.getWeights().get(i))
            );
        }

        IO.println(layers.output());
    }
}
