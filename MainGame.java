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

    private static void addBullet(GameProperties bullet, double x, double y) {
        bullets.add(bullet);
        addGameObject(bullet, x, y);
    }

    private static void addEnemy(GameProperties enemy, double x, double y) {
        enemies.add(enemy);
        addGameObject(enemy, x, y);
    }

    private static void addGameObject(GameProperties object, double x, double y) {
        object.getView().setTranslateX(x);
        object.getView().setTranslateY(y);
        gameRoot.getChildren().add(object.getView());
    }

    private static void onUpdate() {

        // set enemies dead that asteroids out of gameview
        for (GameProperties enemy : enemies) {
            if (enemy.getView().getTranslateY() > 600) {
                enemy.setAlive(false);
            }
        }

        //same as above but for defender
        for (GameProperties defender : enemies) {
            if (defender.getView().getTranslateY() > 600) {
                defender.setAlive(false);
            }
        }

        // detect crash
        for (GameProperties enemy : enemies) {
            if (salah.isColliding(enemy)) {
                enemy.setAlive(false);

                Game.decreaseXpForCrash(); // decrease health

                gameRoot.getChildren().remove(enemy.getView());

                Game.endGameOnCollision();
            }
        }

        //detect crash for defender
        for (GameProperties defender : enemies) {
            if (salah.isColliding(defender)) {
                defender.setAlive(false);

                Game.decreaseXpForCrash(); // decrease health

                gameRoot.getChildren().remove(defender.getView());

                Game.endGameOnCollision();
            }
        }

        // method to see if the bullets are colliding with defender 1 if so then the defender and bullets are not visible anymore
        for (GameProperties bullet : bullets) {
            for (GameProperties enemy : enemies) {
                if (bullet.isColliding(enemy) && enemy.getView().getTranslateY() > -28) {
                    bullet.setAlive(false);
                    enemy.setAlive(false);

                    Game.increaseScore();

                    gameRoot.getChildren().removeAll(bullet.getView(), enemy.getView());
                }
            }
        }

        // method to see if the bullets are colliding with defender 2 if so then the defender and bullets are not visible anymore
        for (GameProperties bullet : bullets) {
            for (GameProperties defender : enemies) {
                if (bullet.isColliding(defender) && defender.getView().getTranslateY() > -28) {
                    bullet.setAlive(false);
                    defender.setAlive(false);

                    Game.increaseScore();

                    gameRoot.getChildren().removeAll(bullet.getView(), defender.getView());
                }
            }
        }

        // Makes the defenders and balls disappear when they collide together
        bullets.removeIf(GameProperties::isDead);
        enemies.removeIf(GameProperties::isDead);

        // code to keep a tab on the position of the balls and the defenders
        bullets.forEach(GameProperties::update);
        enemies.forEach(GameProperties::update);

        // code to generate defender 1
        // defender 1 is made to generate more times than defender 2 but is slower than defender 2
        if (Math.random() < 0.02) {
            Enemy enemy = new Enemy();
            enemy.setVelocity(new Point2D(0.2, 1.1));
            addEnemy(enemy, Math.random() * gameRoot.getWidth(), Math.random() * (-1 * gameRoot.getPrefHeight() / 2));
        }

        // code to generate defender 2
        // defender 2 is made to generate lesser times than defender 1 but is made faster
        if (Math.random() < 0.01) {
            Enemy2 defender = new Enemy2();
            defender.setVelocity(new Point2D(-0.2, 1.5));
            addEnemy(defender, Math.random() * gameRoot.getWidth(), Math.random() * (-1 * gameRoot.getPrefHeight() / 2));
        }


        // this code makes the background move by a factor of 0.2 in the negative Y-axis giving the user a feel of moving ahead referring to progress
        gamePane.setTranslateY(gamePane.getTranslateY() + 0.2);

    }

    private static class Game<balls> {
        private static boolean created = false;
        private static int xpIncrease = 17; // The XP of the striker increases by a factor of only 50% of the XP lost each time
        private static int xpReduce = 35; // The XP of the striker reduces by 35% each time the striker collides with the defenders
        private static int xp = 100; // The XP of the striker is set as 100 each time the game starts
        private static int playOrPause = 0;
        private static int balls = 30; // The Striker is given 30 balls in the start of the game
        private static int ballsIncrease = 1; // After every second the number of balls that the striker
        private static int endTarget = 0;
        private static Pane xpPane = new Pane(xpBar());

        static boolean isCreated() {
            return created;
        }

        static void created() {
            created = true;
        }

        static void start() {
            playOrPause = 1;
        }

        static boolean isStarted() {
            return playOrPause == 1;
        }

        static void pause() {
            playOrPause = 2;
            timer.stop();
        }

        static boolean isPaused() {
            return playOrPause == 2;
        }

        static void resume() {
            playOrPause = 1;
            timer.start();
        }

        static void stop() {
            playOrPause = 3;
            timer.stop();
        }

        static boolean isStopped() {
            return playOrPause == 3;
        }

        static boolean outOfBalls() { // When the striker runs out of balls to shoot the balls are set to be zero and does not reduce below that into the negatives
            return balls <= 0;
        }

        static boolean outOfBalls(boolean multiBullet) {
            return balls <= 1;
        }

        static void ballsIncrease() {
            if(balls<=47) {
                balls += Game.ballsIncrease;
                updateBallsLabel();
            }
        }

        static void ballsReduce(int subtract) {
                balls -= subtract;
                updateBallsLabel();
        }

        static void updateBallsLabel() {
            ballsLabel.setText("BALLS: " + balls); //method to update the number of balls by 3

            if (balls < 10) {
                ballsLabel.setStyle("-fx-text-fill: red"); //this makes the user aware that he/she is on their last 10 balls by making the text turn to red
            } else {
                ballsLabel.setStyle(null);
            }
        }

        static void updateScoreLabel() {
            scoreLabel.setText("Score : " + getDestroy());
        }

        static int getDestroy() {
            return endTarget;
        }

        static void increaseScore() { // Every time the striker shoots at a defender the score is increased by 1
            endTarget += 1;
            updateScoreLabel(); // The counter is then sent to the ScoreLabel
        }

        private static Rectangle xpBar() {
            return new Rectangle((300 / 100) * Game.xp, 10, Color.web("#00FF00")); // The XP bar is set as a rectangle filled with green colour
        }

        static void setXp() {
            Game.xpPane.getChildren().remove(0);
            Game.xpPane.getChildren().add(xpBar());
        }


        static void increaseXpPerSecond() {
            xp = Math.min(xp + xpIncrease, 100); // Even though the XP bar increases by a factor of 50% of the XP lost each time it is important to not let the XP bar increase above 100%
            setXp();
        }

        static void decreaseXpForCrash() {
            xp = Math.max(xp - xpReduce, 0); // code to not let the XP bar go below 0
            setXp();
        }


        static void endGameOnCollision() {
            if (StrikerCrash()) {
                stop();

                linearTimer.cancel();

                showFinalScore(); // When the defender collides with the striker and the XP is at zero the game ends after the final score is shown on the screen
            }
        }

        private static void showFinalScore() {

            finalScore = new VBox(15);

            ImageView endGame = new ImageView(new Image("./assets/gamefinish.png")); // the game ends by showing this game over picture

            Label score = instructions("Score : " + getDestroy());
            Button start = getReStartButton();
            start.setOnAction((ActionEvent e) -> gameStart());


            finalScore.getChildren().addAll(endGame, score, start);

            //display of final score in the middle when the game comes to an end
            finalScore.setTranslateX(176);
            finalScore.setTranslateY(200);
            finalScore.setAlignment(Pos.CENTER);

            gameRoot.getChildren().add(finalScore);


        }

        private static boolean StrikerCrash() {
            return xp <= 0;
        }
    }

    private static class Player extends GameProperties {
        Player() {
            super(new ImageView(new Image("./assets/Salah.png"))); //image for striker

            setWidth(64);
        }
    }

    private static class Enemy extends GameProperties {
        Enemy() {
            super(new ImageView(new Image("./assets/ramos.png"))); //image for defender 1
        }
    }

    private static class Enemy2 extends GameProperties {
        Enemy2() {
            super(new ImageView(new Image("./assets/vandijkfinal.png"))); //image for defender 2
        }
    }

    private static class Bullet extends GameProperties {

        private int velocity = -7; //the balls are set to move at a speed of 7 in the upward direction, that is what the negative represents
        private int size = 4; // SIze of the sprite

        Bullet() {
            super(new ImageView(new Image("./assets/finalball.png"))); // sprite image for the bulle
        }

        double getLeftSide(GameProperties salah) {
            return salah.getView().getTranslateX() + 0;  //This is for the balls to come out of the left side of the striker when the user presses 'Z'
        }
        double getRightSide(GameProperties salah) {
            return salah.getView().getTranslateX() + salah.getWidth() - Bullet.this.size * 2; //This is for the balls the come out of the right position of the striker when the user presses 'X'
        }

        double getMiddle(GameProperties salah) {
            return salah.getView().getTranslateX() + ((salah.getWidth() - Bullet.this.size) / 2) - 2; //This is for the balls to come out of the center of the striker when the user presses 'X'
        }


    }

    @Override
    public void start(Stage stage) throws Exception {
        window = stage;

        window.setTitle("Ball Escape"); //title of the page is set
        window.getIcons().add(new Image("./assets/finalball.png")); // window icon of the game is set
        window.setScene(new Scene(createEntryContent())); //
        window.setWidth(600);
        window.setHeight(630);
        window.setResizable(false);
        window.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
