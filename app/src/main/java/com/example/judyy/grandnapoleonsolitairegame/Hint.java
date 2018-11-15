package com.example.judyy.grandnapoleonsolitairegame;

public class Hint {
    int originStack;
    int destinationStack;

    public Hint(int org, int dest){
        originStack = org;
        destinationStack = dest;
    }

    public int getOrigin(){
        return originStack;
    }

    public int getDestination(){
        return destinationStack;
    }
}
