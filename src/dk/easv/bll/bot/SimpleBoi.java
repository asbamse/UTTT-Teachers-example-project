package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;

public class SimpleBoi implements IBot
{
    private static final String BOTNAME = "Some Simple Boi";

    private final int SCORE_SEND_EMPTY;
    private final int SCORE_SEND_WANTED_FIELD_EMPTY;
    private final int SCORE_SEND_FINISHED;
    private final int SCORE_SEND_MACRO_LOSE;
    private final int SCORE_SEND_MICRO_LOSE;
    private final int SCORE_BLOCKING_LOSE;
    private final int SCORE_MACRO_WIN;
    private final int SCORE_MICRO_WIN;
    private final int SCORE_TWO_IN_ROW;
    private final int SCORE_FIRST;
    private final int SCORE_SECOND;
    private final int SCORE_THIRD;

    private Random rand = new Random();

    private class PreferredMove
    {
        private final int[][] PREFERRED_POSITION_FIRST =
        {
            {
                1, 1
            }
        };
        private final int[][] PREFERRED_POSITION_SECOND =
        {
            {
                0, 0
            },
            {
                2, 2
            },
            {
                0, 2
            },
            {
                2, 0
            }
        };
        private final int[][] PREFERRED_POSITION_THIRD =
        {
            {
                0, 1
            },
            {
                2, 1
            },
            {
                1, 0
            },
            {
                1, 2
            }
        };

        private int score;
        private IMove move;

        /**
         * Constructor calculates score.
         * @param move
         * @param state
         */
        PreferredMove(IMove move, IGameState state)
        {
            this.score = 0;
            this.move = move;
            int player = state.getMoveNumber() % 2;
            int enemy = ((player + 1) % 2);

            IMove localMove = new Move(move.getX() % 3, move.getY() % 3);
            String[][] microBoard = microBoard((move.getX() / 3) * 3, (move.getY() / 3) * 3, state);
            String[][] nextMicroBoard = microBoard(localMove.getX() * 3, localMove.getY() * 3, state);
            String[][] microBoardWithPlayerMove = microBoard;
            for (int i = 0; i < 3; i++)
            {
                System.arraycopy(microBoard[i], 0, microBoardWithPlayerMove[i], 0, microBoard[i].length);
            }
            microBoardWithPlayerMove[localMove.getX()][localMove.getY()] = player + "";
            String[][] microBoardWithEnemyMove = microBoard;
            for (int i = 0; i < 3; i++)
            {
                System.arraycopy(microBoard[i], 0, microBoardWithEnemyMove[i], 0, microBoard[i].length);
            }
            microBoardWithEnemyMove[localMove.getX()][localMove.getY()] = enemy + "";
            String[][] macroBoard = new String[3][3];
            for (int i = 0; i < 3; i++)
            {
                System.arraycopy(state.getField().getMacroboard()[i], 0, macroBoard[i], 0, state.getField().getMacroboard()[i].length);
            }
            String[][] macroBoardWithEnemyMacroMove = new String[3][3];
            for (int i = 0; i < 3; i++)
            {
                System.arraycopy(macroBoard[i], 0, macroBoardWithEnemyMacroMove[i], 0, macroBoard[i].length);
            }

            if (macroBoardWithEnemyMacroMove[localMove.getX()][localMove.getY()] == null)
            {
                System.out.println("NULL!!!");
            }

            checkPreferredPosition(localMove);

            if (checkIfBoardIsWon(microBoardWithPlayerMove).equalsIgnoreCase(player + ""))
            {
                if (checkIfBoardIsWon(macroBoard).equalsIgnoreCase(player + ""))
                {
                    score += SCORE_MACRO_WIN;
                }
                else
                {
                    score += SCORE_MICRO_WIN;
                }
            }

            if (checkTwoInRow(player + "", microBoardWithPlayerMove))
            {
                score += SCORE_TWO_IN_ROW;
            }

            if (checkTwoInRow(enemy + "", microBoard))
            {
                if (checkIfBoardIsWon(microBoardWithEnemyMove).equals(enemy))
                {
                    score += SCORE_BLOCKING_LOSE;
                }
            }

            if (checkTwoInRow(enemy + "", nextMicroBoard))
            {
                score += SCORE_SEND_MICRO_LOSE;
                if (checkTwoInRow(enemy + "", macroBoardWithEnemyMacroMove))
                {
                    score += SCORE_SEND_MACRO_LOSE;
                }
            }

            if (!(macroBoard[localMove.getX()][localMove.getY()].equals("-1") || macroBoard[localMove.getX()][localMove.getY()].equals(".")))
            {
                score += SCORE_SEND_FINISHED;
            }

            if (isEmpty(nextMicroBoard))
            {
                score += SCORE_SEND_EMPTY;
            }

            List<IMove> emptyFields = getEmptyFields(nextMicroBoard);
            for (IMove emptyField : emptyFields)
            {
                if (checkTwoInRow(player + "", microBoard(emptyField.getX(), emptyField.getY(), state)))
                {
                    score += SCORE_SEND_WANTED_FIELD_EMPTY;
                }
            }
        }

        /**
         * Gets empty fields in 3x3.
         * @param board
         * @return
         */
        private List<IMove> getEmptyFields(String[][] board)
        {
            List<IMove> emptyFields = new ArrayList<>();
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    if (board[i][j].equals("-1") || board[i][j].equals("."))
                    {
                        emptyFields.add(new Move(i, j));
                    }
                }
            }
            return emptyFields;
        }

        /**
         * Check if 3x3 board is empty
         * @param board
         * @return
         */
        private boolean isEmpty(String[][] board)
        {
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    if (!(board[i][j].equals("-1") || board[i][j].equals(".")))
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * Counts two in same row.
         *
         * @param board 3 by 3.
         */
        private boolean checkTwoInRow(String player, String[][] board)
        {
            // Vertical.
            for (int i = 0; i < 3; i++)
            {
                if (board[i][0].equals(player) && board[i][1].equals(player)
                        || board[i][1].equals(player) && board[i][2].equals(player)
                        || board[i][0].equals(player) && board[i][2].equals(player))
                {
                    return true;
                }
            }

            // Horizontal.
            for (int i = 0; i < 3; i++)
            {
                if (board[0][i].equals(player) && board[1][i].equals(player)
                        || board[1][i].equals(player) && board[2][i].equals(player)
                        || board[0][i].equals(player) && board[2][i].equals(player))
                {
                    return true;
                }
            }

            // Diagnoal 1.
            if (board[0][0].equals(player) && board[1][1].equals(player)
                    || board[1][1].equals(player) && board[2][2].equals(player)
                    || board[0][0].equals(player) && board[2][2].equals(player))
            {
                return true;
            }

            // Diagnoal 2.
            if (board[2][0].equals(player) && board[1][1].equals(player)
                    || board[1][1].equals(player) && board[0][2].equals(player)
                    || board[2][0].equals(player) && board[0][2].equals(player))
            {
                return true;
            }

            return false;
        }

        /**
         * Check if a 3 by 3 board is won.
         *
         * @param board 3 by 3.
         * @return the winner if there is any, else returns ".".
         */
        private String checkIfBoardIsWon(String[][] board)
        {
            // Vertical.
            for (int i = 0; i < 3; i++)
            {
                if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]))
                {
                    return board[i][0];
                }
            }

            // Horizontal.
            for (int i = 0; i < 3; i++)
            {
                if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]))
                {
                    return board[0][i];
                }
            }

            // Diagnoal 1.
            if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]))
            {
                return board[0][0];
            }

            // Diagnoal 2.
            if (board[2][0].equals(board[1][1]) && board[1][1].equals(board[0][2]))
            {
                return board[2][0];
            }

            return ".";
        }

        /**
         * Gives point for positioning priority.
         * @param localMove
         */
        private void checkPreferredPosition(IMove localMove)
        {
            for (int i = 0; i < PREFERRED_POSITION_FIRST.length; i++)
            {
                if (PREFERRED_POSITION_FIRST[i][0] == localMove.getX() && PREFERRED_POSITION_FIRST[i][1] == localMove.getY())
                {
                    score += SCORE_FIRST;
                    return;
                }
            }

            for (int i = 0; i < PREFERRED_POSITION_SECOND.length; i++)
            {
                if (PREFERRED_POSITION_SECOND[i][0] == localMove.getX() && PREFERRED_POSITION_SECOND[i][1] == localMove.getY())
                {
                    score += SCORE_SECOND;
                    return;
                }
            }

            for (int i = 0; i < PREFERRED_POSITION_THIRD.length; i++)
            {
                if (PREFERRED_POSITION_THIRD[i][0] == localMove.getX() && PREFERRED_POSITION_THIRD[i][1] == localMove.getY())
                {
                    score += SCORE_THIRD;
                    return;
                }
            }
        }

        /**
         * Creates microboard from x and y coordinate.
         * @param startX
         * @param startY
         * @param state
         * @return
         */
        private String[][] microBoard(int startX, int startY, IGameState state)
        {
            String[][] microBoard = new String[3][3];
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    microBoard[j][i] = state.getField().getBoard()[startX + j][startY + i];
                }
            }
            return microBoard;
        }

        /**
         * Gets score.
         * @return
         */
        public int getScore()
        {
            return score;
        }

        /**
         * Gets IMove.
         * @return
         */
        public IMove getMove()
        {
            return move;
        }

        /**
         * Equals override. Comparing on score.
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final PreferredMove other = (PreferredMove) obj;
            if (this.score != other.score)
            {
                return false;
            }
            return true;
        }
    }

    public SimpleBoi()
    {
        this(1, 2, -6, -10, 4, 8, 2, 2, 1, 1);
    }

    public SimpleBoi(int scoreSendEmpty, int scoreSendWantedFieldEmpty,
            int scoreSendFinished, int scoreSendMicroLose,
            int scoreBlockingLose, int scoreMicroWin,
            int scoreTwoInRow, int scoreFirst, int scoreSecond, int scoreThird)
    {
        SCORE_SEND_EMPTY = scoreSendEmpty;
        SCORE_SEND_WANTED_FIELD_EMPTY = scoreSendWantedFieldEmpty;
        SCORE_SEND_FINISHED = scoreSendFinished;
        SCORE_SEND_MACRO_LOSE = -1000;
        SCORE_SEND_MICRO_LOSE = scoreSendMicroLose;
        SCORE_BLOCKING_LOSE = scoreBlockingLose;
        SCORE_MACRO_WIN = 5000;
        SCORE_MICRO_WIN = scoreMicroWin;
        SCORE_TWO_IN_ROW = scoreTwoInRow;
        SCORE_FIRST = scoreFirst;
        SCORE_SECOND = scoreSecond;
        SCORE_THIRD = scoreThird;
    }

    /**
     * Makes a turn. Edit this method to make your bot smarter. Currently does
     * only random moves.
     *
     * @return The selected move we want to make.
     */
    @Override
    public IMove doMove(IGameState state)
    {
        List<IMove> moves = state.getField().getAvailableMoves();
        List<PreferredMove> preferredMoves = new ArrayList<>();

        for (IMove move : moves)
        {
            preferredMoves.add(new PreferredMove(move, state));
        }

        if (moves.size() > 0)
        {
            List<PreferredMove> finalMove = new ArrayList<>();
            for (PreferredMove preferredMove : preferredMoves)
            {
                if (finalMove.size() < 1)
                {
                    finalMove.add(preferredMove);
                }
                else
                {
                    if (finalMove.get(0).getScore() < preferredMove.getScore())
                    {
                        finalMove.clear();
                        finalMove.add(preferredMove);
                    }
                    else if (finalMove.get(0).getScore() == preferredMove.getScore())
                    {
                        finalMove.add(preferredMove);
                    }
                }
            }
            if (finalMove.size() > 0)
            {
                //System.out.println("Available Moves: " + moves.size() + ", FINAL MOVE SIZE: " + finalMove.size());
                PreferredMove chosen = finalMove.get(rand.nextInt(finalMove.size()));
                //System.out.println("Chosen X: " + chosen.getMove().getX() + " Y: " + chosen.getMove().getY() + " Score: " + chosen.getScore());
                return chosen.getMove();
            }
        }
        return null;
    }

    @Override
    public String getBotName()
    {
        return BOTNAME;
    }
}
