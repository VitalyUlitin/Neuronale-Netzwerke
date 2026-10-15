import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Reader {

    public static Layers[] readLayers(String source) {
        ParsedCSV parsed = parseCSV(source);

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
        ParsedCSV parsed = parseCSV(source);

        /*
         * Bei firstInit kann die Topologie entweder
         * aus den Matrizen oder aus #Functions kommen.
         */
        int[] neuronNumbers =
                determineNeuronNumbers(parsed);

        double[] input = getInput(
                parsed.data,
                neuronNumbers
        );

        return createLayers(
                input,
                neuronNumbers,
                parsed.weights,
                parsed.functions,
                false
        );
    }


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


    private static double[] getInput(
            double[] data,
            int[] neuronNumbers) {

        int inputCount = neuronNumbers[0];

        int outputCount =
                neuronNumbers[neuronNumbers.length - 1];

        if (data.length
                != inputCount + outputCount) {

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
            int[] normalNeuronNumbers,
            ArrayList<double[][]> fileWeights,
            ArrayList<String[]> functionNames,
            boolean withWeights) {

        int layerCount =
                normalNeuronNumbers.length;

        Layers[] layers =
                new Layers[layerCount];


        for (int i = 0; i < layerCount; i++) {

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


        return layers;
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


    /*
     * Aktivierungsfunktion aus einem String erzeugen.
     */
    private static Function parseFunction(
            String name) {

        String normalized =
                name.trim().toLowerCase();


        switch (normalized) {

            case "id":
            case "identity":
                return value -> value;


            case "relu":
                return value ->
                        Math.max(0.0, value);


            case "sigmoid":
            case "logistic":
                return value ->
                        1.0
                                / (1.0
                                + Math.exp(-value));


            case "tanh":
                return Math::tanh;
        }


        /*
         * Schwellwertfunktion:
         *
         * step:3
         * step:1.5
         */
        if (normalized.startsWith("step:")) {

            double theta =
                    Double.parseDouble(
                            normalized.substring(5)
                    );

            return value ->
                    value >= theta
                            ? 1.0
                            : 0.0;
        }


        /*
         * Deutsche Variante ebenfalls erlauben:
         *
         * schwellwert:3
         */
        if (normalized.startsWith("schwellwert:")) {

            double theta =
                    Double.parseDouble(
                            normalized.substring(
                                    "schwellwert:".length()
                            )
                    );

            return value ->
                    value >= theta
                            ? 1.0
                            : 0.0;
        }


        throw new IllegalArgumentException(
                "Unbekannte Aktivierungsfunktion: "
                        + name
        );
    }


    /*
     * Prüft, dass pro Layer genau so viele
     * Funktionen wie normale Neuronen
     * angegeben wurden.
     */
    private static void validateFunctions(
            ArrayList<String[]> functions,
            int[] neuronNumbers) {

        /*
         * Keine Angabe bedeutet:
         * überall Identitätsfunktion.
         */
        if (functions.isEmpty()) {
            return;
        }


        if (functions.size()
                != neuronNumbers.length) {

            throw new IllegalArgumentException(
                    "Anzahl der Funktionszeilen stimmt "
                            + "nicht mit der Anzahl der Layer überein."
            );
        }


        for (int i = 0;
             i < functions.size();
             i++) {

            if (functions.get(i).length
                    != neuronNumbers[i]) {

                throw new IllegalArgumentException(
                        "Ebene "
                                + i
                                + " benötigt "
                                + neuronNumbers[i]
                                + " Aktivierungsfunktionen, "
                                + "angegeben wurden "
                                + functions.get(i).length
                );
            }
        }
    }


    /*
     * Liest die komplette Datei.
     */
    private static ParsedCSV parseCSV(
            String source) {

        ArrayList<String> lines =
                readLines(source);

        double[] data = null;

        ArrayList<String[]> functions =
                new ArrayList<>();

        ArrayList<double[][]> weights =
                new ArrayList<>();

        ArrayList<double[]> currentMatrix =
                new ArrayList<>();


        Section section =
                Section.NONE;


        for (String rawLine : lines) {

            String line =
                    rawLine.trim();


            if (line.isEmpty()) {
                continue;
            }


            /*
             * Neuer Hauptblock beginnt.
             */
            if (line.equalsIgnoreCase("#Functions")) {

                /*
                 * Falls vorher ein Gewichtsblock
                 * offen war, abschließen.
                 */
                if (section == Section.WEIGHTS
                        && !currentMatrix.isEmpty()) {

                    weights.add(
                            toMatrix(currentMatrix)
                    );

                    currentMatrix.clear();
                }

                section =
                        Section.FUNCTIONS;

                continue;
            }


            if (line.equalsIgnoreCase("#Weights")) {

                if (section == Section.WEIGHTS
                        && !currentMatrix.isEmpty()) {

                    weights.add(
                            toMatrix(currentMatrix)
                    );

                    currentMatrix.clear();
                }

                section =
                        Section.WEIGHTS;

                continue;
            }


            /*
             * Noch kein Block:
             * erste Zahlenzeile ist Input + Output.
             */
            if (section == Section.NONE) {

                if (!line.startsWith("#")
                        && data == null) {

                    data =
                            parseValues(line);
                }

                continue;
            }


            /*
             * Funktionsblock
             */
            if (section
                    == Section.FUNCTIONS) {

                /*
                 * "# ebene ..." nur als Kommentar
                 * verwenden.
                 */
                if (line.startsWith("#")) {
                    continue;
                }

                functions.add(
                        parseStrings(line)
                );

                continue;
            }


            /*
             * Gewichtsblock
             */
            if (section
                    == Section.WEIGHTS) {

                /*
                 * NUR "# ebene ..."
                 * startet eine neue Matrix.
                 */
                if (isLayerHeader(line)) {

                    if (!currentMatrix.isEmpty()) {

                        weights.add(
                                toMatrix(
                                        currentMatrix
                                )
                        );

                        currentMatrix.clear();
                    }

                    continue;
                }


                /*
                 * # Bias Ebene ...
                 * oder andere Kommentare
                 */
                if (line.startsWith("#")) {
                    continue;
                }


                currentMatrix.add(
                        parseValues(line)
                );
            }
        }


        /*
         * Letzte Matrix hinzufügen.
         */
        if (!currentMatrix.isEmpty()) {

            weights.add(
                    toMatrix(currentMatrix)
            );
        }


        if (data == null) {

            throw new IllegalArgumentException(
                    "Keine Input-/Output-Zeile gefunden."
            );
        }


        return new ParsedCSV(
                data,
                functions,
                weights
        );
    }


    private static boolean isLayerHeader(
            String line) {

        String normalized =
                line.toLowerCase()
                        .replaceAll("\\s+", "");

        return normalized.startsWith(
                "#ebene"
        );
    }


    private static String[] parseStrings(
            String line) {

        String[] values =
                line.split(",");

        for (int i = 0;
             i < values.length;
             i++) {

            values[i] =
                    values[i].trim();
        }

        return values;
    }


    private static double[][] toMatrix(
            ArrayList<double[]> rows) {

        double[][] matrix =
                rows.toArray(
                        new double[0][]
                );

        validateMatrix(matrix);

        return matrix;
    }


    private static void validateMatrix(
            double[][] matrix) {

        if (matrix.length == 0) {

            throw new IllegalArgumentException(
                    "Leere Gewichtsmatrix."
            );
        }


        int columnCount =
                matrix[0].length;


        if (columnCount == 0) {

            throw new IllegalArgumentException(
                    "Gewichtsmatrix ohne Spalten."
            );
        }


        for (double[] row : matrix) {

            if (row.length
                    != columnCount) {

                throw new IllegalArgumentException(
                        "Gewichtsmatrix ist "
                                + "nicht rechteckig."
                );
            }
        }
    }


    private static ArrayList<String> readLines(
            String source) {

        ArrayList<String> lines =
                new ArrayList<>();


        try (Scanner scanner =
                     new Scanner(
                             new File(source)
                     )) {

            while (scanner.hasNextLine()) {

                lines.add(
                        scanner.nextLine()
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Fehler beim Lesen der CSV-Datei.",
                    e
            );
        }


        return lines;
    }


    private static double[] parseValues(
            String line) {

        String[] values =
                line.split(",");

        double[] result =
                new double[values.length];


        for (int i = 0;
             i < values.length;
             i++) {

            result[i] =
                    Double.parseDouble(
                            values[i].trim()
                    );
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