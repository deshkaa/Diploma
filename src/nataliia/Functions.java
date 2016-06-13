package nataliia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Functions {

    static ArrayList<ArrayList<Double>> readFile(File file)
            throws FileNotFoundException {
        ArrayList<ArrayList<Double>> data = new ArrayList<>();

        Scanner dataFile;
        try {
            dataFile = new Scanner(file);
        }catch (NullPointerException e){
            return null;
        }

        while (dataFile.hasNextLine()) {
            String line = dataFile.nextLine();

            ArrayList<Double> objectData = new ArrayList<>();

            Scanner lineScanner = new Scanner(line);
            lineScanner.useDelimiter(" ");
            while (lineScanner.hasNextDouble()) {
                objectData.add(lineScanner.nextDouble());
            }
            lineScanner.close();
            data.add(objectData);
        }
        return data;
    }

    static List<String[]> listToStringArray(
            ArrayList<ArrayList<Double>> objectsData, int parametr) {
        List<String[]> result = new ArrayList<>(objectsData.size());
        for (int i = 0; i < objectsData.size(); i++) {
            ArrayList<Double> tmpList = objectsData.get(i);
            int size = tmpList.size() + 1;
            String[] stringArray = new String[size];
            if (parametr == 0)
                stringArray[0] = "#" + (i + 1);
            if (parametr == 2)
                stringArray[0] = "" + (i + 1);
            for (int j = 1; j < size; j++) {
                NumberFormat formatter = new DecimalFormat("#0.00");
                stringArray[j] = formatter.format(tmpList.get(j - 1));
            }
            result.add(stringArray);
        }
        if (parametr == 1) {
            result.get(0)[0] = "Val";
            result.get(1)[0] = "%";
            result.get(2)[0] = "%+";
            result.get(3)[0] = "vect";
        }

        return result;
    }

    static ArrayList<Double> arrayAsList(double[] values) {
        ArrayList<Double> result = new ArrayList<>();
        for (double value : values) {
            result.add(value);
        }
        return result;
    }

    static void writeToFile(String filePath, ArrayList<String> writeToFile) {
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

    static ArrayList<ArrayList<Double>> doubleArrayToDoubleList(double[][] data) {
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