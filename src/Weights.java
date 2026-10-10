import java.util.ArrayList;
import java.util.Random;

public class Weights {

    private ArrayList<double[][]> weights;
    private static final Random rand = new Random();

    public Weights() {
        weights = new ArrayList<>();
    }


    public ArrayList<double[][]> getWeights() {
        return weights;
    }

    public void setWeights(ArrayList<double[][]> weights) {
        this.weights = weights;
    }

    public void addWeights(double[][] weights) {
        this.weights.add(weights);
    }


    //randomized weights for the next layer
    public static double[][] initWeight(int n, int k) {

        double[][] w = new double[n][k];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {

                w[i][j] =
                        Math.floor(rand.nextDouble(-1, 1) * 100)
                                / 100.0;
            }
        }

        return w;
    }


    public static double[][] unitMatrix(int n) {

        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1;
        }

        return matrix;
    }




    public static String printWeight(double[][] weights) {
        String output = "";

        for (int i = 0; i < weights.length;i++) {
            output += "[";
            for (int j = 0; j < weights[0].length; j++) {
                output += weights[i][j] + " ";
            }
            output += "\b]\n";
        }

        return output + "\b";
    }

    @Override
    public String toString() {
        String output = "";

        for (double[][] w : weights) {
            output += printWeight(w) + "\n";
        }

        return output;
    }
}
