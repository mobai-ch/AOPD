package LogUtil;

import CsvUtil.CsvUtil;

import java.util.ArrayList;
import java.util.List;
import CsvUtil.RowElement;

public class LogSet {
    private final List<Log> logs;

    public LogSet(){
        this.logs = new ArrayList<Log>();
    }


    public void addLog(Log log){
        this.logs.add(log);
    }

    public Log getLog(int pos){
        return this.logs.get(pos);
    }


    public List<RowElement> getRowElements(){
        CsvUtil csvUtil = new CsvUtil();
        List<RowElement> rowElements = new ArrayList<>();
        for(Log log: logs){
            rowElements.add(log.toRowElement());
        }
        return rowElements;
    }


    public void readRecord(String logFile){

    }

    public int size(){
        return this.logs.size();
    }
}
