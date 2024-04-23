package com.shpp.p2p.cs.skurochka.assignment4;

import acm.graphics.*;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Breakout extends WindowProgram {
    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /** Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /** Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Number of bricks per row */
    private static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    private static final int BRICK_SEP = 4;

    /** It's a bad idea to calculate brick width from APPLICATION_WIDTH */
    // private static final int BRICK_WIDTH = (APPLICATION_WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 10;

    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

    /** Number of turns */
    private static final int NTURNS = 3;

    /** The amount of time to pause between frames (60fps). */
    private static final double PAUSE_TIME = 1000.0 / 60;

    /** Font settings for the caption. */
    private static final Font FONT_FOR_LABEL = new Font("Serif", Font.BOLD, 25);

    /** Initial velocity along the y-axis. */
    private static final double STARTING_VELOCITY_ALONG_Y_AXIS = 3;

    //A paddle for batting the ball.
    private GRect paddle = null;
    // A ball for the game.
    private GOval ball = null;
    // A caption to display information.
    private GLabel label = null;
    // The total number of bricks in the game.
    private int numberOfBricksInTheGame = NBRICKS_PER_ROW * NBRICK_ROWS;

    // The method of launching the program.
    public void run() {
        // Add a paddle to bounce the ball.
        addRacket();
        // Add a mouse listener.
        addMouseListeners();
        // Add a ball to the screen composition.
        addBall();
        // Add bricks to the screen composition.
        drawColumnsOfBricks();
        // A method of launching game animation.
        moveTheBall();
        // Display the final caption on the screen.
        drawFinalGreeting();
    }

    /*
     * A method of creating animations of ball movement and interaction with objects.
     * A variable is created that will hold the number of available attempts for the game.
     * Starting vectors for ball movement.
     * The vector in the X coordinate axis is taken as a random value.
     * The vector of the Y-axis is taken from the corresponding constant.
     * Then a loop is started that will repeat the actions if the number of available games
     * is greater than zero and the number of bricks on the screen is greater than zero.
     * In the loop, the ball is moved.
     * The changes in the motion vectors are tracked depending on the object near which the ball is located.
     * Then it checks whether the ball is on the floor.
     * If it is, the number of available game attempts is reduced by one.
     * Then a window with a corresponding caption is displayed and a mouse click is expected.
     * The starting position for the ball is then set.
     * The starting vectors of the ball movement are also set.
     * At the end of the cycle, a pause is called with the corresponding constant parameter.
     * */
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

    /*
     * Method for setting initial values for the ball.
     * The ball object is taken and values are set for the centering of the ball.
     * */
    private void setTheStartingPositionOfTheBall() {
        ball.setLocation(getWidth() / 2.0 - BALL_RADIUS, getHeight() / 2.0 - BALL_RADIUS);
    }

    /*
     * A method for changing the vector of ball movement.
     * The current motion vector is accepted.
     * Repulsion from the upper wall and reflection from the paddle and bricks are taken into account and removed.
     * A new motion vector is returned.
     * */
    private double workingOutTheMovementAlongTheAxisY(double startDy) {
        startDy = pushingOffTheWallTop(startDy);
        startDy = pushingOffColliderAndBricks(getCollidingObject(), startDy);
        return startDy;
    }

    /*
     * Method of handling an event when the ball is lost.
     * The parameter of the number of attempts played is accepted.
     * The condition is checked if the number of available attempts is greater than zero.
     * If the condition is met, a corresponding message is displayed,
     * and the stream stops until the user clicks the mouse to continue.
     * Then the message is removed.
     * If the condition is not met and there are no available attempts for the game,
     * the ball is removed and the corresponding caption is made.
     * */
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

    /*
     * The method of pushing the ball away from the side walls.
     * The motion vector along the X-axis is accepted.
     * The method calculates the repulsion from the left wall.
     * Then the repulsion from the right wall is calculated.
     * Accordingly, the changed values are returned to the point of calling the method.
     * */
    private double pushingOffTheWalls(double startDx) {
        startDx = pushingOffTheWallLeft(startDx);
        startDx = pushingOffTheWallRight(startDx);
        return startDx;
    }

    /*
     * A method that displays the final congratulation in case of winning.
     * The method checks whether the user has broken all the bricks.
     * If the condition is met, the congratulation message is displayed.
     * Then the ball and the paddle are removed.
     * */
    private void drawFinalGreeting() {
        if (numberOfBricksInTheGame <= 0) {
            drawLabel("You have won.");
            remove(ball);
            remove(paddle);
        }
    }

    /*
     * A method of drawing lettering on the screen.
     * Accepted as a string parameter.
     * In the middle, a GLabel object is created with an input label,
     * which is assigned to the label class variable.
     * Then the font of the label is set.
     * After that, the color of the label is set.
     * Then the parameters of the coordinates of placement on the screen are added.
     * Then the label is added to the screen composition.
     * */
    private void drawLabel(String inputString) {
        label = new GLabel(inputString);
        label.setFont(FONT_FOR_LABEL);
        label.setColor(Color.BLUE);
        label.setLocation(getWidth() / 2.0 - label.getWidth() / 2.0, getHeight() / 2.0 - label.getHeight() / 2.0);
        add(label);
    }

    /*
     * A method of obtaining colors.
     * The method returns an array with the colors needed to draw all rows of bricks.
     * */
    private Color[] getColor() {
        return new Color[]{Color.RED, Color.RED, Color.ORANGE, Color.ORANGE, Color.YELLOW, Color.YELLOW, Color.GREEN,
                Color.GREEN, Color.CYAN, Color.CYAN, Color.MAGENTA, Color.MAGENTA, Color.GRAY, Color.GRAY};
    }

    /*
     * A method for building all brick strings.
     * In the middle, an array of colors for all rows is obtained,
     * then all rows are built in a loop using the corresponding colors.
     * */
    private void drawColumnsOfBricks() {
        Color[] colors = getColor();
        for (int i = 0; i < NBRICK_ROWS; i++) {
            drawRowOfBrick((BRICK_HEIGHT + BRICK_SEP) * i, colors[i]);
        }
    }

    /*
     * A method of building a single row of bricks.
     * The value of the starting position X and the color value are accepted.
     * In the middle, the width of the brick is calculated depending on the width of
     * the window and the number of bricks in one row.
     * Then the entire row is built in a loop.
     * */
    private void drawRowOfBrick(double startY, Color color) {
        double brickWidth = (getWidth() - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / (double) NBRICKS_PER_ROW;
        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            drawBrick(0 + (brickWidth + BRICK_SEP) * i, startY, brickWidth, BRICK_HEIGHT, color);
        }
    }

    /*
     * The method of drawing a single brick.
     * Accepts the parameters of the start and end positions X and Y, as well as the color of the brick.
     * An object of type GRect is created with the input parameters.
     * Then the color of the object is set.
     * Then you set the visibility of the inner field.
     * The last step is to add the brick to the screen composition.
     * */
    private void drawBrick(double startX, double startY, double endX, double endY, Color color) {
        GRect gRect = new GRect(startX, startY + BRICK_Y_OFFSET, endX, endY);
        gRect.setColor(color);
        gRect.setFilled(true);
        add(gRect);
    }

    /*
     * The method of changing the direction vector when colliding with a paddle,
     * changing the motion vector when colliding with a brick, with the removal of the brick.
     * If the ball is near the paddle and the direction of motion is positive, the motion vector is reversed.
     * If there is nothing near the ball, the vector does not change.
     * If there is an object near the ball, and it is not a paddle,
     * then the number of bricks is reduced, then this object is removed and the vector of motion is reversed.
     * If none of the conditions are met, the unchanged motion vector is simply returned.
     * */
    private double pushingOffColliderAndBricks(GObject collider, double startDy) {
        if (collider == paddle && startDy > 0) {
            return -startDy;
        } else if (collider == null) {
            return startDy;
        } else if (collider != paddle) {
            numberOfBricksInTheGame--;
            remove(collider);
            return -startDy;
        } else {
            return startDy;
        }
    }

    /*
     * A method of obtaining objects that are found near the ball.
     * The method checks to see if the receiver is near the first point of the ball.
     * If the object is a paddle, then null is returned.
     * If the object is not null, then the found object is returned.
     * If the object is not found in the data exactly,
     * then the variable is assigned an object that is located according to the parameters of the second point.
     * If there is a paddle, then zero is returned.
     * If there is another object, the object is returned.
     * If no object is found under the given point,
     * the object is assigned an object according to the parameters of the next ball point.
     * If an object is found there, this object is returned.
     * If no object is found, the variable is assigned the value of the last point of the ball.
     * If an object is found there, the found object is returned.
     * If the object is not found, null is returned.
     * */
    private GObject getCollidingObject() {
        GObject cornerObject = getElementAt(ball.getX(), ball.getY());
        if (cornerObject == paddle) { return null; }
        if (cornerObject == null) {
            cornerObject = getElementAt(ball.getX() + (2 * BALL_RADIUS), ball.getY());
            if (cornerObject == paddle) { return null; }
            if (cornerObject == null) {
                cornerObject = getElementAt(ball.getX(), ball.getY() + (2 * BALL_RADIUS));
                if (cornerObject == null) {
                    cornerObject = getElementAt(ball.getX() + (2 * BALL_RADIUS),
                            ball.getY() + (2 * BALL_RADIUS));
                }
            }
        }
        return cornerObject;
    }

    /*
     * A method for changing the motion vector when the ball touches the top wall.
     * Returns the changed value depending on the conditions.
     * */
    private double pushingOffTheWallTop(double dyBall) {
        return ballBelowTopWall(ball) && dyBall < 0 ? -dyBall : dyBall;
    }

    /*
     * A method for changing the motion vector when the ball touches the right wall.
     * Returns the changed value depending on the conditions.
     * */
    private double pushingOffTheWallRight(double dxBall) {
        return ballBehindTheWallRight(ball) && dxBall > 0 ? -dxBall : dxBall;
    }

    /*
     * A method of changing the motion vector when the ball touches the left wall.
     * Returns the changed value depending on the conditions.
     * */
    private double pushingOffTheWallLeft(double dxBall) {
        return ballBehindTheWallLeft(ball) && dxBall < 0 ? -dxBall : dxBall;
    }

    /*
     * Method for checking if the floor is touched.
     * The conditions are checked and a boolean value is returned.
     * */
    private boolean ballBelowFloor(GOval ball) {
        return ball.getY() + ball.getHeight() >= getHeight();
    }

    /*
     * Method for checking if the top wall is touched.
     * The conditions are checked and a boolean value is returned.
     * */
    private boolean ballBelowTopWall(GOval ball) {
        return ball.getY() <= 0;
    }

    /*
     * Method for checking if the right wall is touched.
     * The conditions are checked and a boolean value is returned.
     * */
    private boolean ballBehindTheWallRight(GOval ball) {
        return ball.getX() + ball.getWidth() >= getWidth();
    }

    /*
     * The method of checking the left wall.
     * Conditions are checked and a boolean value is returned.
     * */
    private boolean ballBehindTheWallLeft(GOval ball) {
        return ball.getX() + ball.getWidth() <= ball.getWidth();
    }

    /*
     * A method for obtaining a random value of a variable speed along the X coordinate axis.
     * A randomizer object is created.
     * Then a random value from one to three is obtained.
     * Then this value is returned with a nak that is set by the random boolean value generator.
     * */
    private double getVx() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        double vx = rgen.nextDouble(1.0, 3.0);
        return rgen.nextBoolean(0.5) ? -vx : vx;
    }

    /*
     * A method for drawing a ball.
     * A GOval object with constant parameters is created, a reference to which is assigned to the ball variable.
     * The inner field of the ball is set.
     * Then the ball is added to the screen composition.
     * */
    private void addBall() {
        ball = new GOval(getWidth() / 2.0 - BALL_RADIUS,
                getHeight() / 2.0 - BALL_RADIUS, 2 * BALL_RADIUS, 2 * BALL_RADIUS);
        ball.setFilled(true);
        add(ball);
    }

    /*
     * The method of drawing a paddle (racket).
     * A GRect object with constant parameters is created, a reference to which is assigned to the paddle.
     * The internal field of the paddle is set.
     * Then the paddle is added to the screen composition.
     * */
    private void addRacket() {
        paddle = new GRect(getWidth() / 2.0 - PADDLE_WIDTH / 2.0,
                getHeight() - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        add(paddle);
    }

    /*
     * A method that will get a mouse listener.
     * Checks the conditions for possible movement of the paddle to bounce the ball.
     * If the conditions are met, the paddle is moved along with the mouse cursor.
     * */
    public void mouseMoved(MouseEvent mouseEvent) {
        if (PADDLE_WIDTH / 2.0 < mouseEvent.getX() && (getWidth() - PADDLE_WIDTH / 2.0) > mouseEvent.getX()) {
            paddle.setLocation(mouseEvent.getX() - PADDLE_WIDTH / 2.0, paddle.getY());
        }
    }
}