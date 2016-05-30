package nataliia;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    public Button calculateBtn;
    public TableView<String[]> dataTable;
    public TableView<String[]> newDataTable;
    public TableView<String[]> eigenValuesTable;
    public TableView<String[]> corelationTable;
    public TextField dispMinValueTF, dispPercentSumTF, componentsCountTF;
    public TextField intervalStartTF, intervalEndTF;
    public CheckBox standartizeCB;
    public RadioButton middleStandartRB, intervalStandartRB;
    public Pane cryteriaPane;
    public RadioButton dispMinValueRB, dispPercentSumRB, componentsCountRB;
    public TabPane methodsTabPane;
    public Pane standartizedPane;
    public Button selectBtn;
    public Button openButton;
    public LineChart<Number, Number> chart;
    public NumberAxis yAxis;
    public NumberAxis xAxis;
    public TextField epselenTF;
    public TextField groupCountTF;
    public Button startExtremalGroupingBtn;
    public Tab extremalGroupingMethodTab;
    public TableView newFactorsTable;
    public TableView factorsCorelationTable;
    public Button saveFactorsBtn;
    public AnchorPane anchorPaneTab1;
    public Tab principalComponentsMethodTab;
    public AnchorPane anchorPaneTab2;

    JFileChooser jFileChooser;
    FileChooser fileChooser;

    private final ToggleGroup groupStandart = new ToggleGroup();
    private final ToggleGroup groupCryteria = new ToggleGroup();

    private boolean radiobuttonsStandartAdded = false;
    private boolean radiobuttonsCtyteriaAdded = false;

    private Data dataObject;
    private ArrayList<ArrayList<Double>> workingObjectData;
    //private ArrayList<ArrayList<Double>> workingFeatureData;
    private Eigen eigen;
    private ArrayList<Double> components = new ArrayList<>();

    @FXML
    private void initialize() {
        standartizedPane.setDisable(true);
//        anchorPaneTab2.setVisible(false);
    }

    public void openButtonClick(ActionEvent actionEvent) throws FileNotFoundException {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("D:\\2 семестр\\диплом\\Diploma\\"));
        fileChooser.setTitle("Open file with objectsData:");
        ArrayList<ArrayList<Double>> readFile = Functions.readFile(fileChooser.showOpenDialog(openButton.getScene().getWindow()));
        if (readFile == null)
            return;
        dataObject = new Data(readFile);
        workingObjectData = dataObject.getObjectsData();
        methodsTabPane.setDisable(false);
        standartizedPane.setDisable(false);
        loadDataToTableView(dataTable, dataObject.getObjectsData(), 0);
    }

    public void calculateBtnClick(ActionEvent actionEvent) {
        eigen = dataObject.getEigen();

        eigenValuesTable.setVisible(true);
        newDataTable.setVisible(true);

        loadDataToTableView(eigenValuesTable, dataObject.getEigenTableModel(), 1);

        selectBtn.setDisable(false);

        chartSetting(chart,
                xAxis,
                yAxis,
                "Number of Principal Component",
                "Value",
                "Principal Components",
                "Series1",
                dataObject.getEigen().getValues());

        cryteriaPane.setDisable(false);
        if (!radiobuttonsCtyteriaAdded) {
            dispPercentSumRB.setSelected(true);
            dispPercentSumTF.setVisible(true);

            dispMinValueRB.setToggleGroup(groupCryteria);
            dispPercentSumRB.setToggleGroup(groupCryteria);
            componentsCountRB.setToggleGroup(groupCryteria);
            radiobuttonsCtyteriaAdded = true;
        }
    }

    private void chartSetting(LineChart<Number, Number> chart, NumberAxis xAxis, NumberAxis yAxis, String xLabel,
                              String yLabel, String title, String seriesName, double[] data) {
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);
        chart.setTitle(title);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        chartPopulating(series, data);
        chart.getData().add(series);
    }

    private void chartPopulating(XYChart.Series<Number, Number> series, double[] values) {
        for (int i = 0; i < values.length; i++) {
            series.getData().add(new XYChart.Data<>(i + 1, values[i]));
        }
    }

    public void selectBtnClicked(Event event) {
        //// по пороговому значению главных компонент
        if (dispMinValueRB.isSelected()) {
            if (!dispMinValueTF.getText().equals("")) {
                double dispMinValue;
                try {
                    dispMinValue = Double.valueOf(dispMinValueTF.getText());
                    components = dataObject.getPrincipalComponents(dispMinValue, "dispMinValue");
                } catch (NumberFormatException e) {
                    dispMinValueTF.setText("Please, enter ONLY numbers here");
                }
            }
        }
        //// по сумме процента дисперсий
        else if (dispPercentSumRB.isSelected()) {
            if (!dispPercentSumTF.getText().equals("")) {
                try {
                    double dispPercentSum = Double.valueOf(dispPercentSumTF.getText());
                    components = dataObject.getPrincipalComponents(dispPercentSum, "dispPercentSum");
                } catch (NumberFormatException e) {
                    dispPercentSumTF.setText("Please, enter ONLY numbers here");
                }
            }
        }
        //// по количеству компонент
        else if (componentsCountRB.isSelected()) {
            if (!componentsCountTF.getText().equals("")) {
                try {
                    double componentsCount = Double.valueOf(componentsCountTF.getText());
                    components = dataObject.getPrincipalComponents(componentsCount, "componentsCount");
                } catch (NumberFormatException e) {
                    dispPercentSumTF.setText("Please, enter ONLY numbers here");
                }
            }
        }

        jFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        jFileChooser.setFileFilter(filter);
        jFileChooser.setCurrentDirectory(new File("D:"));//\\2 семестр\\диплом\\Diploma\\"));
        if (jFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            dataObject.saveSelectedComponentsObjectData(workingObjectData, jFileChooser.getSelectedFile().getAbsoluteFile() + ".txt");
            newDataTable.setVisible(true);
            loadDataToTableView(newDataTable, dataObject.getNewObjectsData(), 0);
        }

        corelationTable.setVisible(true);
        loadDataToTableView(corelationTable, dataObject.getComponentsCorelationTableModel(), 2);

        extremalGroupingMethodTab.setDisable(false);
    }

    public void standatizationClicked(Event event) {
        boolean selected = standartizeCB.isSelected();

        workingObjectData = (selected) ? dataObject.getStandartizedObjectsData() : dataObject.getObjectsData();
        loadDataToTableView(dataTable, workingObjectData, 0);

        middleStandartRB.setSelected(selected);
        middleStandartRB.setDisable(!selected);
        intervalStandartRB.setDisable(!selected);
        intervalStartTF.setDisable(!selected);
        intervalEndTF.setDisable(!selected);

        if (!radiobuttonsStandartAdded) {
            middleStandartRB.setToggleGroup(groupStandart);
            intervalStandartRB.setToggleGroup(groupStandart);

            radiobuttonsStandartAdded = true;
        }

        setIntervalTFOnKeyPressed(intervalStartTF, event);
        setIntervalTFOnKeyPressed(intervalEndTF, event);
    }

    public void middleStandartRBClicked(Event event) {
        workingObjectData = dataObject.getStandartizedObjectsData();
        loadDataToTableView(dataTable, workingObjectData, 0);
    }

    public void intervalStandartRBClicked(Event event) {
        String start, end;
        if (!(start = intervalStartTF.getText()).equals("") && !(end = intervalEndTF.getText()).equals("")) {
            Integer intervalStart, intervalEnd;
            try {
                intervalStart = Integer.valueOf(start);
                intervalEnd = Integer.valueOf(end);
                workingObjectData = dataObject.getStandartizedObjectsData(intervalStart, intervalEnd);
                loadDataToTableView(dataTable, workingObjectData, 0);
            } catch (NumberFormatException e) {
                intervalStartTF.setText("Please, enter ONLY INTEGER numbers here");
            }
        }
    }

    public void setIntervalTFOnKeyPressed(TextField textField, Event event) {
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    intervalStandartRBClicked(event);
                }
            }
        });
    }

    public void dispMinValueRBClicked(Event event) {
        dispMinValueTF.setVisible(true);
        dispPercentSumTF.setVisible(false);
        componentsCountTF.setVisible(false);
    }

    public void dispPercentSumRBClicked(Event event) {
        dispMinValueTF.setVisible(false);
        dispPercentSumTF.setVisible(true);
        componentsCountTF.setVisible(false);
    }

    public void componentsCountRBClicked(Event event) {
        dispMinValueTF.setVisible(false);
        dispPercentSumTF.setVisible(false);
        componentsCountTF.setVisible(true);
    }

    public void loadDataToTableView(TableView tableView,
                                    ArrayList<ArrayList<Double>> objectsData, int parametr) {
        final ObservableList<String[]> observableList = FXCollections.observableArrayList();

        tableView.getColumns().clear();
        observableList.clear();
        tableView.setVisible(true);

        List<String[]> dataList = Functions.listToStringArray(objectsData, parametr);

        observableList.addAll(dataList);
        if (dataList.size() > 0) {
            for (int i = 0; i < dataList.get(0).length; i++) {
                TableColumn tableColumn;
                if (i == 0) {
                    tableColumn = new TableColumn("#");
                    tableColumn.setPrefWidth(40);
                } else {
                    tableColumn = new TableColumn(i + "");
                    tableColumn.setPrefWidth(70);
                }
                final int columnNumber = i;
                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> param) {
                        return new SimpleStringProperty(param.getValue()[columnNumber]);
                    }
                });
                if (parametr == 2) {
                    tableColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
                        public TableCell call(TableColumn param) {
                            return new TableCell<Double, String>() {

                                @Override
                                public void updateItem(String item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (!isEmpty()) {
                                        this.setTextFill(Color.RED);
                                        // Get fancy and change color based on data
                                        try {
                                            item = item.replace(',', '.');
                                            double tmp = Double.valueOf(item);
                                            if (tmp < -0.5)
                                                this.setTextFill(Color.YELLOWGREEN);
                                            else if (tmp > -0.5 && tmp < 0)
                                                this.setTextFill(Color.YELLOW);
                                            else if (tmp > 0 && tmp < 0.5)
                                                this.setTextFill(Color.ORANGE);
                                            else if (tmp > 0.5 && tmp < 1)
                                                this.setTextFill(Color.RED);
                                        } catch (Exception e) {
                                        } finally {
                                            setText(item);
                                        }
                                    }
                                }
                            };
                        }
                    });
                }
                tableView.getColumns().add(tableColumn);
            }
        }
        tableView.setItems(observableList);
    }

    public void startExtremalGroupingClicked(Event event) {
        String groupCountStr, epselenStr;
        int groupCountInt;
        double epselen;
        if (!(groupCountStr = groupCountTF.getText()).equals("")
                && !(epselenStr = epselenTF.getText()).equals("")) {
            try {
                groupCountInt = Integer.valueOf(groupCountStr);
                epselen = Double.valueOf(epselenStr);
                if (groupCountInt > components.size()) {
                    groupCountTF.setText("Enter group number less then features number");
                    return;
                }
                if (epselen < 0) {
                    epselenTF.setText("Enter correct epselen");
                    return;
                }
                dataObject.extremalGroupingMethod1(workingObjectData, groupCountInt, epselen);
            } catch (NumberFormatException e) {
                groupCountTF.setText("Please, enter ONLY INTEGER numbers here");
                epselenTF.setText("Please, enter ONLY DOUBLE values here");
            }

            loadDataToTableView(newFactorsTable, dataObject.getNewFactorsObjectsData(), 0);
            loadDataToTableView(factorsCorelationTable, dataObject.getFactorsCorelationTableModel(), 2);

        } else {
            epselenTF.setText("Enter correct epselen");
            groupCountTF.setText("Enter group number less then features number");
        }
    }

    public void saveFactorsBtnClicked(Event event) {
        jFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        jFileChooser.setFileFilter(filter);
        jFileChooser.setCurrentDirectory(new File("D:\\"));//2 семестр\\диплом\\Diploma\\"));
        if (jFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            dataObject.saveFactorsObjectData(jFileChooser.getSelectedFile().getAbsoluteFile() + ".txt");
            newDataTable.setVisible(true);
            loadDataToTableView(newDataTable, dataObject.getNewObjectsData(), 0);
        }
    }
}

