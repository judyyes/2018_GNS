package com.example.judyy.grandnapoleonsolitairegame;



import android.content.Context;
import android.graphics.Color;
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
    private static boolean baseStackOrder = false;

    // Input from GameActivity
    private static Card[] cards;
    private static Stack[] stacks;

    // Variables for Step Counter
    public static TextView stepCounter;
    public static int numSteps = 0;

    // Variables for Undo
    private static Button undoButton;
    public static float previousX, previousY;
    public static Card previousCard;
    public static int previousStack;
    public static boolean previousCanMove;


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
    public void main(Context context, Card[] c, Stack[] s) {
        // Assignment for variables used in Drag and Drop
        cards = c;
        stacks = s;
        numSteps = 0;   // Reset numSteps to 0
        // Get size of stack ImageView
        stackHeight = stacks[0].getHeight();    // Set stack height
        stackWidth = stacks[0].getWidth();      // Set stack width
        baseStackOrder = false;
        this.c = context;

        // Enable touch for cards
        enableTouch();

        // Set stacking order of all stacks to both order (ascending + descending)
        for (int i = 0; i < s.length; i++) {
            s[i].setStackingOrder(1);
        }
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
        stacks[c.getCurrentStackID()].removeCardFromStack(c);
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
        float xSpaceStack = Math.abs(stacks[0].getLeftSideLocation() + stacks[0].getWidth() - stacks[4].getLeftSideLocation());
        float ySpaceStack = Math.abs(stacks[0].getTopSideLocation() + stacks[0].getHeight() - stacks[1].getTopSideLocation());
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

        //Log.d("", "tempX " + tempX + " tempY " + tempY + " whichStack " + whichStack);
        boolean validStack = canStack(whichStack, card.getCurrentStackID());   // Check if the stack can be stacked.

        // Location where the card's ImageView should be set to
        float xToSet = 0;
        float yToSet = 0;

        // Check if target stack is valid for putting cards on
        if (validStack) {
            // Special case. Card is dropped in cellar.
            if (whichStack == 48 && stacks[whichStack].getCurrentCards().size() == 0) {
                // Undo button variables assignment
                previousCard = card;
                previousX = card.getXPosition();
                previousY = card.getYPosition();
                previousStack = card.getCurrentStackID();
                previousCanMove = card.getCanMove();

                // Add the card to the cellar
                stacks[whichStack].addCardToStack(card);

                // Assign the location to be set for the card
                xToSet = stacks[whichStack].getLeftSideLocation();
                yToSet = stacks[whichStack].getTopSideLocation();

                // Set the card's position
                cardImage.setX(xToSet);
                cardImage.setY(yToSet);

                // Overwrite the card's position to the new position
                card.setXYPositions(xToSet, yToSet);


                // Unlock the cards that must be unlocked after the move
                cardMoveCheck(previousStack);

                // Lock card
                if (stacks[47].getCurrentCards().size() > 0 && stacks[49].getCurrentCards().size() > 0  ){
                    card.setCanMove(false);
                }

                // Special case. Stack is 16 to 19, or 24 to 27 and the stack is empty
            } else if (((whichStack >= 16 && whichStack < 20) || (whichStack >= 24 && whichStack < 28)) && stacks[whichStack].getCurrentCards().size() == 0) {
                // Undo button variables assignment
                previousCard = card;
                previousX = card.getXPosition();
                previousY = card.getYPosition();
                previousStack = card.getCurrentStackID();
                previousCanMove = card.getCanMove();

                // Add the card to the stack
                stacks[whichStack].addCardToStack(card);

                // Assign the location to be set for the card
                xToSet = stacks[whichStack].getLeftSideLocation();
                yToSet = stacks[whichStack].getTopSideLocation();

                // Set the card's position
                cardImage.setX(xToSet);
                cardImage.setY(yToSet);

                // Overwrite the card's position to the new position
                card.setXYPositions(xToSet, yToSet);


                // Unlock the cards that must be unlocked after the move.
                cardMoveCheck(previousStack);

                // Any other case
            } else {
                Card stackCard = stacks[whichStack].getLastCard();  // Get the card to be stacked on top of by the card dropped by user
//                Log.d("", "Stack is valid");
                // Compare whether the two cards can be stacked
                if (compareCards(stackCard, card)) {
//                    Log.d("", "Two cards can be stacked");
                    // Undo button variables assignment
                    previousCard = card;
                    previousX = card.getXPosition();
                    previousY = card.getYPosition();
                    previousStack = card.getCurrentStackID();
                    previousCanMove = card.getCanMove();

                    // If the stack has no cards, assign the location to be set for the card to simply the stack
                    if (stacks[whichStack].getCurrentCards().size() == 0) {
                        xToSet = stacks[whichStack].getLeftSideLocation();
                        yToSet = stacks[whichStack].getTopSideLocation();
                    } else {
                        // On the left side of the board, set offset to the left
                        if (whichStack < 20) {
                            xToSet = stacks[whichStack].getLeftSideLocation() - xSpaceStack;
                            yToSet = stacks[whichStack].getTopSideLocation();
                            // In the base stacks, set offset to below
                        } else if (whichStack < 24) {
                            int difference = stackCard.getNumber() - card.getNumber();  // Calculate the number difference between the two cards
                            // Set the stacking order of all base if it has NOT been set yet
                            if (!baseStackOrder) {
                                // -12 or 1 means descending
                                if (difference == -12 || difference == 1) {
                                    stacks[20].setStackingOrder(0);
                                    stacks[21].setStackingOrder(0);
                                    stacks[22].setStackingOrder(0);
                                    stacks[23].setStackingOrder(0);
                                    // 12 or -1 means ascending
                                } else if (difference == 12 || difference == -1) {
                                    stacks[20].setStackingOrder(2);
                                    stacks[21].setStackingOrder(2);
                                    stacks[22].setStackingOrder(2);
                                    stacks[23].setStackingOrder(2);
                                }
                                // Stacking order has been set for base
                                baseStackOrder = true;
                            }
                            // Check if stacking order is valid. Set the position to be set.
                            if ((difference == -12) || (difference == 1) && stacks[whichStack].getStackingOrder() == 0) {
                                xToSet = stacks[whichStack].getLeftSideLocation();
                                yToSet = stacks[whichStack].getTopSideLocation() + ySpaceStack;
                            } else if ((difference == 12) || (difference == -1) && stacks[whichStack].getStackingOrder() == 2) {
                                xToSet = stacks[whichStack].getLeftSideLocation();
                                yToSet = stacks[whichStack].getTopSideLocation() + ySpaceStack;
                            } else {    // Not valid, reset the card's position to where it should go
                                xToSet = card.getXPosition();
                                yToSet = card.getYPosition();
                                whichStack = card.getCurrentStackID();
                            }
                            // Right side of the board, set offset to the right
                        } else if (whichStack < 44) {
                            xToSet = stacks[whichStack].getLeftSideLocation() + xSpaceStack;
                            yToSet = stacks[whichStack].getTopSideLocation();
                        }
                    }
                    // Add card to the stack
                    stacks[whichStack].addCardToStack(card);

                    // Set the card's position
                    cardImage.setX(xToSet);
                    cardImage.setY(yToSet);
//                    Log.d("", "X and Y are set to " + xToSet + " " + yToSet);

                    // Overwrite the card's position to the new position
                    card.setXYPositions(xToSet, yToSet);


                    // Unlock the cards that must be unlocked after the move
                    cardMoveCheck(previousStack);
                    // Not valid, add card back to where it was
                } else {
                    stacks[card.getCurrentStackID()].addCardToStack(card);
                    cardImage.setX(card.getXPosition());
                    cardImage.setY(card.getYPosition());
                }
            }
            // Not valid, add card back to where it was
        } else {
            xToSet = card.getXPosition();
            yToSet = card.getYPosition();
            stacks[cardID].addCardToStack(card);
            cardImage.setX(xToSet);
            cardImage.setY(yToSet);
        }

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
        v.bringToFront();
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
            for (int i = 1; i <= whichStack / 4; i++) {
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
            for (int i = 1; i <= whichStack / 4; i++) {
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

    /**
     * Unlock the cards that should be unlocked by the move.
     * This is called every time a valid move was done by the user.
     *
     * @param stackID ID of the card that was moved
     */
    private static void cardMoveCheck(int stackID) {
        // Simply unlock the card to the right if stack < 16
        if (stackID < 16) {
            for (int i = 0; i < stacks[stackID + 4].getCurrentCards().size(); i++) {
                stacks[stackID + 4].getCurrentCards().get(i).setCanMove(true);
            }
            // Simply unlock the card to the left if stack >= 24 and < 44
        } else if (stackID >= 24 && stackID < 44) {
            for (int i = 0; i < stacks[stackID - 4].getCurrentCards().size(); i++) {
                stacks[stackID - 4].getCurrentCards().get(i).setCanMove(true);
            }
            // Simply unlock the card to the right
        } else if (stackID >= 44 && stackID < 48) {
            for (int i = 0; i < stacks[stackID + 1].getCurrentCards().size(); i++) {
                stacks[stackID + 1].getCurrentCards().get(i).setCanMove(true);
            }
            // Simply unlock the card to the left
        } else if (stackID > 48) {
            for (int i = 0; i < stacks[stackID - 1].getCurrentCards().size(); i++) {
                stacks[stackID - 1].getCurrentCards().get(i).setCanMove(true);
            }
        }
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
     * @param c1 Card 1
     * @param c2 Card 2
     * @return True if can be stacked. False otherwise
     */
    private static boolean compareCards(Card c1, Card c2) {
        if (c1.getSuit() == c2.getSuit()) {
            if ((Math.abs(c1.getNumber() - c2.getNumber()) == 1) || (Math.abs(c1.getNumber() - c2.getNumber()) == 12)) {
                return true;
            }
        }
        return false;
    }

    private static void setUpUndo() {
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousCard != null) {
                    Log.d("UNDO CALLLED --------", "" + previousCard.getXPosition());
                    Log.d("UNDO CALLLED --------", "" + previousX);
                    Log.d("UNDO CALLLED --------", "" + previousCard.getYPosition());
                    Log.d("UNDO CALLLED --------", "" + previousY);
                    Log.d("UNDO CALLLED --------", "" + previousCard.getCurrentStackID());
                    Log.d("UNDO CALLLED --------", "" + previousStack);

                    // Check if the card can be moved back
                    if ((previousCard.getXPosition() != previousX && previousCard.getCurrentStackID() != previousStack) || (previousCard.getYPosition() != previousY && previousCard.getCurrentStackID() != previousStack)) {
                        Boolean currentState = true;
                        if (previousCard.getYPosition() == previousY && previousCard.getCurrentStackID() != previousStack) {
                            currentState = false;
                        }
//                        Log.d("", "Undo-ing");
                        // Undo the previous move
                        int id = previousCard.getCurrentStackID();
                        stacks[previousCard.getCurrentStackID()].removeCardFromStack(previousCard);
                        stacks[previousStack].addCardToStack(previousCard);
                        previousCard.setXYPositions(previousX, previousY);
                        previousCard.getImageView().setX(previousX);
                        previousCard.getImageView().setY(previousY);
                        previousCard.setCanMove(previousCanMove); //This cause problem after under the stack previous can now move
                        if (stacks[id] != null && id != 48) {
                            stacks[id].getFirstCard().setCanMove(currentState);
                        }

                        previousCard = null;
                    }
                } else {
                    Log.d("", "Cannot undo");
                }
            }
        });
    }


    public boolean myDoubleTap(Card c){
        if (c.getCanMove()) {
            for (int i =0; i < 4; i++) {
                int whichStack =  20 + i;
                actionUp(c, stacks[whichStack].getLeftSideLocation(), stacks[whichStack].getTopSideLocation());
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
        // TODO: Only let outside cards move. All cards can be moved right now.
        cards[0].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[0]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[0]);
            }
        });
        cards[1].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[1]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[1]);
            }
        });
        cards[2].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[2]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[2]);
            }
        });
        cards[3].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[3]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[3]);
            }
        });
        cards[4].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[4]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[4]);
            }
        });
        cards[5].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[5]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[5]);
            }
        });
        cards[6].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[6]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[6]);
            }
        });
        cards[7].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[7]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[7]);
            }
        });
        cards[8].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[8]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[8]);
            }
        });
        cards[9].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[9]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[9]);
            }
        });
        cards[10].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[10]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[10]);
            }
        });
        cards[11].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[11]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[11]);
            }
        });
        cards[12].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[12]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[12]);
            }
        });
        cards[13].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[13]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[13]);
            }
        });
        cards[14].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[14]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[14]);
            }
        });
        cards[15].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[15]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[15]);
            }
        });
        cards[16].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[16]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[16]);
            }
        });
        cards[17].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[17]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[17]);
            }
        });
        cards[18].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[18]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[18]);
            }
        });
        cards[19].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[19]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[19]);
            }
        });
        cards[24].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[24]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[24]);
            }
        });
        cards[25].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[25]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[25]);
            }
        });
        cards[26].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[26]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[26]);
            }
        });
        cards[27].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[27]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[27]);
            }
        });
        cards[28].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[28]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[28]);
            }
        });
        cards[29].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[29]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[29]);
            }
        });
        cards[30].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[30]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[30]);
            }
        });
        cards[31].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[31]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[31]);
            }
        });
        cards[32].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[32]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[32]);
            }
        });
        cards[33].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[33]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[33]);
            }
        });
        cards[34].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[34]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[34]);
            }
        });
        cards[35].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[35]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[35]);
            }
        });
        cards[36].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[36]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[36]);
            }
        });
        cards[37].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[37]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[37]);
            }
        });
        cards[38].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[38]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[38]);
            }
        });
        cards[39].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[39]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[39]);
            }
        });
        cards[40].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[40]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[40]);
            }
        });
        cards[41].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[41]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[41]);
            }
        });
        cards[42].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[42]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[42]);
            }
        });
        cards[43].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return myDoubleTap(cards[43]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[43]);
            }
        });
        cards[44].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[44]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[44]);
            }
        });
        cards[45].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[45]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[45]);
            }
        });
        cards[46].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[46]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[46]);
            }
        });
        cards[47].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[47]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[47]);
            }
        });
        cards[48].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[48]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[48]);
            }
        });
        cards[49].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[49]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[49]);
            }
        });
        cards[50].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[50]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[50]);
            }
        });
        cards[51].getImageView().setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return myDoubleTap(cards[51]);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return myTouch(v, event, cards[51]);
            }
        });
    }
}