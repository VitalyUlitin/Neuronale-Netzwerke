package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Reader {
    //contains training sets e.g. (1;2;       3)
    public static class DataSet {
        private final double[][] inputs;
        private final double[][] outputs;

        public DataSet(double[][] inputs, double[][] outputs) {
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

    //reads the training sets and
    public static DataSet readData(String source, int inputCount, int outputCount) {
        int rowCount = countDataRows(source);

        double[][] inputs = new double[rowCount][inputCount];
        double[][] outputs = new double[rowCount][outputCount];

        try (BufferedReader reader = openReader(source)){
            String line;
            int row = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                double[] values = parseNumbers(line);

                if (values.length != inputCount + outputCount) {
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
                for (int i = 0; i < inputCount; i++) {
                    inputs[row][i] = values[i];
                }
                // Output
                for (int i = 0; i < outputCount; i++) {
                    outputs[row][i] = values[inputCount + i];
                }
                row++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der Daten.", e);
        }

        return new DataSet(inputs, outputs);
    }
     /* reads weights
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
     *for matrixMultiplication this translates internally to:
     * weights[layer][sourceNeuron][targetNeuron]
     */
    public static double[][][] readWeights(String source) {
        int layerCount = countWeightLayers(source);

        if (layerCount == 0) {
            throw new IllegalArgumentException("Keine Gewichtsebenen gefunden.");
        }

        int[] rows = new int[layerCount];
        int[] columns = new int[layerCount];

        //first iteration: reads dimensions of the double[][][]
        try (BufferedReader reader = openReader(source)){
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

                //ignore #Weights,#Bias Ebene and other # controls
                if (line.startsWith("#")) {
                    continue;
                }

                if (layer < 0) {
                    continue;
                }

                double[] values = parseNumbers(line);

                if (columns[layer] == 0) {
                    columns[layer] = values.length;
                } else if (columns[layer] != values.length) {
                    reader.close();
                    throw new IllegalArgumentException(
                            "Gewichtsmatrix Ebene " + (layer + 1) + " ist nicht rechteckig.");
                }
                rows[layer]++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der Gewichte.", e);
        }
        //creates weightmatrix
        double[][][] weights = new double[layerCount][][];

        for (int i = 0; i < layerCount; i++) {
            weights[i] = new double[rows[i]][columns[i]];
        }
        //second iteration: fills the double[][][]
        try (BufferedReader reader = openReader(source)){
            String line;
            int layer = -1;

            int[] currentRow = new int[layerCount];

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }
                if (isLayerHeader(line)) {
                    layer++;
                    continue;
                }
                if (line.startsWith("#") || layer < 0) {
                    continue;
                }

                double[] values = parseNumbers(line);

                for (int column = 0; column < values.length; column++) {
                    weights[layer][currentRow[layer]][column] = values[column];
                }

                currentRow[layer]++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der Gewichte.", e);
        }

        return weights;
    }

    //counts rows of the trainingset
    private static int countDataRows(String source) {
        int count = 0;
        try (BufferedReader reader = openReader(source)){
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty() && !line.startsWith("#")) {
                    count++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der Daten.", e);
        }
        return count;
    }

    //counts #-ccontrol blocks
    private static int countWeightLayers(String source) {
        int count = 0;

        try (BufferedReader reader = openReader(source)){
            String line;

            while ((line = reader.readLine()) != null) {
                if (isLayerHeader(line)) {
                    count++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der Gewichte.", e);
        }

        return count;
    }

    //recognizes # eben control blocks
    private static boolean isLayerHeader(String line) {
        String normalized = line.trim().toLowerCase().replace(" ", "");

        return normalized.startsWith("#ebene");
    }

    //parses a line to double[] allowed formats: 1;2;3 or 1,2,3 or 1;2;3; (doubles represented as 2.3)
    private static double[] parseNumbers(String line) {
        String normalized = line.replace(',', ';');
        String[] raw = normalized.split(";");

        int count = 0;
        for (String string : raw) {
            if (!string.trim().isEmpty()) {
                count++;
            }
        }

        double[] values = new double[count];

        int index = 0;

        for (String s : raw) {
            String value = s.trim();

            if (!value.isEmpty()) {
                values[index] = Double.parseDouble(value);
                index++;
            }
        }

        return values;
    }

    //comfort method: tries ressource/ as first path, then attempts usual file path
    private static BufferedReader openReader(String source) throws IOException {
        String resourceName = source;

        if (!resourceName.startsWith("/")) {
            resourceName = "/" + resourceName;
        }

        InputStream input = Reader.class.getResourceAsStream(resourceName);

        if (input != null) {
            return new BufferedReader(new InputStreamReader(input));
        }

        return new BufferedReader(new FileReader(source));
    }
}