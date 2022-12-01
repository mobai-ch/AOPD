package CsvUtil;

import Request.Request;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    public List<RowElement> readCsvFile(String fileName){
        List<RowElement> rows = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(Paths.get(fileName));
            CSVReader csvReader = new CSVReader(reader)) {
            String[] record;

            while ((record = csvReader.readNext()) != null) {
                RowElement rowElement = new RowElement();
                rowElement.setData(record);
                rows.add(rowElement);
            }
        } catch (IOException | CsvValidationException ex) {
            ex.printStackTrace();
        }
        return rows;
    }


    public void saveCsvFile(List<RowElement> rowElements, String filePath){
        File file = new File(filePath);
        try {
            FileWriter outFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outFile);
            for(RowElement rowElement: rowElements) {
                writer.writeNext(rowElement.getData());
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
