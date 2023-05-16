abstract class Question{
    private String description;
    private String answer;
    protected void setDescription(String description){
        this.description=description;
    }
    protected void setAnswer(String answer){
        this.answer=answer;
    }
    protected String getDescription(){
        return description;
    }
    protected String getAnswer(){
        return answer;
    }
}