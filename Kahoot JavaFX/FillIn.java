public class FillIn extends Question{
    public String toString(String a){
        int b = a.indexOf("{blank}");
        int c = b+7;
        String d = a.substring(0,b)+"______"+a.substring(c);
        return d;
    }
}