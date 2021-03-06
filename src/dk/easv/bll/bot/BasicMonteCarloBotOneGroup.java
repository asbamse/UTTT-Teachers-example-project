/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author janvanzetten
 */
public class BasicMonteCarloBotOneGroup implements IBot {
    
    private List<Integer[]> results; //int[0] is total tries int[1] is score 1 for each win 0 for each draw and -1 for each lose
    private List<IMove> myMoves;
    private int MAX_TIME_FOR_SEARCHING;
    private IGameState currentState;
    //private int searches = 0;
    
    
    @Override
    public IMove doMove(IGameState state) {
        int player = state.getMoveNumber() % 2;
        
        currentState = new GameState(state);
        MAX_TIME_FOR_SEARCHING = (currentState.getTimePerMove() - 10);
        myMoves = state.getField().getAvailableMoves();
        
        results = new ArrayList<>(myMoves.size());
        
        for (int i = 0; i < 81; i++) {
            results.add(new Integer[2]);
        }
        
        fillResults(player, state.getField(), myMoves);
        
        return getBestMove(state);
    }
    
    
    /**
     * get the best moves when comparing tries and wins for every posibble posiition, if more have the same result then pick random from the best results
     * @param state
     * @return 
     */
    private IMove getBestMove(IGameState state) {
        //TODO find the best move from the results list
        List<Integer> bestMoves = new ArrayList<>();
        double bestResult = ((results.get(0)[1]*1.0) / (results.get(0)[0]*1.0));
        bestMoves.add(0);
        
        for (int i = 1; i < results.size(); i++) {
            double thisResult = ((results.get(i)[1]*1.0) / (results.get(i)[0]*1.0));
            //better
            if (thisResult > bestResult) {
                bestMoves.clear();
                bestMoves.add(i);
                bestResult = thisResult;
                //System.out.println("better");
            } //the same
            else if (thisResult == bestResult) {
                bestMoves.add(i);
                //System.out.println("same");
            }
        }
        
        return myMoves.get(bestMoves.get(selectRandom(bestMoves.size())));
    }
    
    
    /**
     * get a random number from 0 to  max(exclusive)
     * @param max int
     * @return int 
     */
    private int selectRandom(int max) {
        Random random = new Random();
        return random.nextInt(max);
    }
    
    
    /**
     * fills the result list with wins losses and ties for some random games
     * @param player
     * @param field
     * @param myMoves 
     */
    private void fillResults(int player, IField field, List<IMove> myMoves) {
        //searches = 0;
        long startTime = System.currentTimeMillis();
        
        int i = 0;
        
        while (System.currentTimeMillis() < (startTime + MAX_TIME_FOR_SEARCHING)) {
            if (i >= myMoves.size()) {
                i = 0;
            }
            //System.out.println(results.size() + "r"  + myMoves.size());
            IMove testMove = myMoves.get(i);

            GameManager testGameManager = new GameManager(new GameState(currentState));
            
            testGameManager.UpdateGame(testMove);

            //while game is not gameover take a random move of the avalible moves
            while (testGameManager.getGameOver() == GameManager.GameOverState.Active) {
                List<IMove> avalibleMoves = testGameManager.getCurrentState().getField().getAvailableMoves();
                
                IMove chossenMove = avalibleMoves.get(selectRandom(avalibleMoves.size()));
                
                testGameManager.UpdateGame(chossenMove);
                
                
            }
            Integer[] result;
            try {
                result = results.get(i);
            } catch (Exception ex) {
                result = new Integer[2];
                result[0] = 0;
                result[1] = 0;
            }
            
            result[0]++;
            
            if (testGameManager.getGameOver() != GameManager.GameOverState.Tie) {
                if ((((testGameManager.getCurrentState().getMoveNumber()+1) % 2))  == player) {  //check this i am not sure it this is right
                    result[1]++;
                } else {
                    result[1]--;
                }
            }
            try {
                results.set(i, result);
            } catch (Exception ex) {
                results.add(i, result);
            }

            //counter
            i++;
            if (i >= myMoves.size()) {
                i = 0;
            }
            //searches++;
        }
        //System.out.println( " random searches where made");
    }
    
    @Override
    public String getBotName() {
        return "Waste schredder";
    }
    
}
