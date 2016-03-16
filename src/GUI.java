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

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

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
                "-fx-border-color: " + "linear-gradient(" + "to bottom, " + "blue, " + "#6666ff" + ");");
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

        Button search = new Button("go");				// button that initiates the query
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

        Button returnMain1 = new Button("return");
        returnMain1.setOnAction(e -> {mainStage.setScene(scene1);});

        VBox layout2 = new VBox();
        layout2.getChildren().addAll(returnMain1);
        layout2.setAlignment(Pos.CENTER);

        scene2 = new Scene(layout2, 900, 600);

        // ----------------- query2 screen ----------------- //

        Button returnMain2 = new Button("return");
        returnMain2.setOnAction(e -> {mainStage.setScene(scene1);});

        VBox layout3 = new VBox();
        layout3.getChildren().add(returnMain2);
        layout3.setAlignment(Pos.CENTER);

        scene3 = new Scene(layout3, 900, 600);

        // ----------------- query3 screen ----------------- //

        Button returnMain3 = new Button("return");
        returnMain3.setOnAction(e -> {mainStage.setScene(scene1);});

        VBox layout4 = new VBox();
        layout4.getChildren().add(returnMain3);
        layout4.setAlignment(Pos.CENTER);

        scene4 = new Scene(layout4, 900, 600);

        // ----------------- query4 screen ----------------- //

        Button returnMain4 = new Button("return");
        returnMain4.setOnAction(e -> {mainStage.setScene(scene1);});

        VBox layout5 = new VBox();
        layout5.getChildren().add(returnMain4);
        layout5.setAlignment(Pos.CENTER);

        scene5 = new Scene(layout5, 900, 600);

        // ----------------- query5 screen ----------------- //

        Button returnMain5 = new Button("return");
        returnMain5.setOnAction(e -> {mainStage.setScene(scene1);});

        VBox layout6 = new VBox();
        layout6.getChildren().add(returnMain5);
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
                catch (FileNotFoundException e){}                       // do nothing
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
        loadThread.start();                                             // progressbarThread.start();
        new Thread(progressbarTask).start();
    }
}