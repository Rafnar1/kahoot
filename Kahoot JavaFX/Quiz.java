import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Quiz{
    public ArrayList<Question> getQuestions (){return questions;}

    private static String name;
    private ArrayList<Question> questions = new ArrayList();
    public void setName(String name){
        this.name= name;
    }
    public String getName(){
        return name;
    }
    public String toString(){
        return name;
    }
    public Question getQuestionAt(int index){return questions.get(index);}
    public void addQuestion(Question question){
        questions.add(question);
    }
    public static Quiz loadFromFile(String filename) throws FileNotFoundException, InvalidQuizFormatException {
        Quiz quiz = new Quiz();
        File file = new File(filename);
        checkFileExists(file);
        Scanner scanner = new Scanner(file);
        checkQuizFormat(scanner);
        name = filename.split("\\.")[0];
        Test test;
        FillIn fillIn;
        while(scanner.hasNextLine()){
            String description = scanner.nextLine();
            if(description.equals("")) break;
            if(description.contains("{blank}")){
                fillIn=new FillIn();
                fillIn.setDescription(fillIn.toString(description));
                fillIn.setAnswer(scanner.nextLine());
                quiz.addQuestion(fillIn);
                if(scanner.hasNextLine()){
                    scanner.nextLine();
                }
            }
            else{
                test = new Test();
                test.setDescription(description);
                ArrayList<String> options = new ArrayList<>();
                while(scanner.hasNextLine()){
                    String option = scanner.nextLine();
                    if(option.equals("")) break;
                    options.add(option);
                }
                test.setAnswer(options.get(0));
                Collections.shuffle(options);
                test.setOptions(options.toArray(new String [0] ));
                quiz.addQuestion(test);
            }
        }
        scanner.close();
        return quiz;
    }

    private static void checkFileExists(File file) {
        if(!file.exists()){
            System.out.println("Such a file does not exist!");
            System.exit(0);//important thing
        }
    }

    private static void checkQuizFormat(Scanner scanner) throws InvalidQuizFormatException {
        if(!scanner.hasNextLine()){
            throw new InvalidQuizFormatException("No line found");
        }
    }

    /*public void start(){
        Scanner in = new Scanner(System.in);
        String selectedAnswer;
        System.out.println("Welcome to "+name);
        System.out.println("------------------------------------------------");
        int correctAnswers = 0;
        for(int i = 0;i<questions.size();i++){
            System.out.print((i+1)+".");
            String answer = questions.get(i).getAnswer();
            System.out.println(questions.get(i).getDescription());
            if(isTestClass(questions.get(i))) {
                Test test = (Test) questions.get(i);
                for (int j = 0; j < test.getNumOfOptions(); j++) {
                    System.out.println(test.getLabels(j) + test.getOptionAt(j));
                }
                System.out.print("Type your answer:");
                char option = in.next().charAt(0);
                boolean passed = false;
                while (!passed)
                    try {
                        selectedAnswer = test.getOptionAt((int) option - 65);
                        if (selectedAnswer.equals(answer)) {
                            System.out.println("Your answer is correct!");
                            correctAnswers++;
                        } else {
                            System.out.println("Your answer is incorrect!");
                        }
                        passed = true;
                    } catch (Exception a) {
                        System.out.print("Invalid choice! Try again! (Ex: A, B, ...):");
                        option= in.next().charAt(0);
                    }
            }
            else{
                System.out.print("Type your answer: ");
                selectedAnswer=in.next();
                if(selectedAnswer.equalsIgnoreCase(answer)){
                    correctAnswers++;
                    System.out.println("Your answer is correct!");
                }
                else{
                    System.out.println("Your answer is incorrect;");
                }
            }
            System.out.println("------------------------------------------------");
        }
        double score = (((double)correctAnswers)/ questions.size())*100;
        System.out.println("Your score is "+correctAnswers+"/"+questions.size()+"("+score+"%)");
    }*/

    public boolean isTestClass(Question question) {
        return question instanceof Test;
    }
    public int getNumOfQuestions(){
        return questions.size();
    }

}
