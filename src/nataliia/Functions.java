package nataliia;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Functions {

    public static ArrayList<ArrayList<Double>> readFile(File file)
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

    public static List<String[]> listToStringArray(
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
}