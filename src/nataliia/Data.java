package nataliia;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;

import static java.lang.Math.sqrt;

public class Data {
    private ArrayList<ArrayList<Double>> objectsData;
    private ArrayList<ArrayList<Double>> featuresData;
    private ArrayList<ArrayList<Double>> newObjectsData;
    private ArrayList<ArrayList<Double>> newComponentsData;
    private int objectCount;
    private int featureCount;
    private int componentsCount;
    private int groupCount;
    private double[][] corelationMatrix;
    private ArrayList<ArrayList<Double>> standartizedObjectsData;
    private Eigen eigen;
    private double[] eigenValues;
    private double[][] eigenVectors;
    private double eigValuesSum;
    private ArrayList<Double> components_values = new ArrayList<>();
    private ArrayList<double[]> components_vectors = new ArrayList<>();
    //private ArrayList<double[]> factors = new ArrayList<>();
    private ArrayList<ArrayList<Double>> factors = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> partition = new ArrayList<>();

    public Data(ArrayList<ArrayList<Double>> objectsData) {
        this.objectsData = objectsData;
        this.objectCount = objectsData.size();
        this.featuresData = getTransposedData(objectsData);
        this.featureCount = this.featuresData.size();
        this.components_values = new ArrayList<>();
    }

    public ArrayList<ArrayList<Double>> getObjectsData() {
        return objectsData;
    }

    public ArrayList<ArrayList<Double>> getNewObjectsData() {
        return newObjectsData;
    }

    public ArrayList<ArrayList<Double>> getStandartizedObjectsData(String parametr) {
        this.standartizedObjectsData = standartize(objectsData, parametr);
        return standartizedObjectsData;
    }

    public Eigen getEigen() {
        this.corelationMatrix = calculateCorrelationMatrix(featuresData, featureCount, objectCount);
        this.eigen = new Eigen(this.corelationMatrix);
        this.eigenValues = eigen.getValues();
        this.eigenVectors = eigen.getVectors();

        double eigValuesSum = 0.;
        for (double tmp : eigenValues) {
            eigValuesSum += tmp;
        }
        this.eigValuesSum = eigValuesSum;
        return eigen;
    }

    public ArrayList<Double> getPrincipalComponents(double value, String parametr) {

        components_values.clear();
        components_vectors.clear();

        switch (parametr) {
            case "dispMinValue": {
                int i = 0;
                while (eigenValues[i] >= value) {
                    components_values.add(eigenValues[i]);
                    components_vectors.add(eigenVectors[i++]);
                }
                break;
            }
            case "dispPercentSum": {
                int i = 0;
                double dispPercentSum = 0;
                while (dispPercentSum < value && i < eigenValues.length) {
                    dispPercentSum += eigenValues[i] / eigValuesSum * 100;
                    components_values.add(eigenValues[i]);
                    components_vectors.add(eigenVectors[i++]);
                }
                break;
            }
            case "componentsCount": {
                for (int k = 0; k < value; k++) {
                    components_values.add(eigenValues[k]);
                    components_vectors.add(eigenVectors[k]);
                }
                break;
            }
        }
        this.componentsCount = components_values.size();
        return components_values;
    }

    public static ArrayList<ArrayList<Double>> standartize(ArrayList<ArrayList<Double>> objectsData,
                                                           String parametr) {
        ArrayList<ArrayList<Double>> featuresData = getTransposedData(objectsData);
        ArrayList<ArrayList<Double>> newDataFeatures = new ArrayList<>();
        for (ArrayList<Double> list : featuresData) {
            double middleValue = middleValue(list);
            double middleSquareValue = middleSquareValue(list, middleValue);
            ArrayList<Double> newList = new ArrayList<>();
            for (Double element : list) {
                element = Math.abs(middleValue - element);
                if (parametr.equals("middle square")) element /= middleSquareValue;
                newList.add(element);
            }
            newDataFeatures.add(newList);
        }
        ArrayList<ArrayList<Double>> newDataObjects = getTransposedData(newDataFeatures);
        return newDataObjects;
    }

    public ArrayList<ArrayList<Double>> getEigenTableModel() {
        ArrayList<ArrayList<Double>> result = new ArrayList<>();
        double[] values = eigen.getValues();
        result.add(arrayAsList(values));

        ArrayList<Double> percents = new ArrayList<>();
        ArrayList<Double> percentSum = new ArrayList<>();
        ArrayList<Double> titles = new ArrayList<>();
        double eigValuesSumAccumulated = 0;
        for (double value : values) {
            double percent = value / this.eigValuesSum * 100.0;
            percents.add(percent);
            eigValuesSumAccumulated += percent;
            percentSum.add(eigValuesSumAccumulated);
            //titles.add();
        }
        result.add(percents);
        result.add(percentSum);

        int length = eigenVectors[0].length;
        for (int i = 0; i < length; i++) {
            ArrayList<Double> vectorCoordinates = new ArrayList<>();
            for (int j = 0; j < length; j++) {
                vectorCoordinates.add(eigenVectors[j][i]);
            }
            result.add(vectorCoordinates);
        }
        return result;
    }

    public ArrayList<ArrayList<Double>> getCorelationTableModel() {
        this.newComponentsData = getTransposedData(newObjectsData);
        return doubleArrayToDoubleList(calculateCorrelationMatrix(featuresData, newComponentsData, featureCount, componentsCount, objectCount));
    }

    public void saveSelectedComponentsObjectData(String filePath) {
        this.newObjectsData = new ArrayList<>();
        ArrayList<String> writeToFile = new ArrayList<>();
        for (int i = 0; i < objectCount; i++) {
            ArrayList<Double> object = new ArrayList<>();
            StringBuilder string = new StringBuilder();
            for (int j = 0; j < componentsCount; j++) {
                double sum = 0;
                for (int k = 0; k < featureCount; k++) {
                    sum += objectsData.get(i).get(k) * components_vectors.get(j)[k];
                }
                object.add(sum);
                NumberFormat formatter = new DecimalFormat("#.00");
                string.append(formatter.format(sum) + " ");
            }
            newObjectsData.add(object);
            writeToFile.add(string.toString());
        }
        writeToFile(filePath, writeToFile);
    }

    public void extremalGroupingMethod1(int groupCount) {
        factors.clear();
        partition.clear();

        for (int i = 0; i < groupCount; i++) {
            factors.add(featuresData.get(i));
            partition.add(new ArrayList<>());
        }

        while (true) {              /////  ???????? условие выхода из цикла
            calculatePartition();
            calculateFactors();
        }
    }

    //не проверено
    private void calculateFactors() {
        for (int l = 0; l < groupCount; l++) {
            factors.clear();
            double denumerator = 0;
            //корреляция в каждой группе
            partition.get(l).sort(Comparator.naturalOrder());
            ArrayList<Integer> tmp = partition.get(l);
            int tmp_size = tmp.size();
            ArrayList<ArrayList<Double>> tmp_feature_list = new ArrayList<>();
            for (int i : tmp) {
                tmp_feature_list.add(featuresData.get(i));
            }
            double[][] correlationMatrixL = calculateCorrelationMatrix(tmp_feature_list, tmp_size, objectCount);
            double[] maxEigenVectorL = (new Eigen(correlationMatrixL).getVectors()[0]);
            for (int i = 0; i < tmp_size; i++) {
                for (int j = i; j < tmp_size; j++) {
                    denumerator += maxEigenVectorL[i] * maxEigenVectorL[j] * corelation(featuresData.get(tmp.get(i)), featuresData.get(tmp.get(j)));
                }
            }
            denumerator = Math.sqrt(denumerator);

            ArrayList<Double> factor = new ArrayList<>();
            for (int k = 0; k < objectCount; k++) {
                double numerator = 0;
                for (int i = 0; i < tmp_size; i++) {
                    numerator += maxEigenVectorL[i] *
                            featuresData.get(tmp.get(i)).get(k);    //  ?????????????????
                }

                factor.add(numerator / denumerator);
            }
            factors.add(factor);
        }
    }

    //не проверено
    private void calculatePartition() {
        //все очистить
        for (ArrayList<Integer> part : partition) {
            part.clear();
        }
        for (int i = 0; i < featureCount; i++) {
            double cor_0 = Math.pow(corelation(featuresData.get(i), factors.get(0)), 2.);
            int index = 0;
            for (int q = 1; q < groupCount; q++) {
                double cor_q = Math.pow(corelation(featuresData.get(i), factors.get(q)), 2.);
                if (cor_q > cor_0) {
                    cor_0 = cor_q;
                    index = q;
                }
            }
            partition.get(index).add(i);
        }
    }

    //не проверено
    private double corelation(ArrayList<Double> x, ArrayList<Double> f) {
        double numerator = 0;
        double denumerator = 1;
        double denumeratorI = 0;
        double denumeratorJ = 0;
        double midValueI = middleValue(x);
        double midValueJ = middleValue(f);
        for (int l = 0; l < objectCount; l++) {
            numerator += (x.get(l) - midValueI) * (f.get(l) - midValueJ);
            denumeratorI += (x.get(l) - midValueI) * (x.get(l) - midValueI);
            denumeratorJ += (f.get(l) - midValueJ) * (f.get(l) - midValueJ);
        }
        denumerator = sqrt(denumeratorI * denumeratorJ);
        return numerator / denumerator;
    }

    private static ArrayList<ArrayList<Double>> getTransposedData(ArrayList<ArrayList<Double>> objectsData) {
        ArrayList<ArrayList<Double>> features = new ArrayList<>();
        int featureCount = (objectsData.size() > 0) ? objectsData.get(0).size() : 0;
        for (int i = 0; i < featureCount; i++) {
            ArrayList<Double> line = new ArrayList<>();
            for (ArrayList<Double> sublist : objectsData) {
                line.add(sublist.get(i));
            }
            features.add(line);
        }
        return features;
    }

    private static double middleValue(ArrayList<Double> list) {
        double middleValue = 0;
        for (Double element : list) {
            middleValue += element;
        }
        middleValue /= list.size();
        return middleValue;
    }

    private static double middleSquareValue(ArrayList<Double> list, double middleValue) {
        double middleSquareValue = 0;
        for (Double element : list) {
            middleSquareValue += (middleValue - element) * (middleValue - element);
        }
        middleSquareValue /= (list.size() - 1);
        middleSquareValue /= sqrt(middleSquareValue);
        return middleSquareValue;
    }

    private static double[][] calculateCorrelationMatrix(ArrayList<ArrayList<Double>> featuresData,
                                                         int featureCount, int objectCount) {
        double[][] correlationMatrix = new double[featureCount][featureCount];
        for (int i = 0; i < featureCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                if (i == j) correlationMatrix[i][i] = 1.0;
                else {
                    double numerator = 0;
                    double denumerator = 1;
                    double denumeratorI = 0;
                    double denumeratorJ = 0;
                    double midValueI = middleValue(featuresData.get(i));
                    double midValueJ = middleValue(featuresData.get(j));
                    for (int l = 0; l < objectCount; l++) {
                        numerator += (featuresData.get(i).get(l) - midValueI) * (featuresData.get(j).get(l) - midValueJ);
                        denumeratorI += (featuresData.get(i).get(l) - midValueI) * (featuresData.get(i).get(l) - midValueI);
                        denumeratorJ += (featuresData.get(j).get(l) - midValueJ) * (featuresData.get(j).get(l) - midValueJ);
                    }
                    denumerator = sqrt(denumeratorI * denumeratorJ);
                    correlationMatrix[i][j] = correlationMatrix[j][i] = numerator / denumerator;
                }
            }
        }
        return correlationMatrix;
    }

    private static double[][] calculateCorrelationMatrix(ArrayList<ArrayList<Double>> featuresData,
                                                         ArrayList<ArrayList<Double>> componentsData,
                                                         int featureCount, int componentsCount, int objectCount) {
        double[][] correlationMatrix = new double[featureCount][componentsCount];
        for (int i = 0; i < featureCount; i++) {
            for (int j = 0; j < componentsCount; j++) {
                double numerator = 0;
                double denumerator = 1;
                double denumeratorI = 0;
                double denumeratorJ = 0;
                double midValueI = middleValue(featuresData.get(i));
                double midValueJ = middleValue(componentsData.get(j));
                for (int l = 0; l < objectCount; l++) {
                    numerator += (featuresData.get(i).get(l) - midValueI) * (componentsData.get(j).get(l) - midValueJ);
                    denumeratorI += (featuresData.get(i).get(l) - midValueI) * (featuresData.get(i).get(l) - midValueI);
                    denumeratorJ += (componentsData.get(j).get(l) - midValueJ) * (componentsData.get(j).get(l) - midValueJ);
                }
                denumerator = sqrt(denumeratorI * denumeratorJ);
                correlationMatrix[i][j] = numerator / denumerator;
            }
        }
        return correlationMatrix;
    }

    private ArrayList<Double> arrayAsList(double[] values) {
        ArrayList<Double> result = new ArrayList<>();
        for (double value : values) {
            result.add(value);
        }
        return result;
    }

    private void writeToFile(String filePath, ArrayList<String> writeToFile) {
        File file = new File(filePath);
        try {
            if (!file.exists()) file.createNewFile();
            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            try {
                for (String string : writeToFile) {
                    out.println(string);
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<ArrayList<Double>> doubleArrayToDoubleList(double[][] data) {
        ArrayList<ArrayList<Double>> result = new ArrayList<>();
        for (double[] array : data) {
            ArrayList<Double> list = new ArrayList<>();
            for (double element : array) {
                list.add(element);
            }
            result.add(list);
        }
        return result;
    }

}