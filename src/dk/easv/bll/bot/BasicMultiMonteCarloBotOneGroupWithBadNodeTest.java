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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author janvanzetten
 */
public class BasicMultiMonteCarloBotOneGroupWithBadNodeTest implements IBot
{

    private List<Integer[]> results; //int[0] is total tries int[1] is score 1 for each win 0 for each draw and -1 for each lose
    private List<IMove> myMoves;
    private int MAX_TIME_FOR_SEARCHING;
    private IGameState currentState;
    private int searches = 0;

    @Override
    public IMove doMove(IGameState state)
    {
        int player = state.getMoveNumber() % 2;

        currentState = new GameState(state);
        MAX_TIME_FOR_SEARCHING = (currentState.getTimePerMove() - 5);
        myMoves = state.getField().getAvailableMoves();

        results = new ArrayList<>();

        fillResults(player, state.getField(), myMoves);

        return getBestMove(state);
    }

    /**
     * get the best moves when comparing tries and wins for every posibble
     * posiition, if more have the same result then pick random from the best
     * results
     *
     * @param state
     * @return
     */
    private IMove getBestMove(IGameState state)
    {
        List<Integer> bestMoves = new ArrayList<>();
        double bestResult = ((results.get(0)[1] * 1.0) / (results.get(0)[0] * 1.0));
        bestMoves.add(0);

        for (int i = 1; i < results.size(); i++)
        {
            double thisResult = ((results.get(i)[1] * 1.0) / (results.get(i)[0] * 1.0));
            //better
            if (thisResult > bestResult)
            {
                bestMoves.clear();
                bestMoves.add(i);
                bestResult = thisResult;
            } //the same
            else if (thisResult == bestResult)
            {
                bestMoves.add(i);
            }
        }
        
        return myMoves.get(bestMoves.get(selectRandom(bestMoves.size())));
    }

    /**
     * get a random number from 0 to max(exclusive)
     *
     * @param max int
     * @return int
     */
    private int selectRandom(int max)
    {
        Random random = new Random();
        return random.nextInt(max);
    }

    /**
     * fills the result list with wins losses and ties for some random games
     *
     * @param player
     * @param field
     * @param myMoves
     */
    private void fillResults(int player, IField field, List<IMove> myMoves) {
        searches = 0;
        List<Task> tasks = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        tasks.clear();

        for (int i = 0; i < myMoves.size(); i++)
        {
            tasks.add(makeTask(i, player, startTime));
        }

 
        for (Task task : tasks)
        {
            new Thread(task).start();
        }
        
        results = new ArrayList<>(tasks.size());
        for (int j = 0; j < tasks.size(); j++)
        {
            try
            {

                Integer[] result = (Integer[]) tasks.get(j).get();

                try
                {
                    results.set(j, result);
                }
                catch (IndexOutOfBoundsException ex)
                {
                    results.add(j, result);
                }

            }
            catch (InterruptedException | ExecutionException ex)
            {
                Logger.getLogger(BasicMultiMonteCarloBotOneGroupWithBadNodeTest.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        //System.out.println(searches + " random multi searches where made");

    }

    @Override
    public String getBotName()
    {
        return "Waste schredder Multi 2000+";
    }

    /**
     * Make a task object which has i as the for the myMoves indeks
     *
     * @param i
     * @param player1
     * @return
     */
    private Task<Integer[]> makeTask(int i, int player1, long startTime)
    {
        final int myIndex = i;
        final int player = player1;
        final int MINIMUM_SEARCH = 50;
        Task<Integer[]> task;
        task = new Task<Integer[]>()
        {
            @Override
            protected Integer[] call() throws Exception
            {

                Integer[] result;
                try
                {
                    result = results.get(myIndex);
                }
                catch (Exception ex)
                {
                    result = new Integer[2];
                    result[0] = 0;
                    result[1] = 0;
                }
                
                int counter = 0;
                
                while (System.currentTimeMillis() < (startTime + MAX_TIME_FOR_SEARCHING))
                {
                    //test if bad node
                    if (counter > MINIMUM_SEARCH && result[0] < 0){
                        break;
                    }
                    
                    IMove testMove = myMoves.get(myIndex);

                    GameManager testGameManager = new GameManager(new GameState(currentState));

                    testGameManager.UpdateGame(testMove);

                    //while game is not gameover take a random move of the avalible moves
                    while (testGameManager.getGameOver() == GameManager.GameOverState.Active)
                    {
                        List<IMove> avalibleMoves = testGameManager.getCurrentState().getField().getAvailableMoves();

                        IMove chossenMove = avalibleMoves.get(selectRandom(avalibleMoves.size()));

                        testGameManager.UpdateGame(chossenMove);

                    }

                    result[0]++;

                    if (testGameManager.getGameOver() != GameManager.GameOverState.Tie) {
                        if ((((testGameManager.getCurrentState().getMoveNumber() + 1) % 2)) == player) {
                            result[1]++;
                        }
                        else
                        {
                            result[1]--;
                        }
                    }
                }
                
                return result;
            }
        };
        
        return task;
    }

}
