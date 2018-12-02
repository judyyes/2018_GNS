package com.example.judyy.grandnapoleonsolitairegame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import java.util.Random;



public class GameActivity extends AppCompatActivity {

    private int[] location = new int[2];
    Stack[] stacks = new Stack[53];
    Card[] cards = new Card[52];
    Context context = this;
    Recorder recorder;
    HintSolver solver;
    public Button pauseButton;
    public Button hintButton;
    public TextView stepCounter;
    public String type = "normal"; // Set default game type to random
    public static int edtStep;
    public static Chronometer edtTime, timer;
    public static Boolean done = false;
    Snackbar mHintSnackbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);

        // Set game difficulty
        Intent intent = getIntent();
        type = intent.getStringExtra(DifficultySelectionActivity.EXTRA_MESSAGE);

        //Initialize recorder
        recorder = new Recorder(this);

        //Initialize Hint Solver
        solver = new HintSolver(this);

        //Display card to table
        displayCards(type, cards, stacks);

        final GameLayout gameLayout = findViewById(R.id.zoom_linear_layout);
        final ImageView zoomToggle = findViewById(R.id.zoom_toggle);
        zoomToggle.setImageResource(R.drawable.zoom_btn);
        zoomToggle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                boolean zooming = gameLayout.toggleZooming();
                if (zooming){
                    // Sort of Blue More like Green color filter
                    zoomToggle.setColorFilter(Color.argb(123, 0, 255, 162));
                } else {
                    zoomToggle.clearColorFilter();
                }
            }
        });

        final ImageView backBtn = findViewById(R.id.back_btn);
        backBtn.setImageResource(R.drawable.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onBackPressed();
            }
        });

        final ImageView undoBtn = findViewById(R.id.undo_btn);
        undoBtn.setImageResource(R.drawable.undo_btn);
        undoBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                recorder.undoOneStep();
            }
        });

        final ImageView hintBtn = findViewById(R.id.hint_btn);
        hintBtn.setImageResource(R.drawable.hint_btn);
        solver.setDirection();
        mHintSnackbar = Snackbar.make(gameLayout, R.string.No_Hint, Snackbar.LENGTH_SHORT);
        hintBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                for (int i=0; i<52; i++){
                    ImageView cardImg = cards[i].getImageView();
                    cardImg.clearColorFilter();
                }
                Hint mHint = solver.requestHint();
                if (mHint == null){
                    // TODO Display dialog
                    Log.d("Hint", "No Hint");
                    mHintSnackbar.show();
                } else {
                    int originCard = mHint.getOrigin();
//                    Card cardToMove = null;
//                    while (cardToMove == null){
                    Card cardToMove = stacks[originCard].getLastCard();
                    int destinationCard = mHint.getDestination();
//                    Card cardToStack = null;
                    Card cardToStack = stacks[destinationCard].getLastCard();
                    if (cardToMove == null){
                        Log.d("Hint", String.valueOf(originCard));

                    } else {
                        ImageView orgImage = cardToMove.getImageView();
                        orgImage.setColorFilter(Color.argb(123, 0, 255, 162));
                    }

                    if (cardToStack == null){
                        Log.d("Hint", String.valueOf(destinationCard));
                    } else {
                        ImageView destImage = cardToStack.getImageView();
                        destImage.setColorFilter(Color.argb(123, 255, 127, 80));
                    }

//                    cardToMove.setOnTouchListener(new View.OnTouchListener(){
//                        @Override
//                        public boolean onTouch(View view, MotionEvent motionEvent) {
//                            cardToMove.clearColorFilter();
//                            cardToMove.setOnTouchListener(null);
//                            return false;
//                        }
//                    });
                }
            }
        });
    }


    /**
     * Display card on the game page.
     *
     * @param type  type of game that user selected 1 - random game or 2 - predetermined game
     * @param stack stack position on the page
     * @return None
     * @params card card that will be added to stack
     */
    public void displayCards(String type, Card[] cards, Stack[] stacks) {
        // Create 53 stacks
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = new Stack(i);
        }
        if (type.equals("normal")) {
            // Randomly pick a number for base, and fill base with those cards in alternating suit color.
            Random rand = new Random();
            int numb = 0;
            while (numb == 0) {
                numb = rand.nextInt(14);
            }
            int suit = 1;
            for (int i = 20; i < 24; i++) {
                cards[i] = new Card(suit, numb);
                suit++;
            }

            // Fill stacks with cards except cellar
            int index = 0;
            int bound = 14;
            if (numb == 13) {
                bound = numb;
            }
            for (suit = 1; suit < 5; suit++) {
                for (int num = 1; num < bound; num++) {
                    if (num == numb) {
                        num++;
                    }
                    if (index == 20) {
                        index = index + 4;
                    }
                    cards[index] = new Card(suit, num);
                    index++;
                }
            }

            // Shuffle Cards
            for (int i = 0; i < cards.length; i++) {
                // Skip base cards
                if (i == 20) {
                    i = i + 4;
                }
                // Generate random number
                Random r = new Random();
                int randomCard = r.nextInt(cards.length);
                // Skip base cards
                while (randomCard > 19 && randomCard < 24) {
                    randomCard = r.nextInt(cards.length);
                }
                // Swap the two selected cards
                Card tempCard = cards[i];
                cards[i] = cards[randomCard];
                cards[randomCard] = tempCard;
            }
        } else if (type.equals("dummy")) {
            // when predetermined selected - by place card into stack associated
            // TODO - Find at least a layout of solving game - Below is just a dummy layout
            cards[0] = new Card(1, 8);
            cards[1] = new Card(2, 8);
            cards[2] = new Card(3, 8);
            cards[3] = new Card(4, 8);
            cards[4] = new Card(1, 7);
            cards[5] = new Card(2, 7);
            cards[6] = new Card(3, 7);
            cards[7] = new Card(4, 7);
            cards[8] = new Card(1, 6);
            cards[9] = new Card(2, 6);
            cards[10] = new Card(3, 6);
            cards[11] = new Card(4, 6);
            cards[12] = new Card(1, 5);
            cards[13] = new Card(2, 5);
            cards[14] = new Card(3, 5);
            cards[15] = new Card(4, 5);
            cards[16] = new Card(1, 4);
            cards[17] = new Card(2, 4);
            cards[18] = new Card(3, 4);
            cards[19] = new Card(4, 4);
            cards[20] = new Card(1, 9);
            cards[21] = new Card(2, 9);
            cards[22] = new Card(3, 9);
            cards[23] = new Card(4, 9);
            cards[24] = new Card(1, 10);
            cards[25] = new Card(2, 10);
            cards[26] = new Card(3, 10);
            cards[27] = new Card(4, 10);
            cards[28] = new Card(1, 11);
            cards[29] = new Card(2, 11);
            cards[30] = new Card(3, 11);
            cards[31] = new Card(4, 11);
            cards[32] = new Card(1, 12);
            cards[33] = new Card(2, 12);
            cards[34] = new Card(3, 12);
            cards[35] = new Card(4, 12);
            cards[36] = new Card(1, 13);
            cards[37] = new Card(2, 13);
            cards[38] = new Card(3, 13);
            cards[39] = new Card(4, 13);
            cards[40] = new Card(1, 3);
            cards[41] = new Card(2, 3);
            cards[42] = new Card(3, 3);
            cards[43] = new Card(4, 3);
            cards[44] = new Card(1, 2);
            cards[45] = new Card(2, 2);
            cards[46] = new Card(3, 2);
            cards[47] = new Card(4, 2);
            cards[48] = new Card(1, 1);
            cards[49] = new Card(2, 1);
            cards[50] = new Card(3, 1);
            cards[51] = new Card(4, 1);
        }

        stacks[0].setImageView((ImageView) findViewById(R.id.stack0));
        stacks[1].setImageView((ImageView) findViewById(R.id.stack1));
        stacks[2].setImageView((ImageView) findViewById(R.id.stack2));
        stacks[3].setImageView((ImageView) findViewById(R.id.stack3));
        stacks[4].setImageView((ImageView) findViewById(R.id.stack4));
        stacks[5].setImageView((ImageView) findViewById(R.id.stack5));
        stacks[6].setImageView((ImageView) findViewById(R.id.stack6));
        stacks[7].setImageView((ImageView) findViewById(R.id.stack7));
        stacks[8].setImageView((ImageView) findViewById(R.id.stack8));
        stacks[9].setImageView((ImageView) findViewById(R.id.stack9));
        stacks[10].setImageView((ImageView) findViewById(R.id.stack10));
        stacks[11].setImageView((ImageView) findViewById(R.id.stack11));
        stacks[12].setImageView((ImageView) findViewById(R.id.stack12));
        stacks[13].setImageView((ImageView) findViewById(R.id.stack13));
        stacks[14].setImageView((ImageView) findViewById(R.id.stack14));
        stacks[15].setImageView((ImageView) findViewById(R.id.stack15));
        stacks[16].setImageView((ImageView) findViewById(R.id.stack16));
        stacks[17].setImageView((ImageView) findViewById(R.id.stack17));
        stacks[18].setImageView((ImageView) findViewById(R.id.stack18));
        stacks[19].setImageView((ImageView) findViewById(R.id.stack19));
        stacks[20].setImageView((ImageView) findViewById(R.id.stack20));
        stacks[21].setImageView((ImageView) findViewById(R.id.stack21));
        stacks[22].setImageView((ImageView) findViewById(R.id.stack22));
        stacks[23].setImageView((ImageView) findViewById(R.id.stack23));
        stacks[24].setImageView((ImageView) findViewById(R.id.stack24));
        stacks[25].setImageView((ImageView) findViewById(R.id.stack25));
        stacks[26].setImageView((ImageView) findViewById(R.id.stack26));
        stacks[27].setImageView((ImageView) findViewById(R.id.stack27));
        stacks[28].setImageView((ImageView) findViewById(R.id.stack28));
        stacks[29].setImageView((ImageView) findViewById(R.id.stack29));
        stacks[30].setImageView((ImageView) findViewById(R.id.stack30));
        stacks[31].setImageView((ImageView) findViewById(R.id.stack31));
        stacks[32].setImageView((ImageView) findViewById(R.id.stack32));
        stacks[33].setImageView((ImageView) findViewById(R.id.stack33));
        stacks[34].setImageView((ImageView) findViewById(R.id.stack34));
        stacks[35].setImageView((ImageView) findViewById(R.id.stack35));
        stacks[36].setImageView((ImageView) findViewById(R.id.stack36));
        stacks[37].setImageView((ImageView) findViewById(R.id.stack37));
        stacks[38].setImageView((ImageView) findViewById(R.id.stack38));
        stacks[39].setImageView((ImageView) findViewById(R.id.stack39));
        stacks[40].setImageView((ImageView) findViewById(R.id.stack40));
        stacks[41].setImageView((ImageView) findViewById(R.id.stack41));
        stacks[42].setImageView((ImageView) findViewById(R.id.stack42));
        stacks[43].setImageView((ImageView) findViewById(R.id.stack43));
        stacks[44].setImageView((ImageView) findViewById(R.id.stack44));
        stacks[45].setImageView((ImageView) findViewById(R.id.stack45));
        stacks[46].setImageView((ImageView) findViewById(R.id.stack46));
        stacks[47].setImageView((ImageView) findViewById(R.id.stack47));
        stacks[48].setImageView((ImageView) findViewById(R.id.stack48));
        stacks[49].setImageView((ImageView) findViewById(R.id.stack49));
        stacks[50].setImageView((ImageView) findViewById(R.id.stack50));
        stacks[51].setImageView((ImageView) findViewById(R.id.stack51));
        stacks[52].setImageView((ImageView) findViewById(R.id.stack52));

        cards[0].setImageView((ImageView) findViewById(R.id.card0));
        cards[1].setImageView((ImageView) findViewById(R.id.card1));
        cards[2].setImageView((ImageView) findViewById(R.id.card2));
        cards[3].setImageView((ImageView) findViewById(R.id.card3));
        cards[4].setImageView((ImageView) findViewById(R.id.card4));
        cards[5].setImageView((ImageView) findViewById(R.id.card5));
        cards[6].setImageView((ImageView) findViewById(R.id.card6));
        cards[7].setImageView((ImageView) findViewById(R.id.card7));
        cards[8].setImageView((ImageView) findViewById(R.id.card8));
        cards[9].setImageView((ImageView) findViewById(R.id.card9));
        cards[10].setImageView((ImageView) findViewById(R.id.card10));
        cards[11].setImageView((ImageView) findViewById(R.id.card11));
        cards[12].setImageView((ImageView) findViewById(R.id.card12));
        cards[13].setImageView((ImageView) findViewById(R.id.card13));
        cards[14].setImageView((ImageView) findViewById(R.id.card14));
        cards[15].setImageView((ImageView) findViewById(R.id.card15));
        cards[16].setImageView((ImageView) findViewById(R.id.card16));
        cards[17].setImageView((ImageView) findViewById(R.id.card17));
        cards[18].setImageView((ImageView) findViewById(R.id.card18));
        cards[19].setImageView((ImageView) findViewById(R.id.card19));
        cards[20].setImageView((ImageView) findViewById(R.id.card20));
        cards[21].setImageView((ImageView) findViewById(R.id.card21));
        cards[22].setImageView((ImageView) findViewById(R.id.card22));
        cards[23].setImageView((ImageView) findViewById(R.id.card23));
        cards[24].setImageView((ImageView) findViewById(R.id.card24));
        cards[25].setImageView((ImageView) findViewById(R.id.card25));
        cards[26].setImageView((ImageView) findViewById(R.id.card26));
        cards[27].setImageView((ImageView) findViewById(R.id.card27));
        cards[28].setImageView((ImageView) findViewById(R.id.card28));
        cards[29].setImageView((ImageView) findViewById(R.id.card29));
        cards[30].setImageView((ImageView) findViewById(R.id.card30));
        cards[31].setImageView((ImageView) findViewById(R.id.card31));
        cards[32].setImageView((ImageView) findViewById(R.id.card32));
        cards[33].setImageView((ImageView) findViewById(R.id.card33));
        cards[34].setImageView((ImageView) findViewById(R.id.card34));
        cards[35].setImageView((ImageView) findViewById(R.id.card35));
        cards[36].setImageView((ImageView) findViewById(R.id.card36));
        cards[37].setImageView((ImageView) findViewById(R.id.card37));
        cards[38].setImageView((ImageView) findViewById(R.id.card38));
        cards[39].setImageView((ImageView) findViewById(R.id.card39));
        cards[40].setImageView((ImageView) findViewById(R.id.card40));
        cards[41].setImageView((ImageView) findViewById(R.id.card41));
        cards[42].setImageView((ImageView) findViewById(R.id.card42));
        cards[43].setImageView((ImageView) findViewById(R.id.card43));
        cards[44].setImageView((ImageView) findViewById(R.id.card44));
        cards[45].setImageView((ImageView) findViewById(R.id.card45));
        cards[46].setImageView((ImageView) findViewById(R.id.card46));
        cards[47].setImageView((ImageView) findViewById(R.id.card47));
        cards[48].setImageView((ImageView) findViewById(R.id.card48));
        cards[49].setImageView((ImageView) findViewById(R.id.card49));
        cards[50].setImageView((ImageView) findViewById(R.id.card50));
        cards[51].setImageView((ImageView) findViewById(R.id.card51));
        for (int i = 0; i < cards.length; i++) {
            if (i < 4 || (i >= 40 && i < 45) || i == 51) {
                cards[i].setCanMove(true);
            } else {
                cards[i].setCanMove(false);
            }
            if (i < 48) {
                stacks[i].addCardToStack(cards[i]);
            } else {
                stacks[i + 1].addCardToStack(cards[i]);
            }
        }
    }

    // Set stack location
    private void setStacksLocation() {
        for (int i = 0; i < stacks.length; i++) {
//            System.out.print("This is good'"+i+"\n");
            stacks[i].getImageView().getLocationOnScreen(location);
            stacks[i].setSize(stacks[i].getImageView().getWidth(), stacks[i].getImageView().getHeight());
            stacks[i].setXYCoordinates(location[0], location[1]);
        }
        for (int i = 0; i < cards.length; i++) {
            int tempID = cards[i].getCurrentStackID();
            cards[i].setXYPositions(stacks[tempID].getLeftSideLocation(), stacks[tempID].getTopSideLocation());
        }
        new DragDrop().main(context, cards, stacks, recorder, solver);

    }

    // Temporary solution to actually finding location of ImageViews.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        setStacksLocation();
    }



}
