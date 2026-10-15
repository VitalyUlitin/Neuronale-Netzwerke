import java.io.File;
import java.util.Scanner;

public class Reader {

    public static Layers[] readLayers(String source) {
        double[] input = readInput(source);
        double[][][] weights = readWeightMatrices(source);

        Layers[] layers = createLayers(input, weights, true);

        return layers;
    }

    public static Layers[] readLayersFirstInit(String source) {
        double[] input = readInput(source);
        double[][][] weights = readWeightMatrices(source);

        Layers[] layers = createLayers(input, weights, false);

        return layers;
    }

    public static double[] readOutput(String source) {
        String[] lines = readLines(source);

        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();

            if (!line.isEmpty()) {
                return parseValues(line);
            }
        }

        return new double[0];
    }

    private static Layers[] createLayers(
            double[] input,
            double[][][] weights,
            boolean withWeights) {

        if (weights.length == 0) {
            return new Layers[0];
        }

        int count = weights.length + 1;
        int[] neuronNumbers = new int[count];

        // Anzahl der Neuronen aus den Gewichtsmatrizen bestimmen
        for (int i = 0; i < weights.length; i++) {
            double[][] matrix = weights[i];

            // Anzahl der Spalten
            neuronNumbers[i] = matrix[0].length;
        }

        // Letzte Matrix bestimmt die Anzahl
        // der Neuronen im letzten Layer
        neuronNumbers[count - 1] =
                weights[weights.length - 1].length;

        Layers[] layers = new Layers[count];

        for (int i = 0; i < count; i++) {

            if (withWeights && i < weights.length) {
                layers[i] = new Layers(
                        neuronNumbers[i],
                        weights[i]
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

        // Bias-Neuronen berücksichtigen
        for (int i = 0; i < weights.length; i++) {

            int normalNeurons;

            if (i == 0) {
                normalNeurons = input.length;
            } else {
                normalNeurons = weights[i - 1].length;
            }

            if (neuronNumbers[i] == normalNeurons + 1) {
                layers[i].getNeurons()[neuronNumbers[i] - 1]
                        = new BiasNeuron();
            }
        }

        return layers;
    }

    private static double[] readInput(String source) {
        String[] lines = readLines(source);

        if (lines.length == 0) {
            return new double[0];
        }

        return parseValues(lines[0].trim());
    }

    private static double[][][] readWeightMatrices(
            String source) {

        String[] lines = readLines(source);

        // Zählen, wie viele Gewichtsmatrizen vorhanden sind
        int matrixCount = 0;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith("#")) {
                matrixCount++;
            }
        }

        // Falls die erste Matrix keinen #-Marker hat
        if (matrixCount == 0) {
            matrixCount = 1;
        }

        double[][][] allWeights =
                new double[matrixCount][][];

        int currentMatrix = 0;

        // Temporäres normales Array
        double[][] currentRows = new double[0][];

        // Letzte nichtleere Zeile = Output
        int lastLine = lines.length - 1;

        while (lastLine >= 0 &&
                lines[lastLine].trim().isEmpty()) {
            lastLine--;
        }

        for (int i = 1; i < lastLine; i++) {

            String line = lines[i].trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("#")) {

                // Aktuelle Matrix speichern
                if (currentRows.length > 0) {
                    allWeights[currentMatrix] = currentRows;
                    currentMatrix++;

                    currentRows = new double[0][];
                }

            } else {

                double[] row = parseValues(line);

                // Neues Array mit einer zusätzlichen Zeile
                double[][] newRows =
                        new double[currentRows.length + 1][];

                // Alte Zeilen kopieren
                for (int j = 0; j < currentRows.length; j++) {
                    newRows[j] = currentRows[j];
                }

                // Neue Zeile hinzufügen
                newRows[currentRows.length] = row;

                currentRows = newRows;
            }
        }

        // Letzte Matrix speichern
        if (currentRows.length > 0) {
            allWeights[currentMatrix] = currentRows;
        }

        // Falls weniger Matrizen gefunden wurden,
        // Array entsprechend verkleinern
        int actualCount = 0;

        for (int i = 0; i < allWeights.length; i++) {
            if (allWeights[i] != null) {
                actualCount++;
            }
        }

        double[][][] result =
                new double[actualCount][][];

        for (int i = 0; i < actualCount; i++) {
            result[i] = allWeights[i];
        }

        return result;
    }

    private static String[] readLines(String source) {
        try {
            Scanner scanner = new Scanner(new File(source));

            // Erst zählen
            int count = 0;

            while (scanner.hasNextLine()) {
                scanner.nextLine();
                count++;
            }

            scanner.close();

            // Scanner nochmal öffnen
            scanner = new Scanner(new File(source));

            String[] lines = new String[count];

            int i = 0;

            while (scanner.hasNextLine()) {
                lines[i] = scanner.nextLine();
                i++;
            }

            scanner.close();

            return lines;

        } catch (Exception e) {
            System.out.println("Fehler beim Lesen der CSV!");
            return new String[0];
        }
    }

    private static double[] parseValues(String line) {
        String[] values = line.split(",");
        double[] result = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            result[i] =
                    Double.parseDouble(values[i].trim());
        }

        return result;
    }
}
