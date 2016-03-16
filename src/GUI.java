
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Splash page for loading in the msnbc data
 */

public class GUI extends Application {

    private static Data data; // the loaded msnbc data
    private static final int users = 989818; // the total amount of users in the file
    private static final int categories = 17; // the total amount of categories

    public static final String APPLICATION_ICON =
            "http://static.tvtropes.org/pmwiki/pub/images/new-msnbc-logo-781050.gif";
    public static final String SPLASH_IMAGE =
            "http://www.msnbc.com/sites/msnbc/themes/leanforward/images/site-header/msnbc-logo-card-twitter.png";

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private static final int SPLASH_WIDTH = 600;
    private static final int SPLASH_HEIGHT = 400;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void init() {
        ImageView splash = new ImageView(new Image(
                SPLASH_IMAGE
        ));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH+210);
        progressText = new Label("importing data from msnbc . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-padding: 5; " + "-fx-background-color: white; " + "-fx-border-width:2; " + "-fx-border-color: " +
                "linear-gradient(" + "to bottom, " + "blue, " + "#6666ff" + ");");
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(final Stage initStage) throws Exception {

        final Thread loadThread = new Thread() { // anonymous thread to load data
            public void run() {
                try {
                    File dataFile = new File("datafile.txt");
                    data.loadData(dataFile);
                }
                catch (FileNotFoundException e){
                    // do nothing
                }
            }
        };

        final Task<Integer> progressbarTask = new Task<Integer>() {
            public Integer call() {
                while(data.getUsersProcessed() < data.getTotalUsers()){
                    updateProgress(data.getUsersProcessed(), data.getTotalUsers());
                }
                return data.getTotalUsers();
            }
        };
        data = new Data(users, categories);

        while(loadThread.isAlive()){
            System.out.println(data.getUsersProcessed() + " out of " + data.getTotalUsers());
        }


        /*final Task<ObservableList<String>> friendTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> foundFriends =
                        FXCollections.<String>observableArrayList();
                ObservableList<String> availableFriends =
                        FXCollections.observableArrayList(
                                "Fili", "Kili", "Oin", "Gloin", "Thorin",
                                "Dwalin", "Balin", "Bifur", "Bofur",
                                "Bombur", "Dori", "Nori", "Ori"
                        );

                for (int i = 0; i < availableFriends.size(); i++) {
                    Thread.sleep(400);
                    updateProgress(i + 5, availableFriends.size() + 5);
                    String nextFriend = availableFriends.get(i);
                    foundFriends.add(nextFriend);
                    updateMessage("Loading in user data...");
                }
                Thread.sleep(100);
                updateMessage("Data Retrieved");

                return foundFriends;
            }
        };*/

        showSplash(
                initStage,
                progressbarTask,
                () -> showMainStage()
        );

        loadThread.start();
        //progressbarThread.start();
        new Thread(progressbarTask).start();
    }
    private void showMainStage(
            //ReadOnlyObjectProperty<ObservableList<String>> friends
    ) {
        mainStage = new Stage(StageStyle.DECORATED);
        mainStage.setTitle("My Friends");
        mainStage.getIcons().add(new Image(
                APPLICATION_ICON
        ));

        final ListView<String> peopleView = new ListView<>();
        //peopleView.itemsProperty().bind(friends);

        mainStage.setScene(new Scene(peopleView));
        mainStage.show();
    }

    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler
    ) {
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
            } // todo add code to gracefully handle other task states.
        });

        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();
    }
    public interface InitCompletionHandler {
        public void complete();
    }
}