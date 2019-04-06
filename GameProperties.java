package ballescape;

import javafx.geometry.Point2D;
import javafx.scene.Node;

public class GameProperties {

    private Node view;
    private Point2D velocity = new Point2D(0, 0);

    private boolean alive = true;

    public int shiftSpeed = 7; //moving by a factor of 7
    public int width = 0;

    public GameProperties(Node view) {
        this.view = view;
    }

    public void update() {
        view.setTranslateX(view.getTranslateX() + velocity.getX());
        view.setTranslateY(view.getTranslateY() + velocity.getY());
    }

    public void setVelocity(Point2D velocity) {
        this.velocity = velocity;
    }

    public Node getView() {
        return view;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isDead() {
        return !alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getWidth() {
        return width;
    }

    public void shiftLeft() {
        double leftBoundary = view.getTranslateX() - shiftSpeed;

        if (leftBoundary >= -25)
            view.setTranslateX(leftBoundary);
    }


    public void shiftRight(double rootWidth) {
        double rightBoundary = view.getTranslateX() + shiftSpeed;

        if (rightBoundary <= rootWidth - getWidth() + 25)
            view.setTranslateX(view.getTranslateX() + shiftSpeed);
    }

    public boolean isColliding(GameProperties other) {
        return getView().getBoundsInParent().intersects(other.getView().getBoundsInParent());
    }
}
