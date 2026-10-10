public class Matrixoperations {
    public static double[] matrixmultiplication(double[] z, double[][] w) {
        if (w.length == 0 || w[0].length != z.length) {
            throw new IllegalArgumentException(
                    "Gewichtsmatrix und Layer sind nicht kompatibel"
            );
        }

        double[] output = new double[w.length];
        for(int i = 0; i < w.length; i++) {
            for(int j = 0; j < z.length; j++) {
                output[i] = output[i] + (z[j] * w[i][j]);
            }
        }
        return output;
    }
}

