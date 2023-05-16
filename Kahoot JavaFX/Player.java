import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

public class Player extends Application {
    String name;
    int scores=0;
    int pincode;
    DataInputStream fromServer;
    DataOutputStream toServer;
    Stage window;
    int second=0;
    KeyFrame forTimer = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            if(second==10&&!onAns){
                if(onTest){
                    try {
                        toServer.writeInt(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        toServer.writeUTF("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            second++;
        }
    });
    Timeline timerAnimation = new Timeline(forTimer);
    boolean state;
    boolean onAns;
    boolean onTest;
    boolean corPincode;

    @Override
    public void start(Stage primaryStage) {
        Thread beginning = new Thread(()->{
            try {
                toServer.writeInt(pincode);
                corPincode=fromServer.readBoolean();
                while(!corPincode){
                    wrongPincodePage();
                    corPincode=fromServer.readBoolean();
                }
                toServer.writeUTF(name);
                int numOfQues = fromServer.readInt();

                for(int i = 0;i<numOfQues;i++){
                    state=fromServer.readBoolean();
                    Platform.runLater(()->{
                        playerPage(state);
                    });
                    onAns=false;
                    state = fromServer.readBoolean();
                    if(state){
                        correct();
                        scores++;
                    }
                    else {
                        incorrect();
                    }
                    toServer.writeInt(scores);
                }
                fromServer.readBoolean();
                end();


                } catch (Exception e) {
                e.printStackTrace();
            }


        });
        TextField enterName = new TextField();
        enterName.setPromptText("Enter name");
        TextField enterPincode = new TextField();
        enterPincode.setPromptText("Enter pincode");
        Button submit = new Button("Submit");
        submit.setOnAction(e->{
            try {
                Socket client = new Socket("localhost",2020);
                fromServer = new DataInputStream(client.getInputStream());
                toServer = new DataOutputStream(client.getOutputStream());

                name = enterName.getText();
                window.setTitle(name);
                try{
                    pincode=Integer.parseInt(enterPincode.getText());
                }
                catch (Exception ignored){
                    pincode=0;
                }
                beginning.start();
                primaryStage.setScene(new Scene(waiting(),400,400));

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        VBox vBox = new VBox();
        vBox.getChildren().addAll(enterName,enterPincode,submit);
        vBox.setAlignment(Pos.BASELINE_CENTER);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);
        primaryStage.setScene(new Scene(borderPane,400,400));
        window = primaryStage;
        window.show();

    }
    void wrongPincodePage(){
        TextField enterPincode = new TextField();
        enterPincode.setPromptText("Enter pincode");
        Button submit = new Button("Submit");
        submit.setOnAction(e->{
            try {
                try{
                    pincode=Integer.parseInt(enterPincode.getText());
                }
                catch (Exception ignored){
                    pincode=0;
                }
                toServer.writeInt(pincode);
                Thread.sleep(4);
                window.setScene(new Scene(waiting(),400,400));

            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        VBox vBox = new VBox();
        vBox.getChildren().addAll(enterPincode,submit);
        vBox.setAlignment(Pos.BASELINE_CENTER);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);
        Platform.runLater(()->{
            window.setScene(new Scene(borderPane,400,400));
        });
    }
    void playerPage(boolean state){
        second=0;
        onTest=false;
        timerOn();
        if(state) window.setScene(new Scene(forTest(),400,400));
        else window.setScene(new Scene(forFillIn(),400,400));
    }
    public void timerOn(){
        timerAnimation.setCycleCount(-1);
        timerAnimation.play();
    }
    Pane forTest(){
        onTest=true;
        GridPane gridPane = new GridPane();
        Rectangle[] rectangle = new Rectangle[4];
        Button[] rectangle1 = new Button[4];
        int n = 0;
        for(int i = 0;i<2;i++)
            for (int j = 0;j<2;j++){
                rectangle[n] = new Rectangle();
                rectangle1[n] = new Button();
                rectangle1[n].setMinSize(200,200);
                rectangle[n].setHeight(200);
                rectangle[n].setWidth(200);
                if(n==0) rectangle[n].setFill(Color.RED);
                else if(n==1) rectangle[n].setFill(Color.BLUE);
                else if(n==2) rectangle[n].setFill(Color.YELLOW);
                else rectangle[n].setFill(Color.GREEN);
                rectangle[n].setArcWidth(5);
                rectangle[n].setArcHeight(5);
                rectangle1[n].setGraphic(rectangle[n]);
                gridPane.add(rectangle1[n],i,j);
                n++;
            }
            rectangle1[0].setOnAction(e->{
                try {
                    toServer.writeInt(1);
                    window.setScene(new Scene(waiting(),400,400));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            rectangle1[1].setOnAction(e->{
                try {
                    toServer.writeInt(2);
                    window.setScene(new Scene(waiting(),400,400));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            rectangle1[2].setOnAction(e->{
                try {
                    toServer.writeInt(3);
                    window.setScene(new Scene(waiting(),400,400));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            rectangle1[3].setOnAction(e->{
                try {
                    toServer.writeInt(4);
                    window.setScene(new Scene(waiting(),400,400));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

        return gridPane;
    }
    Pane forFillIn(){
        StackPane stackPane = new StackPane();
        TextField textField = new TextField();
        Button submit = new Button("Submit");
        submit.setOnAction(e->{
            try {
                toServer.writeUTF(textField.getText());
                window.setScene(new Scene(waiting(),400,400));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        VBox vBox = new VBox();
        vBox.getChildren().addAll(textField,submit);
        textField.setPromptText("Write your answer");
        vBox.setAlignment(Pos.BASELINE_CENTER);
        stackPane.getChildren().addAll(vBox);
        return stackPane;
    }
    Pane waiting(){
        onAns=true;
        StackPane stackPane = new StackPane();
        Rectangle bg = new Rectangle(500,500,Color.AZURE);
        Text text = new Text("Waiting for others...");
        stackPane.getChildren().addAll(bg,text);
        return stackPane;
    }
    void correct(){
        StackPane stackPane = new StackPane();
        Rectangle bg = new Rectangle(500,500,Color.GREEN);
        Text text = new Text("Your answer is correct!");
        stackPane.getChildren().addAll(bg,text);
        Platform.runLater(()->{
            window.setScene(new Scene(stackPane,400,400));
        });
    }
    void incorrect(){
        StackPane stackPane = new StackPane();
        Rectangle bg = new Rectangle(500,500,Color.CRIMSON);
        Text text = new Text("Your answer is incorrect!");
        stackPane.getChildren().addAll(bg,text);
        Platform.runLater(()->{
            window.setScene(new Scene(stackPane,400,400));
        });
    }
    void end(){
        StackPane stackPane = new StackPane();
        Rectangle bg = new Rectangle(500,500,Color.LIGHTGREEN);
        Text text = new Text("Thanks for taking quiz!");

        stackPane.getChildren().addAll(bg,text);
        Platform.runLater(()->{
            window.setScene(new Scene(stackPane,400,400));
        });
    }
}
