package ballescape;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainGame extends Application {

    private static Stage window;

    private static AnimationTimer timer;

    private static Pane gameRoot, gamePane;
    private static GameProperties salah; // Striker

    static Label scoreLabel, ballsLabel; // score tab and the balls tab

    static VBox finalScore; // gameResult info holder

    static MediaPlayer IntroSound;

    static Timer linearTimer;

    private static List<GameProperties> bullets = new ArrayList<>();
    private static List<GameProperties> enemies = new ArrayList<>();

    private static BackgroundImage getBackground() {
        return new BackgroundImage(
                new Image("./assets/fbground.jpg"), //picture credits: https://www.etsy.com/listing/509000765/1-x-a4-printed-football-pitch-wallpaper
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        true,
                        false,
                        true,
                        false
                )
        );
    }



    private static void gameStart() {    //triggers the activities to take place once the user presses the start button


        // sets the enemies and balls to zero
        bullets.clear();
        enemies.clear();

        // once the game is over the score counter is set back to zero
        Game.endTarget = 0;

        // sets the XP to a maximum which is 100%
        Game.xp = 100;

        // Sets the balls that the striker has to 50
        Game.balls = 50;

        window.setScene(new Scene(gameObjects())); // create game scene

        window.getScene().setOnKeyPressed(e -> {

            // ending all events when game is brought to an end
            if (!Game.isCreated() || Game.isPaused() || Game.isStopped())
                return;

            Bullet bullet; // bullet instance

            // moves the striker left by a factor of 7
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.A) {
                salah.shiftLeft();
            }

            // moves the striker right by a factor of 7
            else if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.D) {
                salah.shiftRight(gameRoot.getWidth());
            }

            // Single shot fire if user presses 'X'
            else if (e.getCode() == KeyCode.X) {
                if (Game.outOfBalls())
                    return;

                bullet = new Bullet();
                bullet.setVelocity(new Point2D(0, bullet.velocity));
                addBullet(bullet, bullet.getMiddle(salah), salah.getView().getTranslateY());

                Game.ballsReduce(1);
            }

            // Double shot fire if user presses 'Z'
            else if (e.getCode() == KeyCode.Z) {

                // prevent firing if there is no balls
                if (Game.outOfBalls(true))
                    return;

                bullet = new Bullet();
                bullet.setVelocity(new Point2D(0, bullet.velocity));
                addBullet(bullet, bullet.getLeftSide(salah), salah.getView().getTranslateY() + 25);
                bullet = new Bullet();
                bullet.setVelocity(new Point2D(0, bullet.velocity));
                addBullet(bullet, bullet.getRightSide(salah), salah.getView().getTranslateY() + 25);

                Game.ballsReduce(2);
            }
        });
        window.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                // when the screen is minimised the game first pauses and once the screen has been minimised the game then resumes again
                if (window.getX() == -32000.0) {
                    if (Game.isCreated() && Game.isStarted()) {
                        Game.pause();
                    }
                }
                // when the screen is maximised the game first pauses and once the screen has been maximised the game then resumes again
                else {
                    if (Game.isCreated() && Game.isPaused()) {
                        Game.resume();
                    }
                }
            }
        });

        Game.created(); // signal that game is created to whom it may concern

        linearTimer = new Timer();
        TimerTask increaseBalls = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!Game.isStopped()) {
                        Game.increaseXpPerSecond();
                        Game.ballsIncrease();
                    }
                });
            }
        };
        linearTimer.schedule(increaseBalls, 1000, 1000);
    }

    private static Button getStartButton() {
        return new Button("START");
    }

    private static Button getReStartButton() {
        return new Button("TRY AGAIN");
    }

    private static Label instructions(String text) {

        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-weight:bold");

        return label;
    }

    private Parent createEntryContent() {
        Pane entryRoot = new Pane();
        entryRoot.setPrefSize(600, 600);

        VBox box = new VBox();
        box.setTranslateX(128.5);
        box.setTranslateY(100);
        box.setAlignment(Pos.CENTER);

        ImageView title = new ImageView(new Image("./assets/BE.png"));

        Label shiftLeft = instructions("PRESS RIGHT ARROW TO MOVE STRIKER RIGHT");
        Label shiftRight = instructions("PRESS LEFT ARROW TO MOVE STRIKER LEFT");
        Label signalFire = instructions("PRESS X TO SHOOT ONE BALL");
        Label doubleFire = instructions("PRESS Z TO SHOOT TWO BALLS");
        box.getChildren().addAll(title, shiftLeft, shiftRight, signalFire, doubleFire);


        String line1 = ("Group Members:");
        String line2 = ("Eshan Raikar");
        String line3 = ("Anuar Aitkali");
        String line4 = ("Takshil Patel");
        String line5 = ("Ayaan Shirazi");
        Label author = instructions(line1 + "\n" + line2 + "\n" + line3 + "\n" + line4 + "\n" + line5);
        author.setStyle("-fx-font-weight: bold");
        author.setTextFill(Color.BLACK); //check the colour
        VBox desc = new VBox();
        desc.setTranslateX(20);
        desc.setTranslateY(500);
        desc.setAlignment(Pos.CENTER);
        desc.getChildren().add(author);

        // start button
        Button start = getStartButton();
        start.setTranslateX(235);
        start.setTranslateY(380);
        start.setStyle("-fx-font-weight: bold");
        start.setCursor(Cursor.HAND);
        start.setOnAction((ActionEvent d) -> gameStart());

        // create background image
        BackgroundImage bg = getBackground();

        gamePane = new Pane();
        gamePane.setPrefSize(600, 700);
        gamePane.setBackground(new Background(bg));
        gamePane.setTranslateX(0);
        gamePane.setTranslateY(0);

        entryRoot.getChildren().addAll(gamePane, box, start, desc);

        Media music = new Media(new File("./src/assets/8bit.mp3").toURI().toString());
        IntroSound = new MediaPlayer(music);
        IntroSound.setAutoPlay(true);
        IntroSound.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                IntroSound.seek(Duration.ZERO);
            }
        });

        return entryRoot;
    }

    private static Parent gameObjects() {
        gameRoot = new Pane();
        gameRoot.setPrefSize(600, 600);

        // create background image
        BackgroundImage bg = getBackground();

        gamePane = new Pane();
        gamePane.setPrefSize(600, 3600);
        gamePane.setBackground(new Background(bg));
        gamePane.setTranslateX(0);
        gamePane.setTranslateY(-3000);

        //Balls tab is set to null and the height and width are set
        ballsLabel = instructions("");
        Game.updateBallsLabel();
        ballsLabel.setTranslateX(520);
        ballsLabel.setTranslateY(570);

        //Score is set to null and the height and width are set
        scoreLabel = instructions("");
        Game.updateScoreLabel();
        scoreLabel.setTranslateX(20);
        scoreLabel.setTranslateY(570);


        //setting the height and width of the XP rectangle
        Game.xpPane.setTranslateX(180);
        Game.xpPane.setTranslateY(572);

        gameRoot.getChildren().addAll(gamePane, ballsLabel, Game.xpPane, scoreLabel);

        salah = new Player();
        salah.setVelocity(new Point2D(1, 0));
        salah.setWidth(64); // width of space ship image
        addGameObject(salah, 268, 480);

        IntroSound.setVolume(.2);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                onUpdate();
            }
        };
        timer.start();

        Game.start(); // signal to inform receivers the game is status

        return gameRoot;
    }
