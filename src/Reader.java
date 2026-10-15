import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Reader {

    /*
     * Liest das vollständige Netz inklusive vorhandener Gewichte.
     */
    public static Layers[] readLayers(String source) {
        ParsedCSV parsed = parseCSV(source);

        return createLayers(
                parsed.input,
                parsed.weightMatrices,
                true
        );
    }


    /*
     * Erstinitialisierung:
     * Die Topologie wird aus den Dimensionen der Matrizen gelesen,
     * die vorhandenen Gewichtswerte werden aber NICHT übernommen.
     *
     * Network.firstInit() erzeugt danach selbst zufällige Gewichte.
     */
    public static Layers[] readLayersFirstInit(String source) {
        ParsedCSV parsed = parseCSV(source);

        return createLayers(
                parsed.input,
                parsed.weightMatrices,
                false
        );
    }


    /*
     * Gibt den erwarteten Output aus der ersten Datenzeile zurück.
     */
    public static double[] readOutput(String source) {
        ParsedCSV parsed = parseCSV(source);

        return parsed.expectedOutput.clone();
    }


    /*
     * Erzeugt die Layer passend zur eingelesenen Topologie.
     */
    private static Layers[] createLayers(
            double[] input,
            ArrayList<double[][]> fileWeights,
            boolean withWeights) {

        if (fileWeights.isEmpty()) {
            throw new IllegalArgumentException(
                    "Keine Gewichtsmatrizen vorhanden."
            );
        }

        int layerCount = fileWeights.size() + 1;

        /*
         * Anzahl NORMALER Neuronen pro Layer,
         * also ohne BiasNeuron.
         */
        int[] normalNeuronCount =
                new int[layerCount];

        /*
         * Die erste Matrix aus der CSV hat:
         *
         * Zeilen = Inputneuronen + Bias
         * Spalten = Neuronen des nächsten Layers
         */
        normalNeuronCount[0] =
                fileWeights.get(0).length - 1;

        /*
         * Aus jeder Matrix ergibt sich die Anzahl
         * der normalen Neuronen des nächsten Layers.
         */
        for (int i = 0; i < fileWeights.size(); i++) {

            double[][] matrix = fileWeights.get(i);

            validateMatrix(matrix);

            int expectedSourceNeurons =
                    normalNeuronCount[i] + 1;

            /*
             * Jeder Layer vor dem Output besitzt bei
             * unserem Modell ein BiasNeuron.
             */
            if (matrix.length != expectedSourceNeurons) {
                throw new IllegalArgumentException(
                        "Ebene " + (i + 1)
                                + " besitzt "
                                + matrix.length
                                + " Eingangszeilen, erwartet wurden "
                                + expectedSourceNeurons
                                + " (inklusive Bias)."
                );
            }

            normalNeuronCount[i + 1] =
                    matrix[0].length;
        }


        /*
         * Gewichtsmatrizen aus dem Dateiformat
         * in das interne Format umwandeln.
         */
        ArrayList<double[][]> internalWeights =
                new ArrayList<>();

        for (int i = 0; i < fileWeights.size(); i++) {

            boolean nextLayerHasBias =
                    i + 1 < layerCount - 1;

            internalWeights.add(
                    convertWeightMatrix(
                            fileWeights.get(i),
                            nextLayerHasBias
                    )
            );
        }


        Layers[] layers =
                new Layers[layerCount];


        /*
         * Layer und Neuronen erzeugen.
         */
        for (int i = 0; i < layerCount; i++) {

            boolean hasBias =
                    i < layerCount - 1;

            int totalNeuronCount =
                    normalNeuronCount[i]
                            + (hasBias ? 1 : 0);


            /*
             * Beim normalen Forward-Pass bekommt jeder
             * Layer seine Gewichtsmatrix.
             *
             * Der Output-Layer besitzt keine Matrix mehr,
             * da kein weiterer Layer folgt.
             */
            if (withWeights && i < internalWeights.size()) {

                layers[i] = new Layers(
                        totalNeuronCount,
                        internalWeights.get(i)
                );

            } else {

                layers[i] =
                        new Layers(totalNeuronCount);
            }


            /*
             * Normale Neuronen:
             * zunächst überall Identitätsfunktion.
             */
            for (int j = 0;
                 j < normalNeuronCount[i];
                 j++) {

                layers[i].getNeurons()[j] =
                        new LayerNeuron(value -> value);
            }


            /*
             * Jeder Layer außer dem Output-Layer
             * bekommt als letztes Neuron ein BiasNeuron.
             */
            if (hasBias) {

                layers[i]
                        .getNeurons()[totalNeuronCount - 1]
                        = new BiasNeuron();
            }
        }


        /*
         * Inputwerte in den ersten Layer schreiben.
         */
        if (input.length != normalNeuronCount[0]) {
            throw new IllegalArgumentException(
                    "Anzahl Inputwerte stimmt nicht "
                            + "mit dem Input-Layer überein."
            );
        }

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
     * Wandelt eine Matrix aus dem Vorlesungsformat
     * in das interne Format um.
     *
     * Vorlesungsformat:
     *
     *      Ziel 1   Ziel 2
     * x1     ...
     * x2     ...
     * Bias   ...
     *
     * also:
     *
     * [Quellneuronen inklusive Bias]
     * [Zielneuronen]
     *
     *
     * Intern benötigt Matrixoperations:
     *
     * [Zielneuronen]
     * [Quellneuronen inklusive Bias]
     *
     *
     * Wenn der Ziel-Layer selbst ein BiasNeuron besitzt,
     * wird zusätzlich eine Nullzeile angelegt.
     *
     * Diese Zeile wird zwar berechnet, ihr Ergebnis wird
     * von Layers.input() beim BiasNeuron ignoriert.
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


        double[][] converted =
                new double[internalTargetCount]
                        [sourceNeuronCount];


        /*
         * Transponieren
         */
        for (int source = 0;
             source < sourceNeuronCount;
             source++) {

            for (int target = 0;
                 target < targetNeuronCount;
                 target++) {

                converted[target][source] =
                        matrix[source][target];
            }
        }

        /*
         * Die ggf. zusätzliche letzte Zeile bleibt
         * automatisch komplett 0.0.
         *
         * Sie entspricht dem BiasNeuron des Ziel-Layers.
         */

        return converted;
    }


    /*
     * Liest die komplette CSV und trennt:
     *
     * - Input
     * - erwarteten Output
     * - Gewichtsmatrizen
     */
    private static ParsedCSV parseCSV(String source) {

        ArrayList<String> lines =
                readLines(source);

        String dataLine = null;

        boolean weightsStarted = false;

        ArrayList<double[][]> matrices =
                new ArrayList<>();

        ArrayList<double[]> currentMatrix =
                new ArrayList<>();


        for (String rawLine : lines) {

            String line =
                    rawLine.trim();


            if (line.isEmpty()) {
                continue;
            }


            /*
             * Vor #Weights suchen wir die erste
             * tatsächliche Datenzeile.
             */
            if (!weightsStarted) {

                if (line.equalsIgnoreCase("#Weights")) {

                    weightsStarted = true;
                    continue;
                }


                if (!line.startsWith("#")
                        && dataLine == null) {

                    dataLine = line;
                }

                continue;
            }


            /*
             * Neue Ebene.
             *
             * NUR "# ebene ..." schließt eine Matrix ab.
             *
             * "# Bias Ebene ..." tut das NICHT.
             */
            if (isLayerHeader(line)) {

                if (!currentMatrix.isEmpty()) {

                    matrices.add(
                            toMatrix(currentMatrix)
                    );

                    currentMatrix.clear();
                }

                continue;
            }


            /*
             * Andere Kommentarzeilen ignorieren.
             *
             * Insbesondere:
             *
             * # Bias Ebene 1
             */
            if (line.startsWith("#")) {
                continue;
            }


            /*
             * Normale Zahlenzeile innerhalb
             * einer Gewichtsmatrix.
             */
            currentMatrix.add(
                    parseValues(line)
            );
        }


        /*
         * Letzte Matrix abschließen.
         */
        if (!currentMatrix.isEmpty()) {

            matrices.add(
                    toMatrix(currentMatrix)
            );
        }


        if (dataLine == null) {
            throw new IllegalArgumentException(
                    "Keine Input-/Output-Zeile gefunden."
            );
        }


        if (matrices.isEmpty()) {
            throw new IllegalArgumentException(
                    "Keine Gewichtsmatrizen gefunden."
            );
        }


        /*
         * Topologie validieren.
         */
        for (double[][] matrix : matrices) {
            validateMatrix(matrix);
        }

        for (int i = 1; i < matrices.size(); i++) {

            double[][] previous =
                    matrices.get(i - 1);

            double[][] current =
                    matrices.get(i);

            /*
             * Anzahl Zielneuronen der vorherigen Ebene
             * + Bias
             *
             * muss der Zeilenanzahl der nächsten
             * Gewichtsmatrix entsprechen.
             */
            int expectedRows =
                    previous[0].length + 1;

            if (current.length != expectedRows) {

                throw new IllegalArgumentException(
                        "Gewichtsmatrizen zwischen Ebene "
                                + i
                                + " und Ebene "
                                + (i + 1)
                                + " sind nicht kompatibel."
                );
            }
        }


        /*
         * Anzahl Inputs:
         *
         * Zeilen der ersten Gewichtsmatrix
         * minus Bias-Zeile.
         */
        int inputCount =
                matrices.get(0).length - 1;


        /*
         * Anzahl Outputs:
         *
         * Spalten der letzten Gewichtsmatrix.
         */
        double[][] lastMatrix =
                matrices.get(
                        matrices.size() - 1
                );

        int outputCount =
                lastMatrix[0].length;


        double[] data =
                parseValues(dataLine);


        if (data.length
                != inputCount + outputCount) {

            throw new IllegalArgumentException(
                    "Input-/Output-Zeile enthält "
                            + data.length
                            + " Werte, erwartet werden "
                            + inputCount
                            + " Inputwerte und "
                            + outputCount
                            + " Outputwerte."
            );
        }


        double[] input =
                Arrays.copyOfRange(
                        data,
                        0,
                        inputCount
                );


        double[] output =
                Arrays.copyOfRange(
                        data,
                        inputCount,
                        inputCount + outputCount
                );


        return new ParsedCSV(
                input,
                output,
                matrices
        );
    }


    /*
     * Erkennt:
     *
     * # ebene 1
     * #ebene 1
     * # Ebene 2
     *
     * NICHT jedoch:
     *
     * # Bias Ebene 1
     */
    private static boolean isLayerHeader(
            String line) {

        String normalized =
                line.toLowerCase()
                        .replaceAll("\\s+", "");

        return normalized.startsWith("#ebene");
    }


    private static double[][] toMatrix(
            ArrayList<double[]> rows) {

        double[][] matrix =
                new double[rows.size()][];

        for (int i = 0; i < rows.size(); i++) {
            matrix[i] = rows.get(i);
        }

        validateMatrix(matrix);

        return matrix;
    }


    /*
     * Prüft, ob alle Zeilen dieselbe Länge besitzen.
     */
    private static void validateMatrix(
            double[][] matrix) {

        if (matrix.length == 0) {
            throw new IllegalArgumentException(
                    "Leere Gewichtsmatrix."
            );
        }

        if (matrix[0].length == 0) {
            throw new IllegalArgumentException(
                    "Gewichtsmatrix ohne Spalten."
            );
        }


        int columns =
                matrix[0].length;

        for (int i = 1;
             i < matrix.length;
             i++) {

            if (matrix[i].length != columns) {

                throw new IllegalArgumentException(
                        "Gewichtsmatrix ist nicht rechteckig."
                );
            }
        }
    }


    private static ArrayList<String> readLines(
            String source) {

        ArrayList<String> lines =
                new ArrayList<>();


        try (Scanner scanner =
                     new Scanner(new File(source))) {

            while (scanner.hasNextLine()) {
                lines.add(
                        scanner.nextLine()
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Fehler beim Lesen der CSV-Datei: "
                            + source,
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


    /*
     * Interne Hilfsklasse für die bereits
     * zerlegte CSV.
     */
    private static class ParsedCSV {

        private final double[] input;
        private final double[] expectedOutput;
        private final ArrayList<double[][]>
                weightMatrices;


        private ParsedCSV(
                double[] input,
                double[] expectedOutput,
                ArrayList<double[][]> weightMatrices) {

            this.input = input;
            this.expectedOutput =
                    expectedOutput;

            this.weightMatrices =
                    weightMatrices;
        }
    }
}