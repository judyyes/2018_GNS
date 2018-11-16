package com.example.judyy.grandnapoleonsolitairegame;

public class Hint {
    int originStack;
    int destinationStack;
    int priority;

    public Hint(int org, int dest, int availableStack){
        originStack = org;
        destinationStack = dest;
        priority = availableStack;
    }

    public int getOrigin(){
        return originStack;
    }

    public int getDestination(){
        return destinationStack;
    }

    public int getPriority(){
        return priority;
    }
}
