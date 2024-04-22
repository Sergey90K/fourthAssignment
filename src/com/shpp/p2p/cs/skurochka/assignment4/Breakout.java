package com.shpp.p2p.cs.skurochka.assignment4;

import acm.graphics.*;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Breakout extends WindowProgram {
    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 3;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /** It's a bad idea to calculate brick width from APPLICATION_WIDTH */
    // private static final int BRICK_WIDTH =
    //        (APPLICATION_WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    /* The amount of time to pause between frames (60fps). */
    private static final double PAUSE_TIME = 1000.0 / 60;

    // Font settings for the caption.
    private static final Font FONT_FOR_LABEL = new Font("Serif", Font.BOLD, 25);

    private static final double STARTING_VELOCITY_ALONG_Y_AXIS = 5;

    GRect paddle = null;
    GOval ball = null;
    GLabel label = null;
    int numberOfBricksInTheGame = NBRICKS_PER_ROW * NBRICK_ROWS;


    public void run() {
        addRacket();
        addMouseListeners();
        addBall();
        drawColumns();
        moveTheBall();
        drawFinalGreeting();
    }

    private void moveTheBall() {
        int numberAttemptsToPlay = NTURNS;
        double startDx = getVx();
        double startDy = STARTING_VELOCITY_ALONG_Y_AXIS;
        while (numberAttemptsToPlay > 0 && numberOfBricksInTheGame > 0) {
            ball.move(startDx, startDy);
            startDx = pushingOffTheWalls(startDx);
            startDy = workingOutTheMovementAlongTheAxisY(startDy);
            if (ballBelowFloor(ball)) {
                numberAttemptsToPlay--;
                workThroughTheFall(numberAttemptsToPlay);
                setTheStartingPositionOfTheBall();
                startDx = getVx();
                startDy = STARTING_VELOCITY_ALONG_Y_AXIS;
            }
            pause(PAUSE_TIME);
        }
    }

    private void setTheStartingPositionOfTheBall() {
        ball.setLocation(getWidth() / 2.0 - BALL_RADIUS, getHeight() / 2.0 - BALL_RADIUS);
    }

    private double workingOutTheMovementAlongTheAxisY(double startDy) {
        startDy = pushingOffTheWallTop(startDy);
        startDy = pushingOffColliderAndBricks(getCollidingObject(), startDy);
        return startDy;
    }

    private void workThroughTheFall(int numberAttemptsToPlay) {
        if (numberAttemptsToPlay > 0) {
            drawLabel("Try again.");
            waitForClick();
            remove(label);
        } else {
            remove(ball);
            drawLabel("Unfortunately, you lost.");
        }
    }

    private double pushingOffTheWalls(double startDx) {
        startDx = pushingOffTheWallLeft(startDx);
        startDx = pushingOffTheWallRight(startDx);
        return startDx;
    }

    private void drawFinalGreeting() {
        if (numberOfBricksInTheGame <= 0) {
            drawLabel("You have won.");
        }
    }

    private void drawLabel(String inputString) {
        label = new GLabel(inputString);
        label.setFont(FONT_FOR_LABEL);
        label.setColor(Color.BLUE);
        label.setLocation(getWidth() / 2.0 - label.getWidth() / 2.0, getHeight() / 2.0 - label.getHeight() / 2.0);
        add(label);
    }

    private Color[] getColor() {
        return new Color[]{Color.RED, Color.RED, Color.ORANGE, Color.ORANGE, Color.YELLOW, Color.YELLOW, Color.GREEN,
                Color.GREEN, Color.CYAN, Color.CYAN, Color.MAGENTA, Color.MAGENTA, Color.GRAY, Color.GRAY};
    }

    private void drawColumns() {
        Color[] colors = getColor();
        for (int i = 0; i < NBRICK_ROWS; i++) {
            drawRowOfBrick((BRICK_HEIGHT + BRICK_SEP) * i, colors[i]);
        }
    }

    private void drawRowOfBrick(double startY, Color color) {
        double brickWidth = (getWidth() - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / (double) NBRICKS_PER_ROW;
        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            drawBrick(0 + (brickWidth + BRICK_SEP) * i, startY, brickWidth, BRICK_HEIGHT, color);
        }
    }

    private void drawBrick(double startX, double startY, double endX, double endY, Color color) {
        GRect gRect = new GRect(startX, startY + BRICK_Y_OFFSET, endX, endY);
        gRect.setColor(color);
        gRect.setFilled(true);
        add(gRect);
    }

    private double pushingOffColliderAndBricks(GObject collider, double startDy) {
        if (collider == paddle) {
            return -startDy;
        } else if (collider == null) {
            return startDy;
        } else {
            numberOfBricksInTheGame--;
            remove(collider);
            return -startDy;
        }
    }

    private GObject getCollidingObject() {
        GObject firstCornerObject = getElementAt(ball.getX(), ball.getY());
        if (firstCornerObject == null) {
            GObject secondCornerObject = getElementAt(ball.getX() + (2 * BALL_RADIUS), ball.getY());
            if (secondCornerObject == null) {
                GObject thirdCornerObject = getElementAt(ball.getX(), ball.getY() + (2 * BALL_RADIUS));
                if (thirdCornerObject == null) {
                    GObject fourthCornerObject = getElementAt(ball.getX() + (2 * BALL_RADIUS), ball.getY() + (2 * BALL_RADIUS));
                    if (fourthCornerObject == null) {
                        return null;
                    } else return fourthCornerObject;
                } else {
                    return thirdCornerObject;
                }
            } else {
                return secondCornerObject;
            }
        } else {
            return firstCornerObject;
        }
    }

    private double pushingOffTheWallTop(double dyBall) {
        return ballBelowTopWall(ball) && dyBall < 0 ? -dyBall : dyBall;
    }

    private double pushingOffTheWallRight(double dxBall) {
        return ballBehindTheWallRight(ball) && dxBall > 0 ? -dxBall : dxBall;
    }

    private double pushingOffTheWallLeft(double dxBall) {
        return ballBehindTheWallLeft(ball) && dxBall < 0 ? -dxBall : dxBall;
    }

    private boolean ballBelowFloor(GOval ball) {
        return ball.getY() + ball.getHeight() >= getHeight();
    }

    private boolean ballBelowTopWall(GOval ball) {
        return ball.getY() <= 0;
    }

    private boolean ballBehindTheWallRight(GOval ball) {
        return ball.getX() + ball.getWidth() >= getWidth();
    }

    private boolean ballBehindTheWallLeft(GOval ball) {
        return ball.getX() + ball.getWidth() <= ball.getWidth();
    }

    private double getVx() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        double vx = rgen.nextDouble(1.0, 3.0);
        return rgen.nextBoolean(0.5) ? -vx : vx;
    }

    private void addBall() {
        ball = new GOval(getWidth() / 2.0 - BALL_RADIUS, getHeight() / 2.0 - BALL_RADIUS, 2 * BALL_RADIUS, 2 * BALL_RADIUS);
        ball.setFilled(true);
        add(ball);
    }

    private void addRacket() {
        paddle = new GRect(getWidth() / 2.0 - PADDLE_WIDTH / 2.0, getHeight() - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        add(paddle);
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        if (PADDLE_WIDTH / 2.0 < mouseEvent.getX() && (getWidth() - PADDLE_WIDTH / 2.0) > mouseEvent.getX()) {
            paddle.setLocation(mouseEvent.getX() - PADDLE_WIDTH / 2.0, paddle.getY());
        }
    }
}
