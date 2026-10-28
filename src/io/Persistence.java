package io;

import network.Layer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Persistence {
    public static void saveWeights(Layer[] layers, String path) {
        String output = "#Weights\n\n";

        for (int i = 0; i < layers.length - 1; i++) {
            String layerToString = "# ebene " + (i + 1) + "\n";
            String biasToString = "# Bias Ebene " + (i + 1) + "\n";
            double[][] weightMatrix = layers[i].getWeightMatrix();

            //iterate through the weights except last row (bias ebene)
            for (int j = 0; j < weightMatrix.length - 1; j++) {
                for (int k = 0; k < weightMatrix[j].length; k++) {
                    layerToString += weightMatrix[j][k] + ";";
                }
                layerToString += "\n";
            }

            //iterate through
            int biasRow = weightMatrix.length - 1;
            for (int j = 0; j < weightMatrix[biasRow].length; j++) {
                biasToString += weightMatrix[biasRow][j] + ";";
            }
            layerToString += biasToString;
            output += layerToString + "\n\n";
        }
        try {
            writeToFile(output, path);
        } catch (IOException exception) {
            System.out.println("Speichern der Gewichte fehlgeschlagen!");
            exception.printStackTrace();
        }
    }

    private static void writeToFile(String output, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(output);

        writer.close();
    }
}
