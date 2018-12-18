package com.example.judyy.grandnapoleonsolitairegame;



import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * GNS Android Game Application
 * DragDrop.java
 * Purpose: Contains the logic of the drag and drop features and undo feature.
 *
 * @author Andrew Lin
 * @version 1.0 10/15/2017
 */

public class DragDrop {
    // Private variables only in DragDrop
    private static float dx, dy, x, y, initialX, initialY;
    private static float stackHeight;
    private static float stackWidth;
    protected static int direction = 0;

    // Input from GameActivity
    private static Card[] cards;
    private static Stack[] stacks;
    private static Recorder recorder;
    private static HintSolver solver;

    // Variables for Step Counter
    public static TextView stepCounter;
    public static int numSteps = 0;

    public static int step = 0;
    public static Context c;

    /**
     * Main method of the Drag and Drop feature. Called from GameActivity, and this will take
     * care of the Drag and Drop with the game rules
     *
     * @param context
     * @param c       The array of cards created in GameActivity
     * @param s       The array of stacks created in GameActivity
     * @param counter The TextView element of the counter
     * @param undo    The Button element of the undo button
     */
    public void main(Context context, Card[] c, Stack[] s, Recorder r, HintSolver hint) {
        // Assignment for variables used in Drag and Drop
        cards = c;
        stacks = s;
        recorder = r;
        solver = hint;
        numSteps = 0;   // Reset numSteps to 0
        // Get size of stack ImageView
        direction = 0;
        stackHeight = stacks[0].getHeight();    // Set stack height
        stackWidth = stacks[0].getWidth();      // Set stack width
        this.c = context;

        // Enable touch for cards
        enableTouch();
    }

    /**
     * Method called when a card has been pressed on by the user.
     * ACTION_DOWN is the event when it detects a touch.
     *
     * @param v     Not used
     * @param event The event that was detected. In this case, it is ACTION_DOWN
     * @param c     The card that detected the touch event.
     */
    private static void actionDown(View v, MotionEvent event, Card c) {
        ImageView i = c.getImageView();
        // Get the card's ImageView initial position. It gets the top left corner of the card's ImageView
        initialX = i.getX();
        initialY = i.getY();
        // Get the location of the touch event
        x = event.getRawX();
        y = event.getRawY();
        // Calculate the locatino ON the card where the touch event happened.
        dx = x - i.getX();
        dy = y - i.getY();
        // Remove the card from its stack
//        stacks[c.getCurrentStackID()].removeCardFromStack(c);
    }

    /**
     * Method called when a card is being moved after ACTION_DOWN.
     * ACTION_MOVE is the event when it is being moved
     *
     * @param i ImageView of the card
     */
    private static void actionMove(ImageView i) {
        // Set the ImageView of the card to follow the touch
        i.setX(x - dx);
        i.setY(y - dy);
    }

    /**
     * Method called when a card is being dropped (or let go) after ACTION_DOWN/ACTION_MOVE
     * ACTION_UP is the event when it is being let go
     * The game rule implementation checks happen in this method.
     *
     * @param card Card being dropped
     * @param x    x location of where the card is being dropped
     * @param y    y location of where the card is being dropped
     */
    private void actionUp(Card card, float x, float y) {
        ImageView cardImage = card.getImageView();
        int cardID = card.getCurrentStackID();
        // Get spacing between stacks to offset the card if a stack has > 1 card.
        float xSpaceStack = Math.abs(stacks[0].getLeftSideLocation() - stacks[4].getLeftSideLocation())*0.18f;
        float ySpaceStack = Math.abs(stacks[0].getTopSideLocation() - stacks[1].getTopSideLocation())*0.13f;
        int whichStack = -1;
        float tempX = 0;    // Variable for stack left side location
        float tempY = 0;    // Variable for stack top side location

        // Check which stack is the card being dropped at
        for (int i = 0; i < stacks.length; i++) {
            tempX = stacks[i].getLeftSideLocation();
            tempY = stacks[i].getTopSideLocation();
            if (x >= tempX && x <= tempX + stackWidth) {
                if (y >= tempY && y <= tempY + stackHeight) {
                    whichStack = i;
                    break;
                }
            }
        }



        Log.d("stackBeingDropped", "tempX " + tempX + " tempY " + tempY + " whichStack " + whichStack );
        boolean validStack = canStack(whichStack, card.getCurrentStackID());   // Check if the stack can be stacked.

        if (!validStack){
            // Compensate for touchscreen control

            int row = 40;
            int i = 44;
            while(i>=row){
                tempY = stacks[i].getTopSideLocation();
                if (y >= tempY) row = i;
                i--;
            }
            whichStack = calculateWhichStack(x, row-40);
            validStack = canStack(whichStack, card.getCurrentStackID());
        }

        Log.d("stackBeingDropped", "validStack " + String.valueOf(validStack));

        // Location where the card's ImageView should be set to
        float xToSet = 0;
        float yToSet = 0;

        // Check if target stack is valid for putting cards on
        if (validStack) {

            // Set values to record later
            float previousX = card.getXPosition();
            float previousY = card.getYPosition();
            int previousStack = card.getCurrentStackID();
            boolean previousCanMove = card.getCanMove();
            boolean recorded = false;

            // Special case. Card is dropped in cellar.
            if (whichStack == 48 && stacks[whichStack].getCurrentCards().size() == 0) {
                Stack currentStack = stacks[card.getCurrentStackID()];
                Stack stackToDrop = stacks[whichStack];

                // Assign the location to be set for the card
                xToSet = stacks[whichStack].getLeftSideLocation();
                yToSet = stacks[whichStack].getTopSideLocation();
                moveCard(currentStack, card, stackToDrop, xToSet, yToSet);

                // Lock card
                if (stacks[47].getCurrentCards().size() > 0 && stacks[49].getCurrentCards().size() > 0  ){
                    card.setCanMove(false);
                }

                // Special case. Stack is 16 to 19, or 24 to 27 and the stack is empty
            } else if (((whichStack >= 16 && whichStack < 20) || (whichStack >= 24 && whichStack < 28)) && stacks[whichStack].getCurrentCards().size() == 0) {

                Stack currentStack = stacks[card.getCurrentStackID()];
                Stack stackToDrop = stacks[whichStack];
                // Assign the location to be set for the card
                xToSet = stackToDrop.getLeftSideLocation();
                yToSet = stackToDrop.getTopSideLocation();
                moveCard(currentStack, card, stackToDrop, xToSet, yToSet);
                recorder.recordStep(card, previousX, previousY, previousStack, previousCanMove);
                recorded = true;
                while(currentStack.getCurrentCards().size() != 0  && (currentStack.getStackID() >23 || currentStack.getStackID() < 20)){
                    card = currentStack.getLastCard();
                    actionUp(card, x, y);
                }

                // Any other case
            } else {
                Stack stackToDrop = stacks[whichStack];
                Stack currentStack = stacks[card.getCurrentStackID()];
                // Compare whether the two cards can be stacked
                if (compareCards(stackToDrop, card)) {
                    // On the left side of the board, set offset to the left
                    if (whichStack < 20) {
                        xToSet = stackToDrop.getLeftSideLocation() - xSpaceStack;
                        yToSet = stackToDrop.getTopSideLocation();
                    // In the base stacks, set offset to below
                    } else if (whichStack < 24) {
                        xToSet = stackToDrop.getLeftSideLocation();
                        yToSet = stackToDrop.getTopSideLocation() + ySpaceStack;
                    } else if (whichStack < 44) {
                        xToSet = stackToDrop.getLeftSideLocation() + xSpaceStack;
                        yToSet = stackToDrop.getTopSideLocation();
                    }

                    moveCard(currentStack, card, stackToDrop, xToSet, yToSet);
                    recorder.recordStep(card, previousX, previousY, previousStack, previousCanMove);
                    recorded = true;
                    while(currentStack.getCurrentCards().size() != 0 && (currentStack.getStackID() >23 || currentStack.getStackID() < 20)){
                        card = currentStack.getLastCard();
                        previousX = card.getXPosition();
                        previousY = card.getYPosition();
                        previousStack = card.getCurrentStackID();
                        previousCanMove = card.getCanMove();
                        cardImage = card.getImageView();
                        moveCard(currentStack, card, stackToDrop, xToSet, yToSet);
                        recorder.recordStep(card, previousX, previousY, previousStack, previousCanMove);
                    }
                } else {
                    cardImage.setX(card.getXPosition());
                    cardImage.setY(card.getYPosition());
                }
            }
            // Record
            if (!recorded) recorder.recordStep(card, previousX, previousY, previousStack, previousCanMove);

            // Not valid, add card back to where it was
        } else {
            xToSet = card.getXPosition();
            yToSet = card.getYPosition();
            cardImage.setX(xToSet);
            cardImage.setY(yToSet);
        }

        if (winConditionCheck()){
            Snackbar mWinSnackbar = Snackbar.make(cardImage, R.string.Win_Dialog, Snackbar.LENGTH_INDEFINITE);
            mWinSnackbar.show();
        }
    }

    public void moveCard(Stack currentStack, Card card, Stack stackToDrop, float xToSet, float yToSet) {
        ImageView cardImage = card.getImageView();
        cardImage.bringToFront();
        // Remove the card from its stack & and release the following card
        currentStack.removeCardFromStack(card);
        if (currentStack.getCurrentCards().size() != 0) {
            if (currentStack.getStackID() > 23 || currentStack.getStackID() < 20) currentStack.getLastCard().setCanMove(true);
        }else {
            Card tNextCard = nextStack(currentStack).getLastCard();
            if (tNextCard != null) tNextCard.setCanMove(true);
        }
        // Lock the current top card of the target stack & Add card to the stack
        if (stackToDrop.getCurrentCards().size() != 0){
            stackToDrop.getLastCard().setCanMove(false);
        }
        stackToDrop.addCardToStack(card);
        // Set the card's position
        cardImage.setX(xToSet);
        cardImage.setY(yToSet);
        // Overwrite the card's position to the new position
        card.setXYPositions(xToSet, yToSet);
    }

    /**
     * Method called when a touch event has been detected. Switch statement to
     * determine which method to call depending on event.
     *
     * @param v NOT USED
     * @param e The event detected
     * @param c The card that detected the event
     * @return True if the function is called successfully.
     */
    public boolean myTouch(View v, MotionEvent e, Card c) {
        if (c.getCanMove()) {
            v.bringToFront();
        }
        x = e.getRawX();
        y = e.getRawY();
        switch (e.getAction()) {
            // Card pressed
            case MotionEvent.ACTION_DOWN:
                // Only allow movement if card can be moved
                if (c.getCanMove()) {
                    actionDown(v, e, c);
                }
                break;
            // Card moving
            case MotionEvent.ACTION_MOVE:
                // Only allow movement if card can be moved
                if (c.getCanMove()) {
                    actionMove(c.getImageView());
                }
                break;
            // Card dropped
            case MotionEvent.ACTION_UP:
                // Only allow movement if card can be moved
                if (c.getCanMove()) {
                    actionUp(c, x, y);
                }
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * This method simply checks whether the stack being used CAN or CANNOT stack cards.
     * It does NOT check whether the stacking is valid.
     *
     * @param whichStack The stack being used when the card is dropped.
     * @param stackID    The stack that the card belonged to before the move.
     * @return
     */
    private static boolean canStack(int whichStack, int stackID) {
//        Log.d("", "" + whichStack);
        // If same stack, return false
        if (whichStack == stackID) {
            return false;
        }
        // whichStack = -1 means invalid
        if (whichStack < 0) {
            return false;
        }
        // Check if the stack has cards
        boolean haveCards;
        if (stacks[whichStack].getCurrentCards().size() == 0) {
            haveCards = false;
        } else {
            haveCards = true;
        }

        // If first column on the left, can only stack if there are cards
        if (whichStack < 4) {
            if (haveCards) {
                return true;
            } else {
                return false;
            }

            // If on the left side of the board, if have cards, have to check whether the stacks
            // to the left are empty. Checks for every stack to the left
        } else if (whichStack < 16) {
            if (haveCards) {
                for (int i = 1; i <= whichStack / 4; i++) {
                    if (stacks[(whichStack - 4 * i)].getCurrentCards().size() != 0) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }

            // If on the left side of the board, have to check whether the stacks
            // to the left are empty. Checks for every stack to the left. These do
            // not need to check if have cards. Can be stacked regardless
        } else if (whichStack < 20) {
            for (int i = 1; i <= 4; i++) {
                if (stacks[(whichStack - 4 * i)].getCurrentCards().size() != 0) {
                    return false;
                }
            }
            return true;

            // Base stack can alaways stack, just need to check if valid stacking
        } else if (whichStack < 24) {
            return true;

            // If on the right side of the board, have to check whether the stacks
            // to the right are empty. Checks for every stack to the right. These do
            // not need to check if have cards. Can be stacked regardless
        } else if (whichStack < 28) {
            for (int i = 1; i <= 4; i++) {
                if (stacks[(whichStack + 4 * i)].getCurrentCards().size() != 0) {
                    return false;
                }
            }
            return true;

            // If on the rightside of the board, if have cards, have to check whether the stacks
            // to the left are empty. Checks for every stack to the right
        } else if (whichStack < 40) {
            if (haveCards) {
                for (int i = 1; i <= whichStack / 4; i++) {
                    if (whichStack + 4 * i < 44) {
                        if (stacks[(whichStack + 4 * i)].getCurrentCards().size() != 0) {
                            Log.d("", "Stack < 40, stacks to right have cards");
                            return false;
                        }
                    }
                }
                Log.d("", "Stack < 40, stacks to right no cards");
                return true;
            } else {
                return false;
            }
            // If first column on the right, can only stack if there are cards
        } else if (whichStack < 44) {
            if (haveCards) {
                return true;
            } else {
                return false;
            }
        } else {
            // Cellar, can only stack if no cards.
            if (whichStack == 48) {
                if (haveCards) {
                    return false;
                } else {
                    return true;
                }
                // Otherwise, stack cannot stack cards.
            } else {
                return false;
            }
        }
    }

    private static Stack nextStack(Stack currentStack) {
        int stackID = currentStack.getStackID();
        Stack nextStack = new Stack(-1);
        nextStack.addCardToStack(new Card(0, 0));
        // case: left side cards
        if (stackID < 16) {
            nextStack = stacks[stackID + 4];
        // case: right side cards
        } else if (stackID >= 24 && stackID < 44) {
            nextStack = stacks[stackID - 4];
        // case: last row, left side
        } else if (stackID >= 44 && stackID < 48) {
            nextStack = stacks[stackID + 1];
        // case: last row, right side
        } else if (stackID > 48) {
            nextStack = stacks[stackID - 1];
        }
        return nextStack;
    }

    /**
     * Check for win condition. Check whether the base all have 13 cards
     *
     * @return True if base are filled. False otherwise.
     */
    private static boolean winConditionCheck() {
        for (int i = 20; i < 24; i++) {
            if (stacks[i].getCurrentCards().size() != 13) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine whether the two cards can be stacked together. Same suit + ascending or descending order.
     *
     * @param s1 cell to stack on
     * @param c2 Card to move
     * @return True if can be stacked. False otherwise
     */
    private static boolean compareCards(Stack s1, Card c2) {
        Card c1 = s1.getLastCard();
        if(c1.getCurrentStackID() > 19 && c1.getCurrentStackID() < 24 && s1.getCurrentCards().size() == 1) {
            if (direction == 0) {
                direction = (c2.getNumber() - c1.getNumber());
                solver.setDirection(direction);
            } else {
                return (c1.getSuit() == c2.getSuit()) && ((c2.getNumber() - c1.getNumber()) == direction);
            }
        }
        if (c1.getSuit() == c2.getSuit()) {

            if ((Math.abs(c1.getNumber() - c2.getNumber()) == 1) || (Math.abs(c1.getNumber() - c2.getNumber()) == 12)) {
                return true;
            }
        }
        return false;
    }


    public boolean myDoubleTap(Card c){
        if (c.getCanMove()) {
            c.getImageView().bringToFront();
            for (int i =0; i < 4; i++) {
                int whichStack =  20 + i;
                if(stacks[whichStack].getLastCard().getSuit() == c.getSuit()) {
                    stacks[c.getCurrentStackID()].removeCardFromStack(c);
                    actionUp(c, stacks[whichStack].getLeftSideLocation(), stacks[whichStack].getTopSideLocation());
                }
            }
        }
        return true;
    }


    /**
     * Set up touch for all cards
     *
     * @return none
     * @parem none
     */
    private void enableTouch() {
        for (int i = 0; i < 52; i ++) {
            final int finalI = i;
            cards[finalI].getImageView().setOnTouchListener(new View.OnTouchListener() {
                // Double Tap Control Feature
                private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        Log.d("TEST", "onDoubleTap");

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                myDoubleTap(cards[finalI]);
                            }
                        }, 100);
                        return true;
                    }
                });

                // Drag and Drop
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return myTouch(v, event, cards[finalI]);
                }
            });
        }
        View.OnClickListener hintButton = new View.OnClickListener() {
            public void onClick(View v) {
                cards[0].getImageView().setColorFilter(Color.argb(50, 0, 0, 0));
            }
        };
    }

    private int calculateWhichStack(float x, int row){
        // helper that calculate possible stack the player want to move card to
        float leftHalf = stacks[20].getLeftSideLocation();
        float rightHalf = stacks[24].getLeftSideLocation();
        int res = -1;
        if (x < leftHalf){
            if (row<4){
                res = 16 + row;
                for (int i=0; i<=4; i++){
                    if (stacks[i*4+row].getLastCard()!=null){
                        res = i*4 + row;
                        break;
                    }
                }
            }
        } else if (x > rightHalf){
            if (row<4){
                res = 24 + row;
                for (int i=4; i>=0; i--){
                    if (stacks[i*4+row+24].getLastCard()!=null){
                        res = i*4 + row + 24;
                        break;
                    }
                }
            }
        } else {
            // middle stack
            if (row < 4){
                res = 20 + row;
            } else {
                // Redundant
                res = 48;
            }
        }
        return res;
    }
}