
package jump61;

import java.util.Random;

/** An automated Player.
 *  @author P. N. Hilfinger
 */
class AI extends Player {

    /** A new player of GAME initially COLOR that chooses moves automatically.
     *  SEED provides a random-number seed used for choosing moves.
     */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _depth = 4;
        _random = new Random(seed);
        _winningValue = Integer.MAX_VALUE;
        _player = getSide();
    }


    @Override
    String getMove() {
        Board board = getGame().getBoard();
        assert getSide() == board.whoseMove();
        assert board.getWinner() == null;
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private int searchForMove() {
        Side player = getSide();
        Board work = new Board(getBoard());
        assert getSide() == work.whoseMove();
        int sense;
        if (player == Side.RED) {
            sense = 1;
        } else {
            sense = -1;
        }
        minMax(work, 0, true, sense, -_winningValue, _winningValue);
        return _foundMove;
    }


    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */

    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        if (depth == _depth || board.getWinner() != null) {
            return staticEval(board, _winningValue);
        }
        Side playerToMove;
        int eval;
        int bestVal;
        Board test = new Board(board);
        if (sense == 1) {
            playerToMove = Side.RED;
            assert test.whoseMove() == playerToMove;
            bestVal = -Integer.MAX_VALUE;
            for (int n = 0; n < test.size() * test.size(); n++) {
                if (test.isLegal(playerToMove, n)) {
                    test.addSpot(playerToMove, n);
                    eval = minMax(test, depth + 1, false, -1, alpha, beta);
                    test.undo();
                    if (eval >= bestVal) {
                        if (saveMove) {
                            _foundMove = n;
                        }
                        bestVal = eval;
                    }
                    alpha = Math.max(alpha, bestVal);
                    if (beta <= alpha) {
                        return bestVal;
                    }

                }
            }
            return bestVal;
        } else {
            assert sense == -1;
            playerToMove = Side.BLUE;
            assert test.whoseMove() == playerToMove;
            bestVal = Integer.MAX_VALUE;
            for (int n = 0; n < test.size() * test.size(); n++) {
                if (test.isLegal(playerToMove, n)) {
                    test.addSpot(playerToMove, n);
                    eval = minMax(test, depth + 1, false, 1, alpha, beta);
                    test.undo();
                    if (eval <= bestVal) {
                        if (saveMove) {
                            _foundMove = n;
                        }
                        bestVal = eval;
                    }
                    beta = Math.min(beta, bestVal);
                    if (beta <= alpha) {
                        return bestVal;
                    }

                }
            }
            return bestVal;
        }
    }

    /** Return a heuristic estimate of the value of board position B.
     *  Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     *  indicate a win for Blue. */
    private int staticEval(Board b, int winningValue) {
        int evaluation;
        int redsides = b.numOfSide(Side.RED);
        int bluesides = b.numOfSide(Side.BLUE);
        if (redsides == b.size() * b.size()) {
            return winningValue;
        } else if (bluesides == b.size() * b.size()) {
            return -winningValue;
        }

        evaluation = redsides - bluesides;
        return evaluation;
    }

    /** A random-number generator used for move selection. */
    private Random _random;

    /** Used to convey moves discovered by minMax. */
    private int _foundMove;

    /** The current player. */
    private Side _player;

    /** Depth at which the AI is set to search at. */
    private int _depth;

    /** Integer denoting that the game is won for a side. */
    private int _winningValue;

}
