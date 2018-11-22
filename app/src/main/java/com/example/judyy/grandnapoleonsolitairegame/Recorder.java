package com.example.judyy.grandnapoleonsolitairegame;

import java.util.Stack;

public class Recorder {
    private Card[] mCards;
    private com.example.judyy.grandnapoleonsolitairegame.Stack[] mStacks;;
    private Stack<OneStep> mRecord;



    public Recorder(GameActivity gameActivity){
        mCards = gameActivity.cards;
        mStacks = gameActivity.stacks;
        mRecord = new Stack<>();
    }

    class OneStep{
        public float previousX, previousY;
        public Card previousCard;
        public int previousStack;
        public boolean previousCanMove;

        OneStep(Card prevCard, float prevX, float prevY, int prevStack, boolean prevCanMove){
            previousCard = prevCard;
            previousX = prevX;
            previousY = prevY;
            previousStack = prevStack;
            previousCanMove = prevCanMove;
        }

        public void undo(){
            int removeStackId = previousCard.getCurrentStackID();
            mStacks[removeStackId].removeCardFromStack(previousCard);
            if (mStacks[removeStackId].getLastCard() != null && (removeStackId<20 || removeStackId>23)){
                mStacks[removeStackId].getLastCard().setCanMove(true);
            }
            if (mStacks[previousStack].getFirstCard() != null){
                mStacks[previousStack].getFirstCard().setCanMove(false);
            } else {
                int innerStackId = findInnerStack(previousStack);
                if (innerStackId != 99){
                    mStacks[innerStackId].getLastCard().setCanMove(false);
                }
                previousX = mStacks[previousStack].getLeftSideLocation();
                previousY = mStacks[previousStack].getTopSideLocation();
            }
            mStacks[previousStack].addCardToStack(previousCard);
            previousCard.setXYPositions(previousX, previousY);
            previousCard.getImageView().setX(previousX);
            previousCard.getImageView().setY(previousY);
            previousCard.getImageView().bringToFront();
            previousCard.setCanMove(true);
        }
    }

    public void undoOneStep(){
        if (!mRecord.empty()){
            OneStep undoStep = mRecord.pop();
            undoStep.undo();
        }
    }

    public void recordStep(Card card){
        // deprecated
        float previousX = card.getXPosition();
        float previousY = card.getYPosition();
        int previousStack = card.getCurrentStackID();
        boolean previousCanMove = card.getCanMove();
        OneStep newStep = new OneStep(card, previousX, previousY, previousStack, previousCanMove);
        mRecord.push(newStep);
    }

    public void recordStep(Card card, float previousX, float previousY, int previousStack, boolean previousCanMove){
        OneStep newStep = new OneStep(card, previousX, previousY, previousStack, previousCanMove);
        mRecord.push(newStep);
    }

    public int findInnerStack(int index){
        if (index < 16) return index + 4;
        if (index > 27 && index < 44) return index - 4;
        if (index > 43 && index <48) return index + 1;
        if (index > 48 && index < 53) return index - 1;
        return 99;
    }
}
