package nataliia;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class Eigen {

    private double[][] matrix;
    private double[] values;
    private double[][] vectors;

    public Eigen(double[][] corelMatrix) {
        this.matrix = corelMatrix;
        Matrix matrix = new Matrix(corelMatrix);
        EigenvalueDecomposition eigDec = matrix.eig();
        values = eigDec.getRealEigenvalues();
        double [][] tmp = eigDec.getV().getArray();
        int size = tmp.length;
        vectors = new double[size][size];
        for (int i=0;i<size;i++)
            for (int j=0;j<size;j++){
                vectors[j][i]=tmp[i][j];
            }

        sort(values,vectors);
    }

    private void sort(double[] x, double[][] y){
        for (int i = 0; i < x.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < x.length - i - 1; j++) {
                if (x[j] < x[j + 1]) {
                    double tmp_value = x[j];
                    x[j] = x[j + 1];
                    x[j + 1] = tmp_value;

                    double[] tmp_vector = y[j];
                    y[j] = y[j + 1];
                    y[j + 1] = tmp_vector;

                    swapped = true;
                }
            }

            if(!swapped)
                break;
        }
    }

    public double[] getValues() {
        return values;
    }

    public double[][] getVectors() {
        return vectors;
    }
}
