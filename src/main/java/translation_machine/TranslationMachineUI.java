package translation_machine;
import translation_machine.SmartLightController;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import javafx.scene.text.TextAlignment;
import javafx.application.Platform;
import javafx.scene.effect.Glow;
import javafx.scene.effect.*;
import javafx.geometry.Insets;

import genius.SongSearch;
import genius.SongSearch.Hit;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class TranslationMachineUI extends Application {

    private TranslateTransition activeSlide;

    private final double CARD_WIDTH = 700;
    private final double SPACING = 40;
 
    private TranslationMachine machine = new TranslationMachine();
    
    TextField songField1, artistField1;
    TextField songField2, artistField2;
    TextField songField3, artistField3;
    
    private Button translateButton;
    private HBox carousel;
    private StackPane viewport;
    private StackPane track;
    private List<Node> cards = new ArrayList<>();
    private double startx;
    private int currentIndex = 0;

    private boolean showingResults = false;

    private double startX;
    private long pressTime;

    private double dragStartX;
    private double dragTranslateStart;

    private double dragAnchorX;
    private double initialTranslateX;
    private double lockedViewportWidth;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        //SmartLightController.init();

        songField1 = new TextField();
        songField1.setPromptText("Enter: Your most recently played song");

        artistField1 = new TextField();
        artistField1.setPromptText("Artist");

        songField2 = new TextField();
        songField2.setPromptText("Enter: Your favorite song today");

        artistField2 = new TextField();
        artistField2.setPromptText("Artist");

        songField3 = new TextField();
        songField3.setPromptText("Enter: Your favorite song this week");

        artistField3 = new TextField();
        artistField3.setPromptText("Artist");

        translateButton = new Button("Translate Songs");
        translateButton.setOnAction(e -> {
            if (!showingResults) {
                handleTranslate();
                translateButton.setText("Back to Home");
                showingResults = true;
            } else {
                resetHome();
                translateButton.setText("Translate Songs");
                showingResults = false;
            }
        });

        carousel = new HBox();
        carousel.setSpacing(SPACING);
        carousel.setAlignment(Pos.CENTER_LEFT);
        carousel.setManaged(false);
        carousel.setMinWidth(Region.USE_PREF_SIZE);
        carousel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        carousel.setMaxWidth(Double.MAX_VALUE);
        //carousel.setTranslateX(0);
        //carousel.setTranslateY(0);
        //carousel.setLayoutY(80);

        track = new StackPane();      // 2
        track.getChildren().add(carousel);
        carousel.setLayoutY(140);
        track.setPrefHeight(650);
        track.setMinHeight(650);
        track.setMaxHeight(650);
        track.setPrefWidth(Region.USE_COMPUTED_SIZE);
        track.setMaxWidth(Double.MAX_VALUE);

        if (track == null || carousel == null) {
            throw new IllegalStateException("UI init failed: track or carousel is null");
        }
        
        viewport = new StackPane();
        //track.setAlignment(Pos.CENTER);
        viewport.getChildren().add(track);
        viewport.setPadding(new Insets(40, 0, 60, 0));
        viewport.setPrefHeight(900);
        viewport.setMinHeight(900);
        viewport.setMaxHeight(900);
        viewport.setAlignment(Pos.CENTER);
        viewport.setClip(null);

        StackPane.setAlignment(carousel, Pos.CENTER);
        StackPane.setAlignment(track, Pos.CENTER);
        carousel.setPadding(new Insets(30, 0, 70, 0));

        // IMPORTANT: clip EXACT viewport size (no +50, no offsets)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(viewport.widthProperty());
        clip.heightProperty().bind(viewport.heightProperty());

        viewport.setClip(clip);
        
        clip.setY(0);
        clip.setX(0);

        VBox inputs = new VBox(10,
                songField1, artistField1,
                songField2, artistField2,
                songField3, artistField3,
                translateButton
        );

        VBox.setVgrow(viewport, Priority.ALWAYS);

        inputs.setSpacing(10);
        inputs.setPadding(new Insets(20, 20, 10, 20));
        //inputs.setAlignment(Pos.TOP_CENTER);
        inputs.setMaxHeight(Region.USE_PREF_SIZE);

        BorderPane root = new BorderPane();

        root.setTop(inputs);
        //root.setFillWidth(true);
        BorderPane.setAlignment(inputs, Pos.CENTER);
        root.setCenter(viewport);
        BorderPane.setMargin(viewport, new Insets(20, 0, 0, 0));;

        //VBox.setMargin(inputs, new Insets(20, 0, 20, 0));

        //VBox root = new VBox();

        //root.getChildren().addAll(inputs, viewport);
        //VBox.setVgrow(viewport, Priority.ALWAYS);
        
        //root.setAlignment(Pos.TOP_CENTER);
        //root.setPadding(new Insets(0, 20, 10, 20));
        //root.setSpacing(30);

        Scene scene = new Scene(root, 1000, 900);

        primaryStage.setTitle("Synaesthesia");
        primaryStage.setScene(scene);
        primaryStage.show();

        //updateLights();

        Platform.runLater(() -> {
            viewport.applyCss();
            viewport.layout();
            animateCarousel();
        });

        enableSwipe();
    }

    private void handleTranslate() {

        carousel.getChildren().clear();

        processSong(songField1.getText(), artistField1.getText(), "Recently Played", 0);
        processSong(songField2.getText(), artistField2.getText(), "Today's Favorite", 1);
        processSong(songField3.getText(), artistField3.getText(), "This Week's Favorite", 2);

        System.out.println("TOTAL CARDS: " + carousel.getChildren().size());

        if (carousel.getChildren().size() > 1) {

            currentIndex = 0; // VERY IMPORTANT (real first card)
        }

        Platform.runLater(() -> {
            viewport.applyCss();
            viewport.layout();

            double width = viewport.getWidth(); // FORCE stabilization

            animateCarousel();
        });

        if (songField1.getText().isEmpty() &&
            songField2.getText().isEmpty() &&
            songField3.getText().isEmpty()) {

            SmartLightController.turnOffAll();
            return;
        }

        SmartLightController.setAllColors(
            (String[]) ((VBox) carousel.getChildren().get(currentIndex)).getUserData()
        );

        translateButton.setText("Back to Home");

        translateButton.setOnAction(e -> {
            carousel.getChildren().clear();

            songField1.clear();
            artistField1.clear();
            songField2.clear();
            artistField2.clear();
            songField3.clear();
            artistField3.clear();

            SmartLightController.turnOffAll();

            translateButton.setText("Translate Songs");
            translateButton.setOnAction(ev -> handleTranslate());
        });
    }

    private void processSong(String song, String artist, String labelText, int index) {

        System.out.println("PROCESSING: " + song + " - " + artist);

        SongResult result = null;

        try {
            result = machine.analyzeSong(song, artist);
        } catch (Exception e) {
            result = null;
        }

        VBox songPage = new VBox(20);
        songPage.setAlignment(Pos.BOTTOM_CENTER);
        songPage.setTranslateY(0);
        songPage.setPadding(new Insets(80, 20, 20, 20));
        songPage.setPrefWidth(CARD_WIDTH);
        songPage.setMinWidth(Region.USE_PREF_SIZE);
        songPage.setMaxWidth(Region.USE_PREF_SIZE);
        songPage.setMinHeight(580);
        songPage.setPrefHeight(580);
        songPage.setMaxHeight(580);
        songPage.setStyle(
            "-fx-background-color: #fafafa;" +
            "-fx-padding: 20;" +
            "-fx-background-radius: 20;"
        );

        Label sectionLabel = new Label(labelText);
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff69b4; -fx-font-weight: bold;");

        Label titleLabel = new Label(song + " - " + artist);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        System.out.println("RESULT: " + result);
        if (result != null) {
            System.out.println("SUBSTRINGS: " + result.getSubstrings());
            System.out.println("COLORS: " + Arrays.toString(result.getColors()));
        }

        boolean noResults =
            result == null ||
            result.getSubstrings() == null ||
            result.getColors() == null ||
            result.getSubstrings().isEmpty() ||
            result.getColors().length == 0;

        HBox orbRow = new HBox();
        orbRow.setSpacing(40);
        orbRow.setAlignment(Pos.CENTER);
        orbRow.setFillHeight(false);
        orbRow.setMaxWidth(CARD_WIDTH);
        orbRow.setPadding(new Insets(40, 0, 0, 0));

        orbRow.setPrefWidth(CARD_WIDTH);
        orbRow.setAlignment(Pos.CENTER);

        if (noResults) {

            Label unavailable = new Label("RESULTS UNAVAILABLE");
            unavailable.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-text-fill: grey;" +
                "-fx-font-weight: bold;"
            );

            orbRow.getChildren().add(unavailable);

            songPage.setUserData(new String[]{"#000000"}); // lights off

        } else {

            List<String> top3 = new ArrayList<>();

            if (result != null && result.getSubstrings() != null) {
                top3.addAll(result.getSubstrings());
            }

            while (top3.size() < 3) {
                top3.add("No dominant theme");
            }

            top3 = top3.subList(0, 3);

            String[] colors = result.getColors();
            int count = Math.min(top3.size(), colors.length);

            // ONLY top 3 colors
            String[] topColors = Arrays.copyOf(colors, Math.min(3, colors.length));

            for (int i = 0; i < count; i++) {

                VBox orbContainer = new VBox(6);
                orbContainer.setAlignment(Pos.CENTER);

                Circle orb = new Circle(60);
                orb.setFill(createGradient(topColors[i]));
                orb.setEffect(new javafx.scene.effect.Glow(0.6));

                ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), orb);
                pulse.setFromX(1);
                pulse.setToX(1.1);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();

                Label substringLabel = new Label(top3.get(i));
                substringLabel.setWrapText(true);
                substringLabel.setMaxWidth(120);
                substringLabel.setMinHeight(Region.USE_PREF_SIZE);
                substringLabel.setTextAlignment(TextAlignment.CENTER);
                substringLabel.setStyle("-fx-font-size: 10px; -fx-alignment: center;");

                Label hexLabel = new Label(topColors[i]);
                hexLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: grey;");
                hexLabel.setMaxWidth(120);
                hexLabel.setWrapText(true);
                hexLabel.setTextAlignment(TextAlignment.CENTER);

                orbContainer.getChildren().addAll(orb, substringLabel, hexLabel);
                orbRow.getChildren().add(orbContainer);
            }

            songPage.setUserData(topColors); // lights
        }

        songPage.getChildren().addAll(sectionLabel, titleLabel, orbRow);
        carousel.getChildren().add(songPage);

        // animation
        songPage.setOpacity(0);
        songPage.setFillWidth(false);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), songPage);
        fade.setToValue(1);

        TranslateTransition lift = new TranslateTransition(Duration.seconds(0.8), songPage);
        lift.setToY(0);

        ScaleTransition pop = new ScaleTransition(Duration.seconds(0.6), songPage);
        pop.setToX(1);
        pop.setToY(1);

        PauseTransition delay = new PauseTransition(Duration.seconds(index * 0.5));

        SequentialTransition sequence = new SequentialTransition(
            delay,
            new ParallelTransition(fade, lift, pop)
        );

        sequence.play();
    }


    private void snapToNearest(double dragAmount) {

        double threshold = 40;

        if (dragAmount < -threshold &&
                currentIndex < carousel.getChildren().size() - 1) {

            currentIndex++;

        } else if (dragAmount > threshold &&
                currentIndex > 0) {

            currentIndex--;
        }

        animateCarousel();
    }
    
    private void animateCarousel() {

        if (carousel.getChildren().isEmpty()) return;

        double step = CARD_WIDTH + SPACING;

        // ALWAYS compute fresh width (never cached)
        double viewportWidth = viewport.getWidth();

        // IMPORTANT: force layout before measuring
        viewport.applyCss();
        viewport.layout();

        double targetX =
                (viewportWidth - CARD_WIDTH) / 2.0
                - (currentIndex * step);

        if (activeSlide != null) {
            activeSlide.stop();
        }

        activeSlide = new TranslateTransition(Duration.millis(300), carousel);
        activeSlide.setToX(targetX);
        activeSlide.setInterpolator(Interpolator.EASE_BOTH);
        activeSlide.play();

        updateLights();
    }
        /* activeSlide.setOnFinished(e -> {
            syncLights();
        }); */
        
    private int getMaxIndex() {
        return Math.max(0, carousel.getChildren().size() - 1);
    }

    private void next() {
        if (carousel.getChildren().isEmpty()) return;

        if (currentIndex < carousel.getChildren().size() - 1) {
            currentIndex++;
            animateCarousel();
        }
    }

    private void previous() {
        if (carousel.getChildren().isEmpty()) return;

        if (currentIndex > 0) {
            currentIndex--;
            animateCarousel();
        }
    }

    private void enableSwipe() {

        viewport.setOnMousePressed(e -> {
            dragAnchorX = e.getSceneX();
            initialTranslateX = carousel.getTranslateX();
        });

        viewport.setOnMouseDragged(e -> {
            double delta = e.getSceneX() - dragAnchorX;

            carousel.setTranslateX(initialTranslateX + delta);
        });

        viewport.setOnMouseReleased(e -> {

            double delta = e.getSceneX() - dragAnchorX;
            double threshold = 80;

            if (delta < -threshold) {
                currentIndex++;
            } else if (delta > threshold) {
                currentIndex--;
            }

            currentIndex = Math.max(0,
                    Math.min(currentIndex, carousel.getChildren().size() - 1));

            animateCarousel();
            updateLights();
        });
    }

    private RadialGradient createGradient(String hexColor) {

        Color base = Color.web(hexColor);
        Color lighter = base.deriveColor(0, 1, 1.4, 1);

        return new RadialGradient(
                0,
                0,
                0.5,
                0.5,
                0.5,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, base),
                new Stop(1, lighter)
        );
    }

    private void updateLights() {

        if (carousel.getChildren().isEmpty()) return;

        VBox card = (VBox) carousel.getChildren().get(currentIndex);
        String[] colors = (String[]) card.getUserData();

        SmartLightController.setAllColors(colors);
    }

    /* private void updateLights() {

        if (carousel.getChildren().isEmpty()) {
            SmartLightController.turnOffAll();
            return;
        }

        VBox currentCard = (VBox) carousel.getChildren().get(currentIndex);
        Object data = currentCard.getUserData();

        if (data == null) {
            SmartLightController.turnOffAll();
            return;
        }

        String[] colors = (String[]) data;

        if (colors.length == 0) {
            SmartLightController.turnOffAll();
            return;
        }

        System.out.println("CURRENT INDEX: " + currentIndex);
        System.out.println("COLORS: " + Arrays.toString(colors));

        SmartLightController.setAllColors(colors);
    } */

    private void syncLights() {
        if (carousel.getChildren().isEmpty()) return;

        VBox card = (VBox) carousel.getChildren().get(currentIndex);
        String[] colors = (String[]) card.getUserData();

        SmartLightController.setAllColors(colors);
    }

    private void resetHome() {
        carousel.getChildren().clear();

        songField1.clear();
        artistField1.clear();

        songField2.clear();
        artistField2.clear();

        songField3.clear();
        artistField3.clear();

        SmartLightController.turnOffAll();

        currentIndex = 0;
        carousel.setTranslateX(0);
    }

}

