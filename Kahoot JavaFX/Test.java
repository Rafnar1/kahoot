import java.util.ArrayList;
import java.util.Arrays;

public class Test extends Question {
    private String[] options;
    private int numOfOptions;
    private ArrayList<String> labels;
    Test(){
        labels= new ArrayList<>();
    }

    public void setOptions(String[] options) {
        this.options = options;
        numOfOptions = options.length;
        setLabels();
    }
    public String getLabels(int index){
        return labels.get(index);
    }
    public void setLabels() {
        int count = 65;
        for(int i = 0; i<options.length; i++){
            labels.add((char)count+")");
            count++;
        }
    }

    public String getOptionAt(int num) {
        return options[num];
    }

    @Override
    public String toString() {
        return "Test{" +
                "options=" + Arrays.toString(options) +
                ", numOfOptions=" + numOfOptions +
                '}';
    }

    public int getNumOfOptions() {
        return numOfOptions;
    }
}