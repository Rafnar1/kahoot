import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class QuizMaker extends Application {
    Media media = new Media(new File("C:\\Users\\Alser\\OneDrive\\Desktop\\resources\\kahoot_music.mp3").toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    Text timer = new Text("timer");
    KeyFrame forTimer = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            if(second==60) {
                minutes++;
                second-=60;
            }
            if (second<10){
                timer.setText(minutes+":"+"0"+second);
            }
            else timer.setText(minutes+":"+second);
            if(second==10&&onCorrectAnsPane){
                try {
                    window.setScene(new Scene(currentPane(currentPage),800,500));
                    passedans=true;
                    passedques=false;
                    onCorrectAnsPane=false;
                    second=0;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if(second==10){
                passedans=false;

                onCorrectAnsPane=true;
                passedques=true;
                second=0;
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                correctAns(currentPage);
                currentPage++;
            }
            second++;
        }
    });
    boolean onCorrectAnsPane;
    Timeline timerAnimation = new Timeline(forTimer);
    Quiz a;
    //Pane[] pane;
    Stage window;
    //Scene scene;
    int second = 0;
    int minutes = 0;
    int currentPage = 0;
    //ArrayList<Object> LUIO = new ArrayList();
    Object[] LUIO;
    Object UIO;
    boolean passed;
    boolean passedques;
    boolean passedans;
    ArrayList<Player> table = new ArrayList();
    int pincode;


    @Override
    public void start(Stage primaryStage) throws IOException {
        window = primaryStage;
        Pane firstpage = firstPage();
        window.setScene(new Scene(firstpage,800,500));
        window.show();
        ServerSocket server = new ServerSocket(2020);
        new Thread(()->{
            while(true){
                try{
                Socket socket = server.accept();
                new Thread(()->{
                    try {
                        DataInputStream fromClient = new DataInputStream(socket.getInputStream());
                        DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
                        Player player = new Player();
                        int scores = 0;
                        int userPincode=0;
                        while (userPincode!=pincode){
                            userPincode = fromClient.readInt();
                            toClient.writeBoolean(userPincode == pincode);
                        }

                        String name = fromClient.readUTF();
                        player.setName(name);
                        player.setScores(scores);
                        synchronized (table){
                            table.add(player);
                        }
                        while (!passed) {
                            Thread.onSpinWait();
                        }
                        toClient.writeInt(a.getNumOfQuestions());
                        for(int i=0;i<a.getNumOfQuestions();i++){
                            Question curQues = a.getQuestionAt(currentPage);
                            if(a.isTestClass(curQues)){
                                toClient.writeBoolean(true);
                                boolean check = false;
                                int UI = fromClient.readInt();
                                Test test = (Test) curQues;
                                while (!passedques) {
                                    Thread.onSpinWait();
                                }
                                if(UI==0) toClient.writeBoolean(false);
                                else
                                    toClient.writeBoolean(test.getAnswer().equals(test.getOptionAt(UI-1)));
                            }
                            else {
                                toClient.writeBoolean(false);
                                String UI = fromClient.readUTF();
                                while (!passedques) {
                                    Thread.onSpinWait();
                                }
                                if(UI.equals("")) toClient.writeBoolean(false);
                                else toClient.writeBoolean(curQues.getAnswer().equalsIgnoreCase(UI));
                            }
                            scores = fromClient.readInt();
                            player.setScores(scores);
                            while (!passedans) {
                                Thread.onSpinWait();
                            }
                        }
                        toClient.writeBoolean(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            catch(IOException ignored){}}
        }).start();

    }


    public Pane currentPane(int index) throws FileNotFoundException {
        second = 0;
        minutes = 0;
        timerAnimation.setCycleCount(-1);
        timerAnimation.play();
        if(index==a.getNumOfQuestions()) return lastPage();
        else if(a.isTestClass(a.getQuestionAt(index))) return fillingTest(a.getQuestionAt(index),index+1);
        else return fillingFillIn(a.getQuestionAt(index),index+1);
    }
    public Pane firstPage() throws FileNotFoundException{
        Image image = new Image(new FileInputStream("C:\\Users\\Alser\\OneDrive\\Desktop\\resources\\img\\background.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(500);
        imageView.setFitWidth(800);
        FileChooser fileChooser = new FileChooser();
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(imageView);
        Button button = new Button("Choose a file");
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(imageView,button);
        borderPane.setCenter(stackPane);

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                File file;
                fileChooser.setTitle("Choose the quiz file");
                file = fileChooser.showOpenDialog(window);
                try {
                    a = Quiz.loadFromFile(file.getPath());
                    //a.fillingPanes();
                    LUIO = new Object[a.getNumOfQuestions()];
                    mediaPlayer.setCycleCount(-1);
                    mediaPlayer.play();
                    window.setTitle(a.getName());

                    window.setScene(new Scene(starting(),800,500));
                }
                catch (InvalidQuizFormatException | IOException e){
                    System.out.println("Exception in first page is caught");
                }
            }
        });

        return  borderPane;
    }
    public void correctAns(int i){
        timerAnimation.setCycleCount(-1);
        timerAnimation.play();
        StackPane stackPane = new StackPane();
        VBox vBox = new VBox();
        Rectangle bg = new Rectangle(1000,1000,Color.YELLOWGREEN);
        Text text = new Text(a.getQuestionAt(i).getAnswer());
        vBox.getChildren().addAll(text,topList());
        stackPane.getChildren().addAll(bg,vBox);
        vBox.setAlignment(Pos.CENTER);
        window.setScene(new Scene(stackPane,800,500));
    }
    public Text topList(){
        Collections.sort(table);
        String list = "";
        for(int i = 0; i<table.size();i++){
            if(i==5) break;
            list+=i+1+"."+table.get(i).getName()+" - "+ table.get(i).getScores();
            if(i!=4) list+="\n";
        }
        Text text = new Text(list);
        return text;
    }
    public Pane fillingTest(Question question,int numOfQuestion) throws FileNotFoundException {
        UIO = new Object();
        Test test = (Test)question;
        GridPane gridPane = new GridPane();
        Image image = new Image(new FileInputStream("C:\\Users\\Alser\\OneDrive\\Desktop\\resources\\img\\logo.png"));
        ImageView imageView = new ImageView(image);

        Text text = new Text(numOfQuestion+")"+question.getDescription());
        VBox vBox = new VBox();
        vBox.getChildren().addAll(text,timer,imageView);
     //   Timeline timeline = new Timeline(forTimer);
     //   timeline.setCycleCount(-1);
     //   timeline.play();
        vBox.setAlignment(Pos.BASELINE_CENTER);
        vBox.setSpacing(10);
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        RadioButton[] choice = new RadioButton[4];
        choice[0] = new RadioButton();
        choice[1] = new RadioButton();
        choice[2] = new RadioButton();
        choice[3] = new RadioButton();
        ToggleGroup group = new ToggleGroup();
        choice[0].setToggleGroup(group);
        choice[1].setToggleGroup(group);
        choice[2].setToggleGroup(group);
        choice[3].setToggleGroup(group);
        if(LUIO[currentPage] instanceof Integer){
            int selectedOption = (int)LUIO[currentPage];
            if(selectedOption!=0) choice[selectedOption-1].setSelected(true);}
        Rectangle rectangle [] = new Rectangle[4];
        int n = 0;
        for(int i = 0;i<2;i++){
            for (int j = 0;j<2;j++){
                rectangle[n] = new Rectangle();
                rectangle[n].setHeight(70);
                rectangle[n].setWidth(300);
                if(n==0) rectangle[n].setFill(Color.RED);
                else if(n==1) rectangle[n].setFill(Color.BLUE);
                else if(n==2) rectangle[n].setFill(Color.YELLOW);
                else rectangle[n].setFill(Color.GREEN);
                rectangle[n].setArcWidth(5);
                rectangle[n].setArcHeight(5);
                gridPane.add(rectangle[n],i,j);
                n++;
            }
        }
        rectangle[0].setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(choice[0].isSelected()){
                    choice[0].setSelected(false);
                    LUIO[currentPage] = 0;
                }
                else {
                    choice[0].setSelected(true);
                    LUIO[currentPage] = 1;
                }
            }
        });
        rectangle[1].setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(choice[1].isSelected()){
                    choice[1].setSelected(false);
                    LUIO[currentPage] = 0;
                }
                else {
                    choice[1].setSelected(true);
                    LUIO[currentPage] = 2;
                }
            }
        });
        rectangle[2].setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(choice[2].isSelected()){
                    choice[2].setSelected(false);
                    LUIO[currentPage] = 0;
                }
                else {
                    choice[2].setSelected(true);
                    LUIO[currentPage] = 3;
                }
            }
        });
        rectangle[3].setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(choice[3].isSelected()){
                    choice[3].setSelected(false);
                    LUIO[currentPage] = 0;
                }
                else {
                    choice[3].setSelected(true);
                    LUIO[currentPage] = 4;
                }
            }
        });
        for(int i = 0;i<4; i++){
            choice[i].setText(test.getOptionAt(i));
            choice[i].setTextFill(Color.WHITE);
            choice[i].setWrapText(true);
        }
        choice[0].setOnAction(e->{
            LUIO[currentPage] = 1;
        });
        choice[1].setOnAction(e->{
            LUIO[currentPage] = 2;
        });
        choice[2].setOnAction(e->{
            LUIO[currentPage] = 3;
        });
        choice[3].setOnAction(e->{
            LUIO[currentPage] = 4;
        });
        gridPane.add(choice[0],0,0);
        gridPane.add(choice[1],0,1);
        gridPane.add(choice[2],1,0);
        gridPane.add(choice[3],1,1);


        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        BorderPane borderPane = new BorderPane();
        borderPane.setBottom(gridPane);
        borderPane.setTop(vBox);
        for(int i = 0;i<4; i++){
            choice[i].setWrapText(true);
            choice[i].setMaxWidth(rectangle[i].getLayoutX()+rectangle[i].getWidth());
        }
        if(numOfQuestion==1){
            Button right = new Button(">>");
            right.setAlignment(Pos.BASELINE_CENTER);
            right.setOnAction(new rightButtonHandler());
            borderPane.setRight(right);
        }
        else if(numOfQuestion==a.getNumOfQuestions()){
            Button left = new Button("<<");
            left.setOnAction(new leftButtonHandler());
            Button submit = new Button("Submit");
            submit.setOnAction(new rightButtonHandler());
            borderPane.setLeft(left);
            borderPane.setRight(submit);
        }
        else {
            Button left = new Button("<<");
            left.setOnAction(new leftButtonHandler());
            Button right = new Button(">>");
            right.setOnAction(new rightButtonHandler());
            borderPane.setRight(right);
            borderPane.setLeft(left);
        }
        return borderPane;
    }
    public Pane fillingFillIn(Question question,int numOfQuestion) throws FileNotFoundException {
        UIO = new Object();
        BorderPane borderPane = new BorderPane();
        FillIn fillIn = (FillIn) question;
        Image image = new Image(new FileInputStream("C:\\Users\\Alser\\OneDrive\\Desktop\\resources\\img\\k.png"));
        ImageView imageView = new ImageView(image);
        Image image1 = new Image(new FileInputStream("C:\\Users\\Alser\\OneDrive\\Desktop\\resources\\img\\fillin.png"));
        ImageView imageView1 = new ImageView(image1);
        imageView1.setFitHeight(200);
        imageView1.setFitWidth(300);
        imageView.setFitWidth(25);
        imageView.setFitHeight(20);
        Label label = new Label(numOfQuestion+")"+fillIn.getDescription(),imageView);
        label.setContentDisplay(ContentDisplay.LEFT);
     //   Timeline timeline = new Timeline(forTimer);
      //  timeline.setCycleCount(-1);
      //  timeline.play();
        VBox vBox = new VBox();
        Text text1 = new Text("Type your answers here:");
        TextField textField = new TextField();
        //   try{
        String UIT = (String)LUIO[currentPage];
        textField.setText(UIT);
        //   }
        //  catch (ClassCastException e){
        //     System.out.println("ClassCastException is caught");
        // }
        textField.setOnKeyTyped(e->{
            LUIO[currentPage] = textField.getText();
        });
        textField.setMaxWidth(250);
        vBox.getChildren().addAll(label,timer,imageView1,text1,textField);
        vBox.setAlignment(Pos.BASELINE_CENTER);
        borderPane.setTop(vBox);
        if(numOfQuestion==1){
            Button right = new Button(">>");
            right.setOnAction(new rightButtonHandler());
            borderPane.setRight(right);
        }
        else if(numOfQuestion==a.getNumOfQuestions()){
            Button left = new Button("<<");
            left.setOnAction(new leftButtonHandler());
            Button submit = new Button("Submit");
            submit.setOnAction(new rightButtonHandler());
            borderPane.setLeft(left);
            borderPane.setRight(submit);
        }
        else {
            Button left = new Button("<<");
            left.setOnAction(new leftButtonHandler());
            Button right = new Button(">>");
            right.setOnAction(new rightButtonHandler());
            borderPane.setRight(right);
            borderPane.setLeft(left);
        }
        return borderPane;
    }
    public Pane lastPage() throws FileNotFoundException {
        Image image = new Image(new FileInputStream("C:\\Users\\Alser\\OneDrive\\Desktop\\resources\\img\\result.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        Text text = new Text("Your Result:\n\n");
        Text percentage = new Text(getPercentage()+"%\n");
        Text correctAnswers = new Text("Number of correct answers: "+getNumberOfCorrectAnswers());
        Text finished = new Text("Finished in: "+timer.getText());
        mediaPlayer.stop();

        Rectangle showAnswers = new Rectangle(300,70, Color.BLUE);
        showAnswers.setArcWidth(5);
        showAnswers.setArcHeight(5);
        Text showAns = new Text("Show Answers");
        StackPane show = new StackPane();
        show.getChildren().addAll(showAnswers,showAns);
        showAns.setFill(Color.WHITE);
        Rectangle closeTest = new Rectangle(300,70,Color.RED);
        closeTest.setArcWidth(5);
        closeTest.setArcHeight(5);
        closeTest.setOnMouseClicked(new closeTest());
        Text closeT = new Text("Close Test");
        closeT.setOnMouseClicked(new closeTest());
        closeT.setFill(Color.WHITE);
        StackPane close = new StackPane();
        close.getChildren().addAll(closeTest,closeT);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(text,percentage,correctAnswers,finished,show,close,imageView);
        vBox.setAlignment(Pos.BASELINE_CENTER);
        vBox.setSpacing(10);
        return vBox;
    }
    public Pane starting() throws FileNotFoundException {
        Image image = new Image(new FileInputStream("C:\\Users\\Alser\\OneDrive\\Desktop\\resources\\img\\background.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(500);
        imageView.setFitWidth(800);
        creatingPinCode();
        BorderPane borderPane = new BorderPane();
        Button button = new Button("Start");
        button.setOnAction(e->{
            try {
                window.setScene(new Scene(currentPane(currentPage),800,500));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            passed=true;
        });
        StackPane stackPane = new StackPane();
        Text text = new Text(pincode+"");
        text.setStyle("-fx-font: 24 arial;");
        text.setTextAlignment(TextAlignment.CENTER);
        borderPane.setTop(text);
        borderPane.setCenter(button);
        stackPane.getChildren().addAll(imageView,borderPane);
        return stackPane;
    }
    public void creatingPinCode(){
        String code1 = "";
        int n;
        for(int i = 0;i<6;i++){
            n = (int)(Math.random()*10);
            code1+=n;
        }
        int code = Integer.parseInt(code1);
        pincode = code;
    }
    public int getNumberOfCorrectAnswers(){
        int num = 0;
        for(int i = 0;i<a.getNumOfQuestions();i++){
            if(a.isTestClass(a.getQuestionAt(i))){
                Test test = (Test)a.getQuestionAt(i);
                if(LUIO[i] instanceof Integer && (int)LUIO[i]!=0)
                    if(test.getOptionAt((int)LUIO[i]-1).equals(test.getAnswer())) num++;
            }
            else {
                if(LUIO[i] instanceof String)
                    if(a.getQuestionAt(i).getAnswer().equalsIgnoreCase((String)LUIO[i])) num++;
            }
        }
        return num;
    }
    public double getPercentage(){
        return (double)((int)((((double)getNumberOfCorrectAnswers())/ a.getNumOfQuestions())*1000))/10;
    }
    class rightButtonHandler implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent actionEvent) {
            currentPage+=1;
            try {
                window.setScene(new Scene(currentPane(currentPage),800,500));
            } catch (FileNotFoundException e) {
                System.out.println("Exception is caught");
            }

        }
    }
    class leftButtonHandler implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent actionEvent) {
            currentPage-=1;
            try {
                window.setScene(new Scene(currentPane(currentPage),800,500));
            } catch (Exception e) {}
        }
    }
    class closeTest implements  EventHandler<MouseEvent>{

        @Override
        public void handle(MouseEvent mouseEvent) {
            try {
                System.out.println("Closing window");
                window.close();

            } catch (Exception e) {}
        }
    }
    class Player implements Comparable<Player>{
        String name;
        int scores;

        public void setName(String name) {
            this.name = name;
        }

        public void setScores(int scores) {
            this.scores = scores;
        }

        public String getName() {
            return name;
        }

        public int getScores() {
            return scores;
        }

        @Override
        public int compareTo(Player o) {
            if(this.scores<o.getScores()) return 1;
            else if(this.scores==o.getScores()) return 0;
            else return -1;
        }
    }
}

