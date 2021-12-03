package jump61;

import org.junit.Test;
import ucb.junit.textui;


/** The suite of all JUnit tests for the Jump61 program.
 *  @author Jake Clayton
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    @Test
    public void newGame() {
        Board gameBoard = new Board(6);
        gameBoard.addSpot(Side.BLUE, 6, 0);
        Side player = gameBoard.whoseMove();
        for (int i = 0; i < 7; i++) {
            gameBoard.addSpot(player, 1, 1);
        }
        player = player.opposite();

    }

    @Test public void boardSetter() {
        int test = (int) Double.POSITIVE_INFINITY;
        Board game2 = new Board(6);
        for (int i = 0; i < 5; i++) {
            game2.addSpot(Side.RED, 1, 1);
        }
        game2.addSpot(Side.RED, 1, 1);
        int i = 0;
    }

    @Test
    public void boardTest() {
        Board gameBoard = new Board(6);
        int aa = gameBoard.sqNum(6, 6);
        int[] a = gameBoard.allNeighbors(1, 1);
        int[] b = gameBoard.allNeighbors(1, 6);
        int[] c = gameBoard.allNeighbors(6, 1);
        int[] d = gameBoard.allNeighbors(6, 6);


        Board copy = new Board(gameBoard.readonlyBoard());
        gameBoard.addSpot(Side.BLUE, 1, 1);
        Board copy2 = new Board(gameBoard.readonlyBoard());
        copy2.addSpot(Side.BLUE, 1, 1);

    }


    public static void main(String[] ignored) {
        System.exit(textui.runClasses(jump61.BoardTest.class));
    }

    @Test
    public void whatsWrong() {
        Board debug = new Board(3);
        debug.set(1, 1, 1, Side.BLUE);
        debug.set(1, 2, 3, Side.RED);
        debug.set(1, 3, 2, Side.BLUE);

    }
}


