/**
 * ---- Main GUI class for MSNBC Threading project ----
 *
 * This class contains the GUI for the project, it incorporates a loading screen while
 * the data from the msnbc file is loaded into the 2d array used to store the data. Then
 * a home screen is brought up which allows the user to select a query to perform on the
 * data set. Each query has it's own scene that will be used to get the user's answer and
 * present it to them.
 *
 * @author Preston Mackert
 * @author Robert Bofinger
 *
 * *** created code for the loading screen based from github user jewelsea
 *      url: https://gist.github.com/jewelsea/2305098
 *
 */

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;

public class GUI extends Application {

    /**
     * The class that contains all of the GUI components, extends Application which
     * allows the program to run, it is the way in which everything will wind up
     * fitting together and looking nice as an end product
     */

    // ---------------------------------------- global variables ---------------------------------------------------- //

    private static Data data;                 // the loaded msnbc data
    private static final int users = 989818;  // the total amount of users in the file
    private static final int categories = 17; // the total amount of categories

    // these are the images used for the loading screen and then also the app icon

    public static final String APPLICATION_ICON =
            "http://static.tvtropes.org/pmwiki/pub/images/new-msnbc-logo-781050.gif";
    public static final String SPLASH_IMAGE =
            "http://static1.squarespace.com/static/5176f0fde4b0c6cbe95767cc/t/555cfe7ee4b089fc801515f6/1432157825036/MSNBC_Logo_Small.png?format=300w";
    public static final String MAIN_LOGO =
            "http://static1.squarespace.com/static/5176f0fde4b0c6cbe95767cc/t/555cfe7ee4b089fc801515f6/1432157825036/MSNBC_Logo_Small.png";

    private Pane splashLayout;                 // to set up the loading screen appearance
    private ProgressBar loadProgress;          // need this to show a loading bar
    private Label progressText;                // give text to the user to see what is being loaded
    private Stage mainStage;
    private Scene scene1, scene2, scene3, scene4, scene5, scene6;
    private static final int SPLASH_WIDTH = 600;
    private static final int SPLASH_HEIGHT = 400;

    // ---------------------------------------- run application ----------------------------------------------------- //

    public static void main(String[] args) throws Exception {launch(args);}  // this is what launches the app itself

    // ---------------------------------------- support methods ----------------------------------------------------- //

    @Override
    public void init() {
        /**
         * method that sets up and starts the loading screen, packs the images and the progress bar
         * into the splash layout then sets up the way the loading will appear
         */
        ImageView splash = new ImageView(new Image(SPLASH_IMAGE));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(400);
        progressText = new Label("importing data from msnbc . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-padding: 5; " + "-fx-background-color: white; " + "-fx-border-width:2; " +
                "-fx-border-color: " + "linear-gradient(" + "to bottom, " + "white, " + "#6666ff" + ");");
        splashLayout.setEffect(new DropShadow());
    }

    public interface InitCompletionHandler {
        void complete(); // needed to have this for changing from loading screen to main
    }

    private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
        /**
         * method to get the splash screen to show, uses the initStage, a task (the thread that will load in the
         * current progress of data), and the init's handler.
         */
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();
                initCompletionHandler.complete();
            }
        });
        // setting up the scene and display options
        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();
    }

    private void showMainStage() {
        /**
         * The load screen is complete and now inside the main application with the data all loaded into our
         * array. From here the user will be able to select a query and progress forward through the program.
         * Each of the five queries are given their own scene in the GUI which allows it to stay looking 100
         */

        mainStage = new Stage();
        mainStage.setTitle("msnbc data queries");


        ObservableList<String> options =					// creating options for the queries
                FXCollections.observableArrayList(
                        "Are there more than ____ users who looked at X",
                        "What percent of users looked at X",
                        "Are there more users who looked at X than Y",
                        "How many users viewed X ___ number of times",
                        "What percent of users looked at X more than Y"
                );

        ComboBox querySel = new ComboBox();					// drop down to select query
        querySel.setItems(options);
        querySel.setPromptText("select a query...");

        Button search = new Button("go");				   // button that initiates the query
        search.setMaxWidth(100);
        search.setMinWidth(100);
        search.setOnAction(e -> {
            String selection = querySel.getSelectionModel().getSelectedItem().toString();
            if (selection.equals("Are there more than ____ users who looked at X")){
                mainStage.setScene(scene2);
            }
            else if (selection.equals("What percent of users looked at X")){
                mainStage.setScene(scene3);
            }
            else if (selection.equals("Are there more users who looked at X than Y")){
                mainStage.setScene(scene4);
            }
            else if (selection.equals("How many users viewed X ___ number of times")){
                mainStage.setScene(scene5);
            }
            else if (selection.equals("What percent of users looked at X more than Y")){
                mainStage.setScene(scene6);
            }
        });

        // ----------------- main screen ------------------ //

        ImageView logo = new ImageView(new Image(MAIN_LOGO));

        TilePane selQuery = new TilePane(Orientation.HORIZONTAL);		// make a pane to make GUI look sexy
        selQuery.getChildren().addAll(querySel, search);
        selQuery.setAlignment(Pos.CENTER);

        HBox select = new HBox(8);
        select.getChildren().addAll(querySel, search);
        select.setAlignment(Pos.CENTER);

        VBox layout1 = new VBox(40);									// pack together the panes and header
        layout1.getChildren().addAll(logo, select);
        layout1.setAlignment(Pos.CENTER);

        scene1 = new Scene(layout1, 900, 600);

        // ----------------- query1 screen ----------------- //

        String[] catNames = {"front page", "news", "tech", "local", "opinion", "on-air", "misc", "weather", "msn-news",
                "health", "living", "business", "msn-sports", "sports", "summary", "bbs", "travel"};

        // this array is used to determine the column in the 2d array we are going to use. The index is the column that
        // gets associated with the string that is the name of the category. Used because you can't index an observable.

        ObservableList<String> categories =					          // creating options for the queries
                FXCollections.observableArrayList(
                        "front page",
                        "news",
                        "tech",
                        "local",
                        "opinion",
                        "on-air",
                        "misc",
                        "weather",
                        "msn-news",
                        "health",
                        "living",
                        "business",
                        "msn-sports",
                        "sports",
                        "summary",
                        "bbs",
                        "travel"
                );

        ComboBox catSel = new ComboBox();					                  // drop down to select query
        catSel.setItems(categories);
        catSel.setPromptText("select a category...");

        TextField numViews = new TextField();                                 // grabs user input
        numViews.setPromptText("views...");
        numViews.setMaxWidth(100);

        Label instruct = new Label("Type in a number of views and then select a category to answer the query");

        Label resultText = new Label();                                      // stores the answer for query 1

        Button returnMain1 = new Button("return");
        returnMain1.setOnAction(e -> {mainStage.setScene(scene1);});

        Button answer = new Button("go");                                   // does the work to answer the query
        answer.setOnAction(e -> {
            String cat = catSel.getSelectionModel().getSelectedItem().toString();
            int col = -1;
            for (int i=0;i<catNames.length;i++) {
                if (catNames[i].equals(cat)) {
                    col = i;
                    break;
                }
            }

            int numberViews = 0;                                            // the threshold

            try{
                numberViews = Integer.parseInt(numViews.getCharacters().toString());
            } catch (NumberFormatException k){
                resultText.setText("enter a valid number for views");
            }

            if (col >= 0){                                                 // if valid category
                boolean answerQ = data.countQuery(numberViews, col);       // compares views vs threshold
                if (answerQ){
                    resultText.setText("There were more than " + numberViews + " users who looked at " + cat);
                } else {
                    resultText.setText("There were fewer than " + numberViews + " users who looked at " + cat);
                }
            } else {
                resultText.setText("The category selected was not registered");
            }
        });

        HBox inputs = new HBox(10);
        inputs.getChildren().addAll(numViews, catSel);
        inputs.setAlignment(Pos.CENTER);

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(returnMain1, answer);
        buttons.setAlignment(Pos.CENTER);

        VBox layout2 = new VBox(70);
        layout2.getChildren().addAll(instruct, inputs, resultText, buttons);
        layout2.setAlignment(Pos.CENTER);

        scene2 = new Scene(layout2, 900, 600);

        // ----------------- query2 screen ----------------- //

        Label instruct2 = new Label("Select a category from the dropdown to answer the query");

        ComboBox catSel2 = new ComboBox();
        catSel2.setItems(categories);
        catSel2.setPromptText("select a category...");

        Label resultText2 = new Label();

        Button answer2 = new Button("go");
        answer2.setOnAction(e -> {
            String cat = catSel2.getSelectionModel().getSelectedItem().toString();
            int col = -1;
            for (int i=0;i<catNames.length;i++) {
                if (catNames[i].equals(cat)) {
                    col = i;
                    break;
                }
            }
            if (col >= 0){
                String percentage = String.format("%.6f", data.percentageCountQuery(col));
                resultText2.setText(cat + " had " + percentage +"% of user's view it.");
            } else {
                resultText2.setText("You have not selected a category");
            }

        });

        Button returnMain2 = new Button("return");
        returnMain2.setOnAction(e -> {mainStage.setScene(scene1);});

        HBox buttons2 = new HBox(10);
        buttons2.getChildren().addAll(returnMain2, answer2);
        buttons2.setAlignment(Pos.CENTER);

        VBox layout3 = new VBox(70);
        layout3.getChildren().addAll(instruct2, catSel2, resultText2, buttons2);
        layout3.setAlignment(Pos.CENTER);

        scene3 = new Scene(layout3, 900, 600);

        // ----------------- query3 screen ----------------- //

        Label instruct3 = new Label("Select two categories from the dropdown bars to answer the query");

        ComboBox catSel3 = new ComboBox();
        catSel3.setItems(categories);
        catSel3.setPromptText("select a category...");

        ComboBox catSel_3 = new ComboBox();
        catSel_3.setItems(categories);
        catSel_3.setPromptText("select a category...");

        Label resultText3 = new Label();

        Button answer3 = new Button("go");
        answer3.setOnAction(e -> {
            String cat1 = catSel3.getSelectionModel().getSelectedItem().toString();
            String cat2 = catSel_3.getSelectionModel().getSelectedItem().toString();

            int col1 = -1;
            for (int i = 0; i < catNames.length; i++) {
                if (catNames[i].equals(cat1)) {
                    col1 = i;
                    break;
                }
            }

            int col2 = -1;
            for (int i = 0; i < catNames.length; i++) {
                if (catNames[i].equals(cat2)) {
                    col2 = i;
                    break;
                }
            }

            if (col1 >= 0 && col2 >=0){
                boolean ans = data.comparisonQuery(col1, col2);
                if (ans) {
                    resultText3.setText(cat1 + " had more viewers than " + cat2);
                } else{
                    resultText3.setText(cat2 + " had more viewers than " + cat1);
                }
            } else {
                resultText3.setText("One or both categories not selected");
            }
        });

        Button returnMain3 = new Button("return");
        returnMain3.setOnAction(e -> {mainStage.setScene(scene1);});

        HBox dropdowns = new HBox(10);
        dropdowns.getChildren().addAll(catSel3, catSel_3);
        dropdowns.setAlignment(Pos.CENTER);

        HBox buttons3 = new HBox(10);
        buttons3.getChildren().addAll(returnMain3, answer3);
        buttons3.setAlignment(Pos.CENTER);

        VBox layout4 = new VBox(70);
        layout4.getChildren().addAll(instruct3, dropdowns, resultText3, buttons3);
        layout4.setAlignment(Pos.CENTER);

        scene4 = new Scene(layout4, 900, 600);

        // ----------------- query4 screen ----------------- //

        Label instruct4 = new Label("Type in a number of views and then select a category to answer the query");

        TextField numViews4 = new TextField();
        numViews4.setPromptText("views...");
        numViews4.setMaxWidth(100);

        ComboBox catSel4 = new ComboBox();
        catSel4.setItems(categories);
        catSel4.setPromptText("select a category...");

        Label resultText4 = new Label();

        Button returnMain4 = new Button("return");
        returnMain4.setOnAction(e -> {mainStage.setScene(scene1);});

        Button answer4 = new Button("go");
        answer4.setOnAction(e -> {
            String cat = catSel4.getSelectionModel().getSelectedItem().toString();
            int col = -1;
            for (int i=0;i<catNames.length;i++) {
                if (catNames[i].equals(cat)) {
                    col = i;
                    break;
                }
            }

            int threshold = 0;

            try{
                threshold = Integer.parseInt(numViews4.getCharacters().toString());
            } catch (NumberFormatException k){
                resultText4.setText("enter a valid number for a threshold");
            }

            if (col >= 0){
                int count = data.countThresholdQuery(threshold, col);
                if (count > threshold){
                    resultText4.setText(cat + " has more than " + threshold + " views.");
                } else {
                    resultText4.setText(cat + " has less than " + threshold + " views.");
                }
            } else {
                resultText4.setText("you did not select a category");
            }

        });

        HBox inputs4 = new HBox(10);
        inputs4.getChildren().addAll(numViews4, catSel4);
        inputs4.setAlignment(Pos.CENTER);

        HBox buttons4 = new HBox(10);
        buttons4.getChildren().addAll(returnMain4, answer4);
        buttons4.setAlignment(Pos.CENTER);

        VBox layout5 = new VBox(70);
        layout5.getChildren().addAll(instruct4, inputs4, resultText4, buttons4);
        layout5.setAlignment(Pos.CENTER);

        scene5 = new Scene(layout5, 900, 600);

        // ----------------- query5 screen ----------------- //

        Label instruct5 = new Label("Select two categories from the dropdown bars to answer the query");

        ComboBox catSel5 = new ComboBox();
        catSel5.setItems(categories);
        catSel5.setPromptText("select a category...");

        ComboBox catSel_5 = new ComboBox();
        catSel_5.setItems(categories);
        catSel_5.setPromptText("select a category...");

        Label resultText5 = new Label();

        Button answer5 = new Button("go");
        answer5.setOnAction(e -> {
            String cat1 = catSel5.getSelectionModel().getSelectedItem().toString();
            int col1 = -1;
            for (int i=0;i<catNames.length;i++) {
                if (catNames[i].equals(cat1)) {
                    col1 = i;
                    break;
                }
            }

            String cat2 = catSel_5.getSelectionModel().getSelectedItem().toString();
            int col2 = -1;
            for (int i=0;i<catNames.length;i++) {
                if (catNames[i].equals(cat2)) {
                    col2 = i;
                    break;
                }
            }

            if (col1 >= 0 && col2 >=0){
                float percAns = data.comparePercentageQuery(col1, col2);
                String percentage = String.format("%.6f", percAns);
                boolean ans = data.comparisonQuery(col1, col2);
                if (ans) {
                    resultText5.setText(cat1 + " had more viewers than " + cat2 + " by " + percentage + "% more viewers");
                } else{
                    resultText5.setText(cat2 + " had more viewers than " + cat1 + " by " + percentage + "% more viewers");
                }
            } else {
                resultText5.setText("One or both categories not selected");
            }
        });

        Button returnMain5 = new Button("return");
        returnMain5.setOnAction(e -> {mainStage.setScene(scene1);});

        HBox dropdowns5 = new HBox(10);
        dropdowns5.getChildren().addAll(catSel5, catSel_5);
        dropdowns5.setAlignment(Pos.CENTER);

        HBox buttons5 = new HBox(10);
        buttons5.getChildren().addAll(returnMain5, answer5);
        buttons5.setAlignment(Pos.CENTER);

        VBox layout6 = new VBox(70);
        layout6.getChildren().addAll(instruct5, dropdowns5, resultText5, buttons5);
        layout6.setAlignment(Pos.CENTER);

        scene6 = new Scene(layout6, 900, 600);

        // ---------------- set first scene --------------- //

        mainStage.getIcons().add(new Image(APPLICATION_ICON));
        mainStage.setScene(scene1);
        mainStage.show();
    }

    @Override
    public void start(final Stage initStage) throws Exception {
        /**
         * the start method is what makes the application go, it is like the main portion of the program for
         * javaFx, the actual data file will be loaded here and the program will work for what we need it to do.
         */
        final Thread loadThread = new Thread() {                         // anonymous thread to load data
            public void run() {
                try {
                    File dataFile = new File("datafile.txt");
                    data.loadData(dataFile);
                }
                catch (IOException e){}                                   // do nothing
            }
        };

        final Task<Integer> progressbarTask = new Task<Integer>() {
            public Integer call() {
                while(data.getUsersProcessed() < data.getTotalUsers()){
                    updateProgress(data.getUsersProcessed(), data.getTotalUsers());
                    updateMessage("importing data from msnbc . . .");
                }
                return data.getTotalUsers();
            }
        };
        data = new Data(users, categories);
        showSplash(initStage, progressbarTask, () -> showMainStage());
        loadThread.start();                                              // progressbarThread.start();
        new Thread(progressbarTask).start();
    }
}