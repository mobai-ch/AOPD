package CsvUtil;

public class RowElement {
    private String[] data;


    public void setData(String[] data){
        this.data = data;
    }


    public String col(int pos){
        if(pos < data.length) {
            return data[pos];
        }else{
            return "None";
        }
    }


    public String[] getData(){
        return this.data;
    }
}
