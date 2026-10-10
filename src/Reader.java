import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Reader {

    private static ArrayList<double[][]> allWeights = new ArrayList<>();
    private static int currentWeight = 0;


    public static double[] readInput(String source) {

        try {
            Scanner scanner = new Scanner(new File(source));

            String line = scanner.nextLine();
            scanner.close();

            String[] values = line.split(",");

            double[] input = new double[values.length];

            for (int i = 0; i < values.length; i++) {
                input[i] = Double.parseDouble(values[i]);
            }

            return input;

        } catch (Exception e) {
            System.out.println("Fehler beim Lesen des Inputs!");
            return new double[0];
        }
    }


    private static double[][] readWeights(String source) {

        // Nur beim ersten Aufruf Datei lesen
        if (allWeights.isEmpty()) {

            try {
                Scanner scanner = new Scanner(new File(source));

                // Input-Zeile überspringen
                scanner.nextLine();

                ArrayList<double[]> currentMatrix = new ArrayList<>();

                while (scanner.hasNextLine()) {

                    String line = scanner.nextLine().trim();

                    // Leere Zeilen überspringen
                    if (line.isEmpty()) {
                        continue;
                    }

                    // # ebene
                    if (line.startsWith("#")) {

                        // Wenn eine Matrix fertig ist → speichern
                        if (!currentMatrix.isEmpty()) {

                            double[][] matrix =
                                    new double[currentMatrix.size()][];

                            for (int i = 0; i < currentMatrix.size(); i++) {
                                matrix[i] = currentMatrix.get(i);
                            }

                            allWeights.add(matrix);
                            currentMatrix.clear();
                        }

                        continue;
                    }

                    // Eine Zeile der Matrix
                    String[] values = line.split(",");

                    double[] row = new double[values.length];

                    for (int i = 0; i < values.length; i++) {
                        row[i] = Double.parseDouble(values[i]);
                    }

                    currentMatrix.add(row);
                }

                // Letzte Matrix speichern
                if (!currentMatrix.isEmpty()) {

                    double[][] matrix =
                            new double[currentMatrix.size()][];

                    for (int i = 0; i < currentMatrix.size(); i++) {
                        matrix[i] = currentMatrix.get(i);
                    }

                    allWeights.add(matrix);
                }

                scanner.close();

            } catch (Exception e) {
                System.out.println("Fehler beim Lesen der Gewichte!");
                return new double[0][0];
            }
        }

        // Nächste Matrix zurückgeben
        if (currentWeight < allWeights.size()) {

            double[][] result = allWeights.get(currentWeight);

            currentWeight++;

            return result;
        }

        // Keine Matrizen mehr vorhanden
        return new double[0][0];
    }

    public static ArrayList<double[][]> getWeights(String source) {
        readWeights(source);
        return allWeights;
    }
}
