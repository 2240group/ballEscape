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
