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
            mStacks[previousCard.getCurrentStackID()].removeCardFromStack(previousCard);
            mStacks[previousStack].addCardToStack(previousCard);
            previousCard.setXYPositions(previousX, previousY);
            previousCard.getImageView().setX(previousX);
            previousCard.getImageView().setY(previousY);
//            previousCard.setCanMove(previousCanMove); //This cause problem after under the stack previous can now move
        }
    }

    public void undoOneStep(){
        if (!mRecord.empty()){
            OneStep undoStep = mRecord.pop();
            undoStep.undo();
        }
    }

    public void recordStep(Card card){
        float previousX = card.getXPosition();
        float previousY = card.getYPosition();
        int previousStack = card.getCurrentStackID();
        boolean previousCanMove = card.getCanMove();
        OneStep newStep = new OneStep(card, previousX, previousY, previousStack, previousCanMove);
        mRecord.push(newStep);
    }
}
