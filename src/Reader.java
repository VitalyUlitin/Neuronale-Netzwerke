import java.io.File;
import java.util.Scanner;

public class Reader {

    public static Layers[] readLayers(String source) {
        double[] input = readInput(source);
        double[][][] weights = readWeightMatrices(source);

        if (parsed.weights.isEmpty()) {
            throw new IllegalArgumentException(
                    "Keine Gewichtsmatrizen gefunden."
            );
        }

        int[] neuronNumbers = determineNeuronNumbers(parsed);

        double[] input = getInput(
                parsed.data,
                neuronNumbers
        );

        return createLayers(
                input,
                neuronNumbers,
                parsed.weights,
                parsed.functions,
                true
        );
    }


    public static Layers[] readLayersFirstInit(String source) {
        double[] input = readInput(source);
        double[][][] weights = readWeightMatrices(source);


    public static double[] readOutput(String source) {
        ParsedCSV parsed = parseCSV(source);

        int[] neuronNumbers =
                determineNeuronNumbers(parsed);

        int inputCount =
                neuronNumbers[0];

        int outputCount =
                neuronNumbers[neuronNumbers.length - 1];

        if (parsed.data.length
                != inputCount + outputCount) {

            throw new IllegalArgumentException(
                    "Input-/Output-Zeile passt nicht "
                            + "zur Netztopologie."
            );
        }

        return Arrays.copyOfRange(
                parsed.data,
                inputCount,
                inputCount + outputCount
        );
    }

    public static double[] readOutput(String source) {
        String[] lines = readLines(source);

        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();

            throw new IllegalArgumentException(
                    "Input-/Output-Zeile passt nicht "
                            + "zur Netztopologie."
            );
        }

        return Arrays.copyOfRange(
                data,
                0,
                inputCount
        );
    }


    /*
     * Bestimmt die Anzahl NORMALER Neuronen
     * pro Layer, also ohne Bias.
     */
    private static int[] determineNeuronNumbers(
            ParsedCSV parsed) {

        /*
         * Falls Gewichtsmatrizen vorhanden sind,
         * wird die Topologie daraus bestimmt.
         */
        if (!parsed.weights.isEmpty()) {

            int[] neuronNumbers =
                    new int[parsed.weights.size() + 1];

            /*
             * Erste Matrix:
             *
             * Zeilen =
             * Inputneuronen + Bias
             */
            neuronNumbers[0] =
                    parsed.weights.getFirst().length - 1;


            for (int i = 0;
                 i < parsed.weights.size();
                 i++) {

                double[][] matrix =
                        parsed.weights.get(i);

                validateMatrix(matrix);

                /*
                 * Zeilenanzahl muss zu aktuellem
                 * Layer + Bias passen.
                 */
                if (matrix.length
                        != neuronNumbers[i] + 1) {

                    throw new IllegalArgumentException(
                            "Gewichtsmatrix der Ebene "
                                    + (i + 1)
                                    + " passt nicht zur "
                                    + "Netztopologie."
                    );
                }

                /*
                 * Spalten =
                 * normale Neuronen des nächsten Layers
                 */
                neuronNumbers[i + 1] =
                        matrix[0].length;
            }

            validateFunctions(
                    parsed.functions,
                    neuronNumbers
            );

            return neuronNumbers;
        }


        /*
         * Keine Gewichte:
         * Dann kann #Functions die Topologie
         * für firstInit festlegen.
         */
        if (!parsed.functions.isEmpty()) {

            int[] neuronNumbers =
                    new int[parsed.functions.size()];

            for (int i = 0;
                 i < parsed.functions.size();
                 i++) {

                neuronNumbers[i] =
                        parsed.functions.get(i).length;
            }

            return neuronNumbers;
        }


        throw new IllegalArgumentException(
                "Netztopologie konnte nicht bestimmt werden."
        );
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

            /*
             * Alle Layer außer dem Output-Layer
             * besitzen ein BiasNeuron.
             */
            boolean hasBias =
                    i < layerCount - 1;

            int totalNeuronCount =
                    normalNeuronNumbers[i]
                            + (hasBias ? 1 : 0);


            /*
             * Gewichtsmatrix eintragen,
             * falls forwardPass() und nicht firstInit().
             */
            if (withWeights
                    && i < fileWeights.size()) {

                double[][] internalMatrix =
                        convertWeightMatrix(
                                fileWeights.get(i),
                                i + 1 < layerCount - 1
                        );

                layers[i] =
                        new Layers(
                                totalNeuronCount,
                                internalMatrix
                        );

        for (int i = 0; i < count; i++) {

            if (withWeights && i < weights.length) {
                layers[i] = new Layers(
                        neuronNumbers[i],
                        weights[i]
                );
            } else {

                layers[i] =
                        new Layers(totalNeuronCount);
            }


            /*
             * Normale Neuronen erzeugen und
             * Aktivierungsfunktion setzen.
             */
            for (int j = 0;
                 j < normalNeuronNumbers[i];
                 j++) {

                Function function;

                /*
                 * Wenn keine Funktionen angegeben wurden:
                 * Default = Identitätsfunktion.
                 */
                if (functionNames.isEmpty()) {

                    function =
                            value -> value;

                } else {

                    String functionName =
                            functionNames
                                    .get(i)[j];

                    function =
                            parseFunction(functionName);
                }


                layers[i]
                        .getNeurons()[j]
                        = new LayerNeuron(function);
            }


            /*
             * BiasNeuron ist immer das letzte
             * Neuron des Layers.
             */
            if (hasBias) {

                layers[i]
                        .getNeurons()[totalNeuronCount - 1]
                        = new BiasNeuron();
            }
        }


        /*
         * Inputwerte setzen.
         *
         * Dabei wird bereits die Aktivierungsfunktion
         * der Inputneuronen angewendet.
         */
        for (int i = 0; i < input.length; i++) {

            LayerNeuron neuron =
                    (LayerNeuron)
                            layers[0]
                                    .getNeurons()[i];

            neuron.setValue(input[i]);
        }

        // Bias-Neuronen berücksichtigen
        for (int i = 0; i < weights.length; i++) {

            int normalNeurons;

            if (i == 0) {
                normalNeurons = input.length;
            } else {
                normalNeurons = weights[i - 1].length;
            }


    /*
     * Wandelt das Vorlesungsformat
     *
     * [Quellneuron][Zielneuron]
     *
     * in das interne Format
     *
     * [Zielneuron][Quellneuron]
     *
     * um.
     */
    private static double[][] convertWeightMatrix(
            double[][] matrix,
            boolean nextLayerHasBias) {

        int sourceNeuronCount =
                matrix.length;

        int targetNeuronCount =
                matrix[0].length;

        int internalTargetCount =
                targetNeuronCount
                        + (nextLayerHasBias ? 1 : 0);


        double[][] result =
                new double[internalTargetCount]
                        [sourceNeuronCount];


        for (int source = 0;
             source < sourceNeuronCount;
             source++) {

            for (int target = 0;
                 target < targetNeuronCount;
                 target++) {

                result[target][source] =
                        matrix[source][target];
            }
        }

        /*
         * Falls der nächste Layer ein BiasNeuron hat,
         * bleibt die letzte Zeile bei 0.
         *
         * Layers.input() ignoriert diesen Wert
         * ohnehin für das BiasNeuron.
         */

        return result;
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


        for (int i = 0; i < values.length; i++) {
            result[i] =
                    Double.parseDouble(values[i].trim());
        }


        return result;
    }


    private enum Section {
        NONE,
        FUNCTIONS,
        WEIGHTS
    }


    private static class ParsedCSV {

        private final double[] data;

        private final ArrayList<String[]>
                functions;

        private final ArrayList<double[][]>
                weights;


        private ParsedCSV(
                double[] data,
                ArrayList<String[]> functions,
                ArrayList<double[][]> weights) {

            this.data = data;
            this.functions = functions;
            this.weights = weights;
        }
    }
}