package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Reader {


    /*
     * Enthält alle Trainingsdatensätze.
     */
    public static class DataSet {

        private final double[][] inputs;
        private final double[][] outputs;

        public DataSet(
                double[][] inputs,
                double[][] outputs) {

            this.inputs = inputs;
            this.outputs = outputs;
        }

        public double[][] inputs() {
            return inputs;
        }

        public double[][] outputs() {
            return outputs;
        }
    }


    /*
     * Liest z.B.
     *
     * 1;2;3;       5;3
     * 0;3.2;0.98;  2;1
     *
     * Bei inputCount = 3
     * und outputCount = 2.
     */
    public static DataSet readData(
            String source,
            int inputCount,
            int outputCount) {

        int rowCount = countDataRows(source);

        double[][] inputs =
                new double[rowCount][inputCount];

        double[][] outputs =
                new double[rowCount][outputCount];

        try {
            BufferedReader reader =
                    openReader(source);

            String line;
            int row = 0;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()
                        || line.startsWith("#")) {
                    continue;
                }

                double[] values =
                        parseNumbers(line);

                if (values.length
                        != inputCount + outputCount) {

                    reader.close();

                    throw new IllegalArgumentException(
                            "Datensatz " + row
                                    + " enthält "
                                    + values.length
                                    + " Werte. Erwartet werden "
                                    + inputCount
                                    + " Inputwerte und "
                                    + outputCount
                                    + " Outputwerte."
                    );
                }


                // Input
                for (int i = 0;
                     i < inputCount;
                     i++) {

                    inputs[row][i] =
                            values[i];
                }


                // Output
                for (int i = 0;
                     i < outputCount;
                     i++) {

                    outputs[row][i] =
                            values[inputCount + i];
                }

                row++;
            }

            reader.close();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Fehler beim Lesen der Daten.",
                    e
            );
        }

        return new DataSet(
                inputs,
                outputs
        );
    }


    /*
     * Liest die Gewichtsmatrizen.
     *
     * Format:
     *
     * #Weights
     * # ebene 1
     * 1;2
     * 1;-1
     * # Bias Ebene 1
     * 0;0
     *
     * # ebene 2
     * 1
     * 1
     * # Bias Ebene 2
     * 0
     *
     *
     * Ergebnis:
     *
     * weights[ebene][quellNeuron][zielNeuron]
     */
    public static double[][][] readWeights(
            String source) {

        int layerCount =
                countWeightLayers(source);

        if (layerCount == 0) {
            throw new IllegalArgumentException(
                    "Keine Gewichtsebenen gefunden."
            );
        }


        int[] rows =
                new int[layerCount];

        int[] columns =
                new int[layerCount];


        /*
         * Erster Durchlauf:
         * Größe jeder Matrix bestimmen.
         */
        try {
            BufferedReader reader =
                    openReader(source);

            String line;
            int layer = -1;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }


                if (isLayerHeader(line)) {
                    layer++;
                    continue;
                }


                /*
                 * #Weights
                 * # Bias Ebene ...
                 * usw. ignorieren.
                 */
                if (line.startsWith("#")) {
                    continue;
                }


                if (layer < 0) {
                    continue;
                }


                double[] values =
                        parseNumbers(line);


                if (columns[layer] == 0) {

                    columns[layer] =
                            values.length;

                } else if (columns[layer]
                        != values.length) {

                    reader.close();

                    throw new IllegalArgumentException(
                            "Gewichtsmatrix Ebene "
                                    + (layer + 1)
                                    + " ist nicht rechteckig."
                    );
                }

                rows[layer]++;
            }

            reader.close();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Fehler beim Lesen der Gewichte.",
                    e
            );
        }


        /*
         * Matrizen anlegen.
         */
        double[][][] weights =
                new double[layerCount][][];

        for (int i = 0;
             i < layerCount;
             i++) {

            weights[i] =
                    new double[rows[i]][columns[i]];
        }


        /*
         * Zweiter Durchlauf:
         * Gewichtswerte einlesen.
         */
        try {
            BufferedReader reader =
                    openReader(source);

            String line;

            int layer = -1;

            int[] currentRow =
                    new int[layerCount];


            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }


                if (isLayerHeader(line)) {
                    layer++;
                    continue;
                }


                if (line.startsWith("#")) {
                    continue;
                }


                if (layer < 0) {
                    continue;
                }


                double[] values =
                        parseNumbers(line);


                for (int column = 0;
                     column < values.length;
                     column++) {

                    weights[layer]
                            [currentRow[layer]]
                            [column]
                            =
                            values[column];
                }

                currentRow[layer]++;
            }

            reader.close();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Fehler beim Lesen der Gewichte.",
                    e
            );
        }


        return weights;
    }


    /*
     * Zählt die Zeilen des Trainingsdatensatzes.
     */
    private static int countDataRows(
            String source) {

        int count = 0;

        try {
            BufferedReader reader =
                    openReader(source);

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (!line.isEmpty()
                        && !line.startsWith("#")) {

                    count++;
                }
            }

            reader.close();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Fehler beim Lesen der Daten.",
                    e
            );
        }

        return count;
    }


    /*
     * Zählt "# ebene ..."-Blöcke.
     */
    private static int countWeightLayers(
            String source) {

        int count = 0;

        try {
            BufferedReader reader =
                    openReader(source);

            String line;

            while ((line = reader.readLine()) != null) {

                if (isLayerHeader(line)) {
                    count++;
                }
            }

            reader.close();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Fehler beim Lesen der Gewichte.",
                    e
            );
        }

        return count;
    }


    /*
     * Erkennt:
     *
     * # ebene 1
     * #ebene 1
     * # Ebene 1
     *
     * aber NICHT:
     *
     * # Bias Ebene 1
     */
    private static boolean isLayerHeader(
            String line) {

        String normalized =
                line.trim()
                        .toLowerCase()
                        .replace(" ", "");

        return normalized.startsWith(
                "#ebene"
        );
    }


    /*
     * Erlaubt sowohl
     *
     * 1;2;3
     *
     * als auch
     *
     * 1,2,3
     *
     * für die Gewichtsdatei.
     *
     * Für Kommazahlen wird natürlich weiterhin
     * der Punkt verwendet:
     *
     * 3.2
     */
    private static double[] parseNumbers(
            String line) {

        String normalized =
                line.replace(',', ';');

        String[] raw =
                normalized.split(";");

        int count = 0;


        /*
         * Leere Felder ignorieren.
         *
         * Dadurch ist auch
         *
         * 1;2;3;
         *
         * problemlos möglich.
         */
        for (int i = 0;
             i < raw.length;
             i++) {

            if (!raw[i].trim().isEmpty()) {
                count++;
            }
        }


        double[] values =
                new double[count];

        int index = 0;


        for (int i = 0;
             i < raw.length;
             i++) {

            String value =
                    raw[i].trim();

            if (!value.isEmpty()) {

                values[index] =
                        Double.parseDouble(value);

                index++;
            }
        }


        return values;
    }


    /*
     * Versucht zuerst eine Resource zu öffnen.
     *
     * Beispiel:
     *
     * io.Reader.readData(
     *     "/training.csv",
     *     3,
     *     2
     * );
     *
     * Falls keine Resource existiert,
     * wird source als normaler Dateipfad behandelt.
     */
    private static BufferedReader openReader(
            String source) throws IOException {

        String resourceName = source;

        if (!resourceName.startsWith("/")) {
            resourceName =
                    "/" + resourceName;
        }


        InputStream input =
                Reader.class.getResourceAsStream(
                        resourceName
                );


        if (input != null) {

            return new BufferedReader(
                    new InputStreamReader(input)
            );
        }


        return new BufferedReader(
                new FileReader(source)
        );
    }
}