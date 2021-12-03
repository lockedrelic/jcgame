
package jump61;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.concurrent.ArrayBlockingQueue;

/** The GUI controller for jump61.  To require minimal change to textual
 *  interface, we adopt the strategy of converting GUI input (mouse clicks)
 *  into textual commands that are sent to the Game object through
 *  a Writer.  The Game object need never know where its input is coming from.
 *  A Display is an Observer of Games and Boards so that it is notified when
 *  either changes.
 *  @author Jake Clayton
 */
class Display extends TopLevel implements View, CommandSource, Reporter {

    /** A new window with given TITLE displaying GAME, and using COMMANDWRITER
     *  to send commands to the current game. */
    Display(String title) {
        super(title, true);
        addButton("New Game", this::newGame, new LayoutSpec("x", 1));
        addButton("Quit Game", this::quit, new LayoutSpec("x", 2));
        addButton("Size 8", this::setSize8, new LayoutSpec("y", 3));
        addButton("Size 6", this::setSize6, new LayoutSpec("y", 4));
        addButton("Size 4", this::setSize4, new LayoutSpec("y", 5));
        addButton("Choose Blue", this::chooseBlue, new LayoutSpec("y", 3));
        addButton("Choose Red", this::chooseRed, new LayoutSpec("y", 4));
        addButton("Two Player", this::twoPlayer, new LayoutSpec("y", 5));
        addButton("Computer Only", this::computerOnly, new LayoutSpec("y", 5));
        _boardWidget = new BoardWidget(_commandQueue);
        add(_boardWidget, new LayoutSpec("y", 1, "width", 2));
        display(true);
    }

    /** Response to "Quit" button click. */
    void quit(String dummy) {
        System.exit(0);
    }

    /** Response to "New Game" button click. */
    void newGame(String dummy) {
        _commandQueue.offer("new");
    }

    /** Response to "Size 6" button click. */
    void setSize6(String dummy) {
        _commandQueue.offer("size 6");
        newGame(dummy);
    }

    /** Response to "Size 4" button click. */
    void setSize4(String dummy) {
        _commandQueue.offer("size 4");
        newGame(dummy);
    }

    /** Response to "Size 4" button click. */
    void setSize8(String dummy) {
        _commandQueue.offer("size 8");
        newGame(dummy);
    }

    /** Response to "Choose Blue" button click. */
    void chooseBlue(String dummy) {
        _commandQueue.offer("manual blue");
        _commandQueue.offer("auto red");
        newGame(dummy);
    }

    /** Response to "Choose Red" button click. */
    void chooseRed(String dummy) {
        _commandQueue.offer("manual red");
        _commandQueue.offer("auto blue");
        newGame(dummy);
    }

    /** Response to "Two Player" button click. */
    void twoPlayer(String dummy) {
        _commandQueue.offer("manual red");
        _commandQueue.offer("manual blue");
        newGame(dummy);
    }

    /** Response to "Computer Only" button click. */
    void computerOnly(String dummy) {
        _commandQueue.offer("auto red");
        _commandQueue.offer("auto blue");
    }

    @Override
    public void update(Board board) {
        try {
            Thread.sleep(BOARD_UPDATE_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _boardWidget.update(board);
        pack();
        _boardWidget.repaint();
    }

    @Override
    public String getCommand(String ignored) {
        try {
            return _commandQueue.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    @Override
    public void announceWin(Side side) {
        showMessage(String.format("%s wins!", side.toCapitalizedString()),
                    "Game Over", "information");
    }

    @Override
    public void announceMove(int row, int col) {
    }

    @Override
    public void msg(String format, Object... args) {
        showMessage(String.format(format, args), "", "information");
    }

    @Override
    public void err(String format, Object... args) {
        showMessage(String.format(format, args), "Error", "error");
    }

    /** Time interval in msec to wait after a board update. */
    static final long BOARD_UPDATE_INTERVAL = 60;

    /** The widget that displays the actual playing board. */
    private BoardWidget _boardWidget;
    /** Queue for commands going to the controlling Game. */
    private final ArrayBlockingQueue<String> _commandQueue =
        new ArrayBlockingQueue<>(5);
}
