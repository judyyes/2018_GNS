package com.example.judyy.grandnapoleonsolitairegame;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class HintSolver {
    int hintDirection = 0;
    int nextDiamond;
    int nextClub;
    int nextHeart;
    int nextSpade;
    Stack<Integer> cardOfInterest;
    int stackToBuild;
    DragDrop mDragDrop;
    final List<Integer> emptyStacks = Arrays.asList(16,17,18,19,24,25,26,27,48);

    private static com.example.judyy.grandnapoleonsolitairegame.Stack[] stacks;

    public HintSolver(GameActivity gameActivity, DragDrop dd){
        mDragDrop = dd;
        stacks = gameActivity.stacks;
    }

    public void init(){
        if (mDragDrop.direction != 0){
            hintDirection = mDragDrop.direction;
        } else {
            hintDirection = guessDirection(mDragDrop);
        }
//        nextDiamond = calculateCost(1, stacks[20].getLastCard().getNumber() + hintDirection);
//        nextClub = calculateCost(2, stacks[21].getLastCard().getNumber() + hintDirection);
//        nextHeart = calculateCost(3, stacks[22].getLastCard().getNumber() + hintDirection);
//        nextSpade = calculateCost(4, stacks[23].getLastCard().getNumber() + hintDirection);
    }

    public Hint requesetHint(){
        // optimize the algorithm to be more intuitive nah?
//        while (!cardOfInterest.isEmpty()){
//            int i = cardOfInterest.pop();
//        }
        return requestHint(true);
    }

    public Hint requestHint(boolean needInitialize){

        if (needInitialize){
            init();
//            int[] array = {nextDiamond, nextClub, nextHeart, nextSpade};


            }
        int emptyStackCnt = countEmptyStack();
        for (int i=0; i<4; i++){
            int nextCardStack = findCard(i+1, stacks[i+20].getLastCard().getNumber() + hintDirection);
            Hint mHint = solve(nextCardStack, emptyStackCnt, 0);
            if (mHint != null){
                return mHint;
            }

//            Arrays.sort(array);


//            int minimumCost = array[0];
//            if (minimumCost == nextDiamond){
//                stackToBuild = 20;
//            } else if (minimumCost == nextClub){
//                stackToBuild = 21;
//            } else if (minimumCost == nextHeart){
//                stackToBuild = 22;
//            } else {
//                stackToBuild = 23;
//            }
        }
        return null;
    }

    public int findOuterStack(int index){
        switch (index){
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return index - 4;
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
                return index + 4;
            case 45:
            case 46:
            case 47:
                return index - 1;
            case 48:
                if (costHelper(47) < costHelper(49)){
                    return 47;
                } else{
                    return 49;
                }
            case 49:
            case 50:
            case 51:
                return index + 1;
            default:
                return 99;
        }
    }

    private Hint solve(int stackNum, int availableStack, int cumulativeCost){
        Card card = stacks[stackNum].getLastCard();
        int cardSuit = card.getSuit();
        int cardNum = card.getNumber();
        int nextCost = calculateCost(cardSuit, cardNum+hintDirection);
        int outerStack = findOuterStack(stackNum);
        if (nextCost==0){

            if (outerStack==99 || stacks[outerStack].getLastCard()==null){ // The card can be moved
                return new Hint(stackNum, findCard(cardSuit, cardNum+hintDirection));
            } else { // Try to move outer card
                return solve(outerStack, availableStack, cumulativeCost);
            }

        } else{
            if (availableStack==0 || nextCost+cumulativeCost>=99){ // Cannot solve
                return null;
            } else if(outerStack==99 || stacks[outerStack].getLastCard()==null){ // Move to empty stack
                return new Hint(stackNum, getEmptyStack());
            }
            return solve(findOuterStack(stackNum), availableStack-1, nextCost+cumulativeCost); // Try to solve recursively
        }

    }

    private int countEmptyStack(){
        int count = 0;
        for (int i : emptyStacks){
            if (stacks[i].getLastCard()==null){
                count ++;
            }
        }
        return count;
    }

    private int getEmptyStack(){
        for (int i : emptyStacks){
            if (stacks[i].getLastCard()==null){
                return i;
            }
        }
        return 99;
    }



    public int findCard(int cardSuit, int cardNum){
        if (cardNum == 0){ // King come before Ace
            cardNum = 13;
        }
        for (int i=0; i <54; i++){
            Card card = stacks[i].getLastCard();
            if (card==null){
                continue;
            }
            if (card.getSuit()==cardSuit && card.getNumber()==cardNum){
                return i;
            }
        }
        return 99;
    }



    public int calculateCost(int cardSuit, int cardNum){
        int i = findCard(cardSuit, cardNum);
        if (i<99){
            return costHelper(i);
        }
        return 99;
    }


    public int costHelper(int index){
        int cost = 0;
        int row;
        if (index < 20){
            row = index % 4;
            for (int i=0; i<=4; i++){
                int adjustedIndex = i*4+row;
                if (adjustedIndex == index){
                    break;
                }
                if (stacks[adjustedIndex].getLastCard()==null){
                    continue;
                }
                cost ++;
            }
        } else if (index < 24){

        } else if(index < 44){
            row = index % 4;
            for (int i=4; i>=0; i--){
                int adjustedIndex = i*4+row+24;
                if (adjustedIndex == index){
                    break;
                }
                if (stacks[adjustedIndex].getLastCard()==null){
                    continue;
                }
                cost ++;
            }
        } else if (index < 48) {
            for (int i=44; i<48; i++){
                if (i == index){
                    break;
                }
                if (stacks[i].getLastCard()==null){
                    continue;
                }
                cost ++;
            }
        } else if (index > 48) {
            for (int i=52; i>48; i--){
                if (i == index){
                    break;
                }
                if (stacks[i].getLastCard()==null){
                    continue;
                }
                cost ++;
            }
        } else {  // index == 48
            int costLeft= 0;
            int costRight = 0;
            for (int i=44; i<48; i++){
                if (i == index){
                    break;
                }
                if (stacks[i].getLastCard()==null){
                    continue;
                }
                costLeft ++;
            }
            for (int i=52; i>48; i--){
                if (i == index){
                    break;
                }
                if (stacks[i].getLastCard()==null){
                    continue;
                }
                costRight ++;
            }
            cost = Math.min(costLeft, costRight);
        }
        return cost;
    }



    private int guessDirection(DragDrop dd){
        int upwards = 0;
        int downwards = 0;
        int cardNum = stacks[20].getLastCard().getNumber();
        for (int i=1; i<=4; i++){
            upwards += calculateCost(i, cardNum+1);
            downwards += calculateCost(i, cardNum-1);
        }
        if (upwards > downwards){
            return -1;
        } else {
            return 1;
        }
    }
}
