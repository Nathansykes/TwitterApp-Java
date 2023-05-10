package TwitterApp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.media.*;


import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private static Client client;
    final private static String ImagesPath = "src/TwitterApp/Images/";
    final private static String UploadedFilesPath = "src/TwitterApp/uploadedFiles/";
    private static boolean ServerRunning;

    public static void main(String[] args) throws IOException {
        try
        {
            client = new Client();
            client.run();
            ServerRunning = true;
        }
        catch(Exception e)
        {
            ServerRunning = false;
        }

        launch(args);




    }

    Stage window;
    Scene visibleScene;
    static String currentUsername;


    private double x, y;
    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;

        if(!ServerRunning)
        {
            visibleScene = Alert("Server is not running");
            UpdateScene(visibleScene);
            window.show();
            return;
        }



        //Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        //visibleScene = new Scene(root);

        visibleScene = LoginScene();


        window.setResizable(false);
        window.setTitle("Twitter");
        window.getIcons().add(new Image("file:"+ImagesPath+"twitter.png"));



        UpdateScene(visibleScene);

        window.show();
    }

    public void UpdateScene(Scene scene) throws Exception
    {
        window.setScene(scene);
    }

    public Scene LoginScene() throws Exception
    {
        //pane
        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: white");

        //vbox
        VBox layout = new VBox();
        layout.setPrefHeight(480);
        layout.setPrefWidth(720);
        layout.setStyle("-fx-background-color: white");
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);
        layout.setFillWidth(false);

        //logo
        ImageView logo = new ImageView();
        logo.setImage(new Image("file:"+ImagesPath+"twitter.png"));
        logo.setFitHeight(150);
        logo.setPreserveRatio(true);

        //title label
        Label title = new Label("Welcome to twitter");
        title.setFont(new Font(40));

        //username label
        Label usernameLBL = new Label("Enter Username");
        usernameLBL.setFont(new Font(24));

        //username input
        TextField usernameTF = new TextField();
        usernameTF.setFont(new Font(16));
        usernameTF.setMinWidth(400);

        //login button
        Button loginBTN = new Button("Login");
        loginBTN.setFont(new Font(20));
        loginBTN.setDefaultButton(true);
        loginBTN.setPrefHeight(50);
        loginBTN.setPrefWidth(200);
        loginBTN.setStyle("-fx-background-color: ddd");
        DropShadow ds = new DropShadow();
        ds.setRadius(10);
        loginBTN.setEffect(ds);

        loginBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    Login(usernameTF.getText());
                    visibleScene = HomeScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        layout.getChildren().addAll(logo,title,usernameLBL,usernameTF,loginBTN);

        pane.getChildren().addAll(layout);
        return new Scene(pane ,720,480);
    }

    public void Login(String input) throws Exception
    {

        String command = "o "+input;
        if(client.executeCommand(command))
        {
            currentUsername = input;
            client.executeCommand("s "+currentUsername);// subscribing to self
        }
    }

    Rectangle tabUnderline = new Rectangle(240,2);
    boolean fileAttached;
    File attachedFile;
    String attachedFileName;



    public Scene HomeScene() throws Exception
    {

        AnchorPane mainPane = new AnchorPane();
        //<editor-fold desc="Base Template">
        //pane


        mainPane.setStyle("-fx-background-color: white");

        //HBox
        HBox navBar = new HBox();
        navBar.setPrefHeight(60);
        navBar.setPrefWidth(720);
        navBar.setStyle("-fx-background-color: edf8ff");


        //button1
        Button homeBTN = new Button("Home");
        homeBTN.setPrefWidth(240);
        homeBTN.setPrefHeight(60);
        homeBTN.setStyle("-fx-background-color: edf8ff");
        //button image
        ImageView homeIMG = new ImageView("file:"+ImagesPath+"house.png");
        homeIMG.setFitHeight(30);
        homeIMG.setFitWidth(30);
        homeIMG.setPreserveRatio(true);
        homeBTN.setGraphic(homeIMG);
        homeBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = HomeScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //button2
        Button channelsBTN = new Button("Channels");
        channelsBTN.setPrefWidth(240);
        channelsBTN.setPrefHeight(60);
        channelsBTN.setStyle("-fx-background-color: edf8ff");
        //button image
        ImageView channelsIMG = new ImageView("file:"+ImagesPath+"friends.png");
        channelsIMG.setFitHeight(30);
        channelsIMG.setFitWidth(30);
        channelsIMG.setPreserveRatio(true);
        channelsBTN.setGraphic(channelsIMG);
        channelsBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = ChannelsScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //button3
        Button logoutBTN = new Button(" Logout");
        logoutBTN.setPrefWidth(240);
        logoutBTN.setPrefHeight(60);
        logoutBTN.setStyle("-fx-background-color: edf8ff");
        //button image
        ImageView logoutIMG = new ImageView("file:"+ImagesPath+"exit.png");
        logoutIMG.setFitHeight(30);
        logoutIMG.setFitWidth(30);
        logoutIMG.setPreserveRatio(true);
        logoutBTN.setGraphic(logoutIMG);
        logoutBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = LoginScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //info pane
        AnchorPane infoPane = new AnchorPane();
        infoPane.setPrefHeight(25);
        infoPane.setPrefWidth(720);
        //infoPane.setStyle("-fx-background-color: ddd");
        infoPane.setLayoutY(60);

        //label
        Label usernameLBL = new Label("Logged in as "+currentUsername);
        usernameLBL.setFont(new Font(16));

        //shape
        tabUnderline.setFill(Color.color(0.0,0.278,0.467));
        tabUnderline.setLayoutX(0);

        infoPane.getChildren().addAll(tabUnderline,usernameLBL);

        navBar.getChildren().addAll(homeBTN,channelsBTN,logoutBTN);


        //</editor-fold>

        //<editor-fold desc="Publish Message">
        //text area
        fileAttached = false;
        TextArea messageTA = new TextArea();
        messageTA.setWrapText(true);
        messageTA.setPromptText("Write a message...");
        messageTA.setFont(new Font(14));
        messageTA.setPrefHeight(270);
        messageTA.setPrefWidth(240);
        messageTA.setLayoutX(15);
        messageTA.setLayoutY(90);

        //file name label
        Label fileLabel = new Label();
        fileLabel.setLayoutX(30);
        fileLabel.setLayoutY(365);
        fileLabel.setMaxWidth(200);

        //add image button
        Button attachFileBTN = new Button("Attach File");
        attachFileBTN.setFont(new Font(14));
        attachFileBTN.setDefaultButton(true);
        attachFileBTN.setPrefHeight(20);
        attachFileBTN.setPrefWidth(200);
        attachFileBTN.setLayoutX(30);
        attachFileBTN.setLayoutY(390);
        attachFileBTN.setStyle("-fx-background-color: ddd");
        DropShadow ds = new DropShadow();
        ds.setRadius(10);
        attachFileBTN.setEffect(ds);
            ImageView attachIMG = new ImageView("file:"+ImagesPath+"plus.png");
            attachIMG.setFitHeight(20);
            attachIMG.setFitWidth(20);
            attachIMG.setPreserveRatio(true);
        attachFileBTN.setGraphic(attachIMG);
        attachFileBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {

                    fileAttached = true;
                    FileChooser fileChooser = new FileChooser();
                    attachedFile = fileChooser.showOpenDialog(window);

                    attachedFileName = attachedFile.getName().replace(" ","");

                    try {
                        FileInputStream fin = new FileInputStream(attachedFile);
                        byte[] bytes = new byte[(int) attachedFile.length()];
                        fin.read(bytes);

                        File outputFile = new File(UploadedFilesPath + attachedFileName);

                        FileOutputStream fw = new FileOutputStream(outputFile);
                        fw.write(bytes);
                        fw.flush();
                        fw.close();


                    }
                    catch (IOException e)
                    {
                        System.out.println("failed");
                    }



                    fileLabel.setText("Attached: " + attachedFileName);

                } catch (Exception ignored) {

                }
            }
        });

        //post button
        Button postBTN = new Button("Publish Message");
        postBTN.setFont(new Font(14));
        postBTN.setDefaultButton(true);
        postBTN.setPrefHeight(20);
        postBTN.setPrefWidth(200);
        postBTN.setLayoutX(30);
        postBTN.setLayoutY(430);
        postBTN.setStyle("-fx-background-color: ddd");
        postBTN.setEffect(ds);
            ImageView postIMG = new ImageView("file:"+ImagesPath+"telegram.png");
            postIMG.setFitHeight(20);
            postIMG.setFitWidth(20);
            postIMG.setPreserveRatio(true);
        postBTN.setGraphic(postIMG);


        postBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button2Click) {
                try {
                    if(!fileAttached)
                    {
                        String messageToSend = messageTA.getText();
                        messageToSend = messageToSend.replace("\n", "[newline]");
                        PublishMessage(messageToSend);
                        visibleScene = HomeScene();
                        UpdateScene(visibleScene);
                    }
                    else
                    {
                        String messageToSend = messageTA.getText();
                        messageToSend = messageToSend.replace("\n", "[newline]");
                        messageToSend += " pic attached " + attachedFileName;
                        System.out.println(messageToSend);
                        PublishMessage(messageToSend);
                        visibleScene = HomeScene();
                        UpdateScene(visibleScene);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //</editor-fold>

        Label searchLabel = new Label("Search in messages: ");
        searchLabel.setLayoutX(300);
        searchLabel.setLayoutY(95);


        TextField searchField = new TextField("");
        searchField.setLayoutX(searchLabel.getLayoutX()+130);
        searchField.setLayoutY(90);

        searchField.setOnAction(actionEvent -> {
            try {
                searchTag = searchField.getText();
                UpdateScrollPane();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } );


        createService();

        UpdateScrollPane();

        mainPane.getChildren().addAll(navBar,infoPane,messageTA,fileLabel,attachFileBTN,postBTN,searchLabel,searchField,scroll);
        return new Scene(mainPane ,720,480);
    }


    String searchTag = "";
    ScrollPane scroll = new ScrollPane();
    public void UpdateScrollPane() throws Exception {
        scroll.setPrefHeight(320);
        scroll.setPrefWidth(430);
        scroll.setLayoutX(275);
        scroll.setLayoutY(140);

        VBox messagesPane = new VBox();
        messagesPane.setSpacing(10);
        messagesPane.setFillWidth(true);
        messagesPane.setPadding(new Insets( 5,5,5,5));

        Vector<Message> messageList = client.getMessages();
        for (int i = 0;i<messageList.size();i++)//loop through all messages
        {

            if(searchTag.equals("") || searchTag.equals(""))
            {
                messagesPane.getChildren().add(AddMessage(messageList.get(i)));
            }
            else if( !searchTag.equals("") && messageList.get(i).getBody().contains(searchTag))
            {
                messagesPane.getChildren().add(AddMessage(messageList.get(i)));

            }
        }

        scroll.setContent(messagesPane);
        scroll.setFitToWidth(true);



    }



    public void createService()
    {

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {

                        if(mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING)
                        {
                            //don't update if video is play or it will reset it
                            System.out.println("Video Playing");

                        }
                        else
                        {
                            System.out.println("Refreshing messages");
                            UpdateScrollPane();
                        }

                    } catch (Exception ignored) {

                    }
                });
            }
        }, 0, 5000);




    }

    MediaView mv = null;
    MediaPlayer mp = null;
    public VBox AddMessage(Message message) throws Exception
    {
        VBox box = new VBox();
        Pane infobox = new Pane();
        VBox messagebox = new VBox();

        infobox.setPrefWidth(410);

        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: ddd");
        box.setSpacing(10);

        Label forMessageLBL = new Label("Message: ");
        String messageBody = message.getBody().replace("[newline]","\n");
        Label messageLBL = new Label(messageBody);
        Label fromLBL = new Label("From: "+message.getFrom());
        Label whenLBL = new Label("When: "+message.getWhen());




        messageLBL.setPadding(new Insets( 5,5,5,5));
        messageLBL.setPrefWidth(400);
        messageLBL.setStyle("-fx-background-radius: 5;-fx-background-color: eee");

        fromLBL.setPadding(new Insets( 5,5,5,5));

        whenLBL.setLayoutX(315);
        whenLBL.setPadding(new Insets( 5,5,5,5));

        ImageView msgImage = new ImageView();
        HBox attachmentControls = new HBox();
        attachmentControls.setSpacing(10);
        attachmentControls.setAlignment(Pos.CENTER);
        attachmentControls.setPadding(new Insets(5,5,5,5));

        Button playBTN = new Button("Play/Pause");
        Button downloadBTN = new Button("Download File");



        String attachment = "";
        if(message.getPic() != null)
        {
            String fileName = message.getPic();
            File receivedFile = new File(UploadedFilesPath+fileName);
            String fileExt = fileName.substring(fileName.lastIndexOf("."));



            if(fileExt.equals(".png") || fileExt.equals(".jpg") || fileExt.equals(".jpeg"))
            {
                //file is an image
                Image image = new Image(new FileInputStream(receivedFile));
                msgImage.setImage(image);
                if(image.getWidth()>image.getHeight())//is landscpae
                {
                    msgImage.setFitWidth(380);
                }
                else//is portrait
                {
                    msgImage.setFitHeight(200);
                }

                msgImage.setPreserveRatio(true);
                attachment = "image";
            }
            else if(fileExt.equals(".mp4") || fileExt.equals(".mov"))
            {
                attachment = "video";
                Media video = new Media(receivedFile.toURI().toString());
                mp = new MediaPlayer(video);
                mv = new MediaView(mp);

                if(video.getWidth()>video.getHeight())//is landscpae
                {
                    mv.setFitWidth(380);
                }
                else//is portrait
                {
                    mv.setFitHeight(200);
                }

                mv.setPreserveRatio(true);
                playBTN.setOnAction(actionEvent ->
                {
                    if(mp.getStatus() == MediaPlayer.Status.PLAYING)
                    {
                        mp.pause();
                    }
                    else
                    {
                        mp.play();
                    }
                });
                attachmentControls.getChildren().addAll(playBTN);

            }
            else
            {
                attachment = "file";
                Label fileLabel = new Label(fileName);
                attachmentControls.getChildren().addAll(fileLabel);
            }


            downloadBTN.setOnAction(actionEvent ->
            {
                FileChooser fileSave = new FileChooser();
                fileSave.setTitle("Save");
                fileSave.setInitialFileName(receivedFile.getName());
                fileSave.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
                File dest = fileSave.showSaveDialog(window);
                if (dest != null)
                {
                    try
                    {
                        Files.copy(receivedFile.toPath(), dest.toPath());
                    } catch (IOException ignored)
                    {

                    }
                }

            });

            attachmentControls.getChildren().addAll(downloadBTN);
        }
        else// no file
        {

        }


        box.setPadding(new Insets( 5,5,0,5));
        infobox.setPadding(new Insets( 0,0,0,0));
        messagebox.setPadding(new Insets( 0,5,5,5));

        infobox.getChildren().addAll(fromLBL,whenLBL);
        switch (attachment)
        {
            case "image":
                messagebox.getChildren().addAll(forMessageLBL,messageLBL,attachmentControls,msgImage);
                break;
            case "video":
                messagebox.getChildren().addAll(forMessageLBL,messageLBL,attachmentControls,mv);
                break;
            case "file":
                messagebox.getChildren().addAll(forMessageLBL,messageLBL,attachmentControls);
                break;
            default:
                messagebox.getChildren().addAll(forMessageLBL,messageLBL);
                break;

        }

        box.getChildren().addAll(infobox,messagebox);



        return box;
    }

    public void PublishMessage(String message) throws Exception
    {
        client.executeCommand("p "+message);
    }

    public Scene ChannelsScene() throws Exception
    {

        //<editor-fold desc="Base Template">
        //pane

        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: white");

        //HBox
        HBox navBar = new HBox();
        navBar.setPrefHeight(60);
        navBar.setPrefWidth(720);
        navBar.setStyle("-fx-background-color: edf8ff");


        //button1
        Button homeBTN = new Button("Home");
        homeBTN.setPrefWidth(240);
        homeBTN.setPrefHeight(60);
        homeBTN.setStyle("-fx-background-color: edf8ff");
        //button image
        ImageView homeIMG = new ImageView("file:"+ImagesPath+"house.png");
        homeIMG.setFitHeight(30);
        homeIMG.setFitWidth(30);
        homeIMG.setPreserveRatio(true);
        homeBTN.setGraphic(homeIMG);
        homeBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = HomeScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //button2
        Button channelsBTN = new Button("Channels");
        channelsBTN.setPrefWidth(240);
        channelsBTN.setPrefHeight(60);
        channelsBTN.setStyle("-fx-background-color: edf8ff");
        //button image
        ImageView channelsIMG = new ImageView("file:"+ImagesPath+"friends.png");
        channelsIMG.setFitHeight(30);
        channelsIMG.setFitWidth(30);
        channelsIMG.setPreserveRatio(true);
        channelsBTN.setGraphic(channelsIMG);
        channelsBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = ChannelsScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //button3
        Button logoutBTN = new Button(" Logout");
        logoutBTN.setPrefWidth(240);
        logoutBTN.setPrefHeight(60);
        logoutBTN.setStyle("-fx-background-color: edf8ff");
        //button image
        ImageView logoutIMG = new ImageView("file:"+ImagesPath+"exit.png");
        logoutIMG.setFitHeight(30);
        logoutIMG.setFitWidth(30);
        logoutIMG.setPreserveRatio(true);
        logoutBTN.setGraphic(logoutIMG);
        logoutBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = LoginScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //info pane
        AnchorPane infoPane = new AnchorPane();
        infoPane.setPrefHeight(25);
        infoPane.setPrefWidth(720);
        //infoPane.setStyle("-fx-background-color: ddd");
        infoPane.setLayoutY(60);

        //label
        Label usernameLBL = new Label("Logged in as "+currentUsername);
        usernameLBL.setFont(new Font(16));

        //shape
        tabUnderline.setFill(Color.color(0.0,0.278,0.467));//000,071,119 //#004777
        tabUnderline.setLayoutX(240);

        infoPane.getChildren().addAll(tabUnderline,usernameLBL);

        navBar.getChildren().addAll(homeBTN,channelsBTN,logoutBTN);


        //</editor-fold>


        //<editor-fold desc="Subscribe">

        VBox subscribeBox = new VBox();
        subscribeBox.setAlignment(Pos.CENTER);
        subscribeBox.setFillWidth(false);
        subscribeBox.setPrefHeight(360);
        subscribeBox.setPrefWidth(250);
        subscribeBox.setLayoutX(15);
        subscribeBox.setLayoutY(100);
        subscribeBox.setSpacing(10);

        Label channelNameLBL = new Label("Enter Channel Name:");
        channelNameLBL.setFont(new Font(16));

        TextField channelNameTF = new TextField();
        channelNameTF.setFont(new Font(14));
        channelNameTF.setPrefWidth(180);

        //button
        Button subscribeBTN = new Button("Subscribe to Channel");
        subscribeBTN.setPrefHeight(50);
        subscribeBTN.setPrefWidth(200);
        subscribeBTN.setStyle("-fx-background-color: ddd");
        subscribeBTN.setDefaultButton(true);
        DropShadow ds = new DropShadow();
        ds.setRadius(10);
        subscribeBTN.setEffect(ds);
        ImageView subscribeIMG = new ImageView("file:"+ImagesPath+"add-friend.png");
        subscribeIMG.setFitHeight(20);
        subscribeIMG.setFitWidth(20);
        subscribeIMG.setPreserveRatio(true);
        subscribeBTN.setGraphic(subscribeIMG);
        subscribeBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    String channelToSub = channelNameTF.getText();

                    client.executeCommand("s "+channelToSub);
                    visibleScene = ChannelsScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        subscribeBox.getChildren().addAll(channelNameLBL,channelNameTF,subscribeBTN);

        //</editor-fold>


        //<editor-fold desc="Channels List">

        Vector<String> channelsList = client.getChannels(currentUsername);
        //System.out.println(channelsList.toString());

        ScrollPane scroll = new ScrollPane();
        scroll.setPrefHeight(360);
        scroll.setPrefWidth(430);
        scroll.setLayoutX(270);
        scroll.setLayoutY(100);

        VBox channelsPane = new VBox();
        channelsPane.setSpacing(10);
        channelsPane.setFillWidth(true);
        channelsPane.setPadding(new Insets( 5,5,5,5));




        for (int i = 0;i<channelsList.size();i++)//loop through all messages
        {

            if(channelsList.get(i).equals(currentUsername))
            {
                System.out.println("channel is current user");


            }
            else if(channelsList.get(i) != "")
            {
                channelsPane.getChildren().add(AddChannel(channelsList.get(i)));

            }


        }

        scroll.setContent(channelsPane);
        scroll.setFitToWidth(true);

        //</editor-fold>

        pane.getChildren().addAll(navBar,infoPane,subscribeBox,scroll);
        return new Scene(pane ,720,480);
    }

    public VBox AddChannel(String channelName) throws Exception
    {
        VBox box = new VBox();


        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: ddd");
        box.setSpacing(10);


        Label channelLBL = new Label("Channel Name: "+channelName);
        channelLBL.setFont(new Font(16));

        Button unsubscribeBTN = new Button("Unsubscribe from channel");
        unsubscribeBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    String channelToSub = channelName;

                    client.executeCommand("u "+channelName);
                    visibleScene = ChannelsScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        box.setPadding(new Insets( 5,5,5,5));

        box.getChildren().addAll(channelLBL,unsubscribeBTN);



        return box;
    }


    public Scene TemplateScene() throws Exception
    {

        //<editor-fold desc="Base Template">
        //pane

        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: white");

        //HBox
        HBox navBar = new HBox();
        navBar.setPrefHeight(60);
        navBar.setPrefWidth(720);
        navBar.setStyle("-fx-background-color: edf8ff");


        //button1
        Button homeBTN = new Button("Home");
        homeBTN.setPrefWidth(240);
        homeBTN.setPrefHeight(60);
        homeBTN.setStyle("-fx-background-color: edf8ff");
            //button image
            ImageView homeIMG = new ImageView("file:"+ImagesPath+"house.png");
            homeIMG.setFitHeight(30);
            homeIMG.setFitWidth(30);
            homeIMG.setPreserveRatio(true);
        homeBTN.setGraphic(homeIMG);
        homeBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = HomeScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //button2
        Button channelsBTN = new Button("Channels");
        channelsBTN.setPrefWidth(240);
        channelsBTN.setPrefHeight(60);
        channelsBTN.setStyle("-fx-background-color: edf8ff");
            //button image
            ImageView channelsIMG = new ImageView("file:"+ImagesPath+"friends.png");
            channelsIMG.setFitHeight(30);
            channelsIMG.setFitWidth(30);
            channelsIMG.setPreserveRatio(true);
        channelsBTN.setGraphic(channelsIMG);
        channelsBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = ChannelsScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //button3
        Button logoutBTN = new Button(" Logout");
        logoutBTN.setPrefWidth(240);
        logoutBTN.setPrefHeight(60);
        logoutBTN.setStyle("-fx-background-color: edf8ff");
            //button image
            ImageView logoutIMG = new ImageView("file:"+ImagesPath+"exit.png");
            logoutIMG.setFitHeight(30);
            logoutIMG.setFitWidth(30);
            logoutIMG.setPreserveRatio(true);
        logoutBTN.setGraphic(logoutIMG);
        logoutBTN.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    visibleScene = LoginScene();
                    UpdateScene(visibleScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //info pane
        AnchorPane infoPane = new AnchorPane();
        infoPane.setPrefHeight(25);
        infoPane.setPrefWidth(720);
        infoPane.setStyle("-fx-background-color: ddd");
        infoPane.setLayoutY(60);

        //label
        Label usernameLBL = new Label("Hello "+currentUsername);
        usernameLBL.setFont(new Font(12));

        //shape
        tabUnderline.setFill(Color.color(0.0,0.278,0.467));
        tabUnderline.setLayoutX(0);

        infoPane.getChildren().addAll(tabUnderline,usernameLBL);

        navBar.getChildren().addAll(homeBTN,channelsBTN,logoutBTN);


        //</editor-fold>


        pane.getChildren().addAll(navBar,infoPane);
        return new Scene(pane ,720,480);
    }

    public Scene Alert(String message) throws Exception
    {
        //pane
        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: white");

        //vbox
        VBox layout = new VBox();
        layout.setPrefHeight(200);
        layout.setPrefWidth(300);
        layout.setStyle("-fx-background-color: white");
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);
        layout.setFillWidth(false);


        //alert label
        Label alert = new Label(message);
        alert.setWrapText(true);
        alert.setFont(new Font(20));

        //login button
        Button btn = new Button("OK");
        btn.setFont(new Font(20));
        btn.setDefaultButton(true);
        btn.setPrefHeight(50);
        btn.setPrefWidth(150);
        btn.setStyle("-fx-background-color: ddd");
        DropShadow ds = new DropShadow();
        ds.setRadius(10);
        btn.setEffect(ds);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent button1Click) {
                try {
                    System.exit(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        layout.getChildren().addAll(alert,btn);

        pane.getChildren().addAll(layout);


        return new Scene(pane ,300,200);

    }

}
