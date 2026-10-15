
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Reader {

    public static Layers[] readLayers(String source) {
        double[] input = readInput(source);
        ArrayList<double[][]> weights = readWeightMatrices(source);

        Layers[] layers = createLayers(input, weights, true);

        return layers;
    }

    public static Layers[] readLayersFirstInit(String source) {
        double[] input = readInput(source);
        ArrayList<double[][]> weights = readWeightMatrices(source);

        Layers[] layers = createLayers(input, weights, false);

        return layers;
    }

    public static double[] readOutput(String source) {
        ArrayList<String> lines = readLines(source);

        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i).trim();

            if (!line.isEmpty()) {
                return parseValues(line);
            }
        }

        return new double[0];
    }

    private static Layers[] createLayers(
            double[] input,
            ArrayList<double[][]> weights,
            boolean withWeights) {

        if (weights.isEmpty()) {
            return new Layers[0];
        }

        int count = weights.size() + 1;
        int[] neuronNumbers = new int[count];

        // Anzahl der Neuronen aus den Gewichtsmatrizen bestimmen
        for (int i = 0; i < weights.size(); i++) {
            double[][] matrix = weights.get(i);
            neuronNumbers[i] = matrix[0].length;
        }

        neuronNumbers[count - 1] =
                weights.getLast().length;

        Layers[] layers = new Layers[count];

        for (int i = 0; i < count; i++) {
            if (withWeights && i < weights.size()) {
                layers[i] = new Layers(
                        neuronNumbers[i],
                        weights.get(i)
                );
            } else {
                layers[i] = new Layers(neuronNumbers[i]);
            }

            // Normale Neuronen initialisieren
            for (int j = 0; j < neuronNumbers[i]; j++) {
                layers[i].getNeurons()[j] =
                        new LayerNeuron(value -> value);
            }
        }

        // Inputwerte im ersten Layer setzen
        for (int i = 0; i < input.length; i++) {
            ((LayerNeuron) layers[0].getNeurons()[i])
                    .setValue(input[i]);
        }

        // Bias-Neuronen berücksichtigen, wenn die
        // Gewichtsmatrix dafür eine zusätzliche Spalte hat
        for (int i = 0; i < weights.size(); i++) {
            int normalNeurons;

            if (i == 0) {
                normalNeurons = input.length;
            } else {
                normalNeurons = weights.get(i - 1).length;
            }

            if (neuronNumbers[i] == normalNeurons + 1) {
                layers[i].getNeurons()[neuronNumbers[i] - 1]
                        = new BiasNeuron();
            }
        }

        return layers;
    }

    private static double[] readInput(String source) {
        ArrayList<String> lines = readLines(source);

        if (lines.isEmpty()) {
            return new double[0];
        }

        return parseValues(lines.get(0).trim());
    }

    private static ArrayList<double[][]> readWeightMatrices(
            String source) {

        ArrayList<String> lines = readLines(source);
        ArrayList<double[][]> allWeights = new ArrayList<>();
        ArrayList<double[]> currentMatrix = new ArrayList<>();

        // Erste Zeile = Input, letzte nichtleere Zeile = Output
        int lastLine = lines.size() - 1;

        while (lastLine >= 0 && lines.get(lastLine).trim().isEmpty()) {
            lastLine--;
        }

        for (int i = 1; i < lastLine; i++) {
            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("#")) {
                if (!currentMatrix.isEmpty()) {
                    allWeights.add(
                            currentMatrix.toArray(new double[0][])
                    );
                    currentMatrix.clear();
                }
            } else {
                currentMatrix.add(parseValues(line));
            }
        }

        if (!currentMatrix.isEmpty()) {
            allWeights.add(
                    currentMatrix.toArray(new double[0][])
            );
        }

        return allWeights;
    }

    private static ArrayList<String> readLines(String source) {
        ArrayList<String> lines = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new File(source));

            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }

            scanner.close();
        } catch (Exception e) {
            System.out.println("Fehler beim Lesen der CSV!");
        }

        return lines;
    }

    private static double[] parseValues(String line) {
        String[] values = line.split(",");
        double[] result = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            result[i] = Double.parseDouble(values[i].trim());
        }

        return result;
    }
}
