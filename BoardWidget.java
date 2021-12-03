
package jump61;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static jump61.Side.*;

/** A GUI component that displays a Jump61 board, and converts mouse clicks
 *  on that board to commands that are sent to the current Game.
 *  @author Jake Clayton
 */
class BoardWidget extends Pad {

    /** Length of the side of one square in pixels. */
    private static final int SQUARE_SIZE = 50;
    /** Half the length of one square in pixels. */
    private static final int HALF_SQUARE = 25;
    /** Width and height of a spot. */
    private static final int SPOT_DIM = 8;
    /** Minimum separation of center of a spot from a side of a square. */
    private static final int SPOT_MARGIN = 12;
    /** Seperation for when more spots are needed. */
    private static final int SMALL_MARGIN = 8;
    /** Width of the bars separating squares in pixels. */
    private static final int SEPARATOR_SIZE = 3;
    /** Width of square plus one separator. */
    private static final int SQUARE_SEP = SQUARE_SIZE + SEPARATOR_SIZE;
    /** For Style Check 1 change 1. */
    private static final int LEN = SQUARE_SIZE - SPOT_MARGIN;


    /** Colors of various parts of the displayed board. */
    private static final Color
        NEUTRAL = Color.WHITE,
        SEPARATOR_COLOR = Color.BLACK,
        SPOT_COLOR = Color.BLACK,
        RED_TINT = new Color(255, 200, 200),
        BLUE_TINT = new Color(200, 200, 255);

    /** A new BoardWidget that monitors and displays a game Board, and
     *  converts mouse clicks to commands to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        _side = 6 * SQUARE_SEP + SEPARATOR_SIZE;
        setMouseHandler("click", this::doClick);
    }

    /* .update and .paintComponent are synchronized because they are called
     *  by three different threads (the main thread, the thread that
     *  responds to events, and the display thread).  We don't want the
     *  saved copy of our Board to change while it is being displayed. */

    /** Update my display to show BOARD.  Here, we save a copy of
     *  BOARD (so that we can deal with changes to it only when we are ready
     *  for them), and recompute the size of the displayed board. */
    synchronized void update(Board board) {
        if (board.equals(_board)) {
            return;
        }
        if (_board != null && _board.size() != board.size()) {
            invalidate();
        }
        _board = new Board(board);
        _side = _board.size() * SQUARE_SEP + SEPARATOR_SIZE;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(_side, _side);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        if (_board == null) {
            return;
        }
        g.setColor(NEUTRAL);
        g.fillRect(0, 0, _side, _side);
        g.setColor(SEPARATOR_COLOR);
        int i = 0;
        while (i < _side) {
            g.fillRect(i, 0, SEPARATOR_SIZE, _side);
            g.fillRect(0, i, _side, SEPARATOR_SIZE);
            i += SQUARE_SEP;
        }
        for (int r = 1; r <= _board.size(); r++) {
            for (int c = 1; c <= _board.size(); c++) {
                displaySpots(g, r, c);
            }
        }

    }

    /** Color and display the spots on the square at row R and column C
     *  on G.  (Used by paintComponent). */
    private void displaySpots(Graphics2D g, int r, int c) {
        if (_board == null) {
            return;
        }
        assert _board.exists(r, c);
        int x =  SEPARATOR_SIZE + (r - 1) * SQUARE_SEP;
        int y =  SEPARATOR_SIZE + (c - 1) * SQUARE_SEP;
        Square copyThis = _board.get(r, c);
        int spots = copyThis.getSpots();
        Side color = copyThis.getSide();
        if (color == BLUE) {
            g.setColor(BLUE_TINT);
        } else if (color == RED) {
            g.setColor(RED_TINT);
        } else {
            g.setColor(NEUTRAL);
        }
        g.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
        if (spots == 1) {
            spot(g, x + HALF_SQUARE, y + HALF_SQUARE);
        } else if (spots == 2) {
            spot(g, x + SPOT_MARGIN, y + HALF_SQUARE);
            spot(g, x + SQUARE_SIZE - SPOT_MARGIN, y + HALF_SQUARE);
        } else if (spots == 3) {
            spot(g, x + HALF_SQUARE, y + HALF_SQUARE);
            spot(g, x + SPOT_MARGIN, y + SQUARE_SIZE - SPOT_MARGIN);
            spot(g, x + SQUARE_SIZE - SPOT_MARGIN, y + SPOT_MARGIN);
        } else if (spots == 4) {
            spot(g, x + SPOT_MARGIN, y + SPOT_MARGIN);
            spot(g, x + SPOT_MARGIN, y + SQUARE_SIZE - SPOT_MARGIN);
            spot(g, x + SQUARE_SIZE - SPOT_MARGIN, y + SPOT_MARGIN);
            spot(g, x + LEN, y + LEN);
        } else if (spots == 5) {
            spot(g, x + HALF_SQUARE, y + HALF_SQUARE);
            spot(g, x + SPOT_MARGIN, y + SPOT_MARGIN);
            spot(g, x + SQUARE_SIZE - SPOT_MARGIN, y + SPOT_MARGIN);
            spot(g, x + SPOT_MARGIN, y + SQUARE_SIZE - SPOT_MARGIN);
            spot(g, x + SQUARE_SIZE - SPOT_MARGIN, y + LEN);
        } else if (spots == 6) {
            spot(g, x + SPOT_MARGIN, y + SMALL_MARGIN);
            spot(g, x + SQUARE_SIZE - SPOT_MARGIN, y + SMALL_MARGIN);
            spot(g, x + SPOT_MARGIN, y + HALF_SQUARE);
            spot(g, x + SQUARE_SIZE - SPOT_MARGIN, y + HALF_SQUARE);
            spot(g, x + SPOT_MARGIN, y + SQUARE_SIZE - SMALL_MARGIN);
            spot(g, x + LEN, y +  LEN);
        }
    }

    /** Draw one spot centered at position (X, Y) on G. */
    private void spot(Graphics2D g, int x, int y) {
        g.setColor(SPOT_COLOR);
        g.fillOval(x - SPOT_DIM / 2, y - SPOT_DIM / 2, SPOT_DIM, SPOT_DIM);
    }

    /** Respond to the mouse click depicted by EVENT. */
    public void doClick(String dummy, MouseEvent event) {
        int x = event.getX() - SEPARATOR_SIZE,
            y = event.getY() - SEPARATOR_SIZE;
        if (_board.getWinner() != null) {
            return;
        }
        int r = x / SQUARE_SEP + 1;
        int c = y / SQUARE_SEP + 1;
        if (!_board.isLegal(_board.whoseMove(), r, c)) {
            return;
        }
        _commandQueue.offer(String.format("%d %d", r, c));
    }

    /** The Board I am displaying. */
    private Board _board;
    /** Dimension in pixels of one side of the board. */
    private int _side;
    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;
}
