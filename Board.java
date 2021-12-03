
package jump61;


import java.util.ArrayDeque;
import java.util.Formatter;

import java.util.function.Consumer;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author Jake Clayton
 */
class Board {

        /** An N x N board in initial configuration. */
    Board(int N) {
        _history = new ArrayDeque<>();
        _numMoves = 0;
        _workQueue = new ArrayDeque<>();
        _squares = new Square[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                _squares[r][c] = Square.square(Side.WHITE, 1);
            }
        }
        _currentPlayer = whoseMove();
        _readonlyBoard = new ConstantBoard(this);
        _notifier = NOP;
    }


    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */

    Board(Board board0) {
        _currentPlayer = board0.whoseMove();
        _history = new ArrayDeque<>();
        _workQueue = new ArrayDeque<>();
        _notifier = NOP;
        _numMoves = board0.numMoves() + 2;
        _squares = new Square[board0.size()][board0.size()];
        internalCopy(board0);
    }

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        _notifier = NOP;
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    void clear(int N) {
        _squares = new Square[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                _squares[r][c] = Square.square(Side.WHITE, 1);
            }
        }
        _workQueue = new ArrayDeque<>();
        Side s = ((numPieces() + size()) & 1) == 0 ? Side.RED : Side.BLUE;
        _currentPlayer = s;
        _numMoves = 0;
        _history = new ArrayDeque<>();
    }

    /** Copy the contents of BOARD into me. */
    void copy(Board board) {
        for (int r = 0; r < size(); r++) {
            for (int c = 0; c < size(); c++) {
                _squares[r][c] = board.get(r + 1, c + 1);
            }
        }
    }

    /** Copies the array representation of the board.
     * @return a copied 2d array.
     * @param arr is the 2d array board containing squares. */
    Square[][] copyData(Square[][] arr) {
        int len = arr.length;
        Square[][] result = new Square[len][len];
        for (int r = 0; r < len; r++) {
            for (int c = 0; c < len; c++) {
                result[r][c] = arr[r][c];
            }
        }
        return result;
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history. Assumes BOARD and I have the same size. */
    private void internalCopy(Board board) {
        assert size() == board.size();
        copy(board);
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return _squares.length;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        return _squares[n / size()][n % size()];
    }

    /** Returns the total number of spots on the board. */
    int numPieces() {
        int sumSpots = 0;
        for (int l = 0; l < size(); l++) {
            for (int w = 0; w < size(); w++) {
                sumSpots += get(w + 1, l + 1).getSpots();
            }
        }
        return sumSpots;
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        if (getWinner() != null) {
            return getWinner().opposite();
        }
        if (_numMoves == 0) {
            Side s = ((numPieces() + size()) & 1) == 0 ? Side.RED : Side.BLUE;
            _currentPlayer = s;
        }
        return _currentPlayer;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        if (!exists(n) || getWinner() != null) {
            return false;
        }
        if (get(n).getSide() == player.opposite()) {
            return false;
        }
        return true;
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        return player == whoseMove();
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        Side initial = get(0).getSide();
        Side check;
        if (initial == Side.WHITE) {
            return null;
        }
        for (int i = 1; i < size() * size(); i++) {
            check = get(i).getSide();
            if (check == Side.WHITE || check != initial) {
                return null;
            }
        }
        return initial;
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        int sumSides = 0;
        for (int l = 1; l <= size(); l++) {
            for (int w = 1; w <= size(); w++) {
                if (get(l, w).getSide().equals(side)) {
                    sumSides += 1;
                }
            }
        }
        return sumSides;
    }

    /** Returns the number of spots in the board of squares of given SIDE. */
    int numOfSpots(Side side) {
        int sumSpots = 0;
        for (int l = 0; l < size(); l++) {
            for (int w = 0; w < size(); w++) {
                Square cur = _squares[l][w];
                if (cur.getSide().equals(side)) {
                    sumSpots += cur.getSpots();
                }
            }
        }
        return sumSpots;
    }

    /** Returns (weighted) number of bad squares for a player.
     * @param side is the color being analyzed. */
    int numOfBadSquares(Side side) {
        int badSquares = 0;
        for (int l = 1; l <= size(); l++) {
            for (int w = 1; w <= size(); w++) {
                Square cur = get(l, w);
                if (cur.getSide().equals(side)) {
                    int spots = cur.getSpots();
                    if (spots == neighbors(l, w)) {
                        int[] check = allNeighbors(l, w);
                        for (int i = 0; i < check.length; i++) {
                            Square temp = get(check[i]);
                            if (temp.getSide() == side.opposite()) {
                                if (temp.getSpots() == neighbors(check[i])) {
                                    badSquares += 3;
                                }
                            }
                        }
                    }
                } else if (cur.getSide().equals(side.opposite())) {
                    if (cur.getSpots() == neighbors(l, w)) {
                        int[] check2 = allNeighbors(l, w);
                        for (int i = 0; i < check2.length; i++) {
                            Square temp = get(check2[i]);
                            if (temp.getSide() == side) {
                                badSquares += 1;
                            }
                        }
                    }
                }
            }
        }
        return badSquares;
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        addSpot(player, sqNum(r, c));
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        assert isLegal(player, n);
        announce();
        if (_history != null) {
            _history.push(copyData(_squares));
        }
        _currentPlayer = _currentPlayer.opposite();
        _numMoves += 1;
        int num;
        int spots = get(n).getSpots() + 1;
        internalSet(n, spots, player);
        if (overfull(spots, row(n), col(n))) {
            internalSet(n, spots - neighbors(n), player);
            addToQueue(n, player);
        }
        if (getWinner() != null) {
            announce();
            return;
        }
        while (!_workQueue.isEmpty()) {
            num = _workQueue.pop();
            spots = get(num).getSpots();
            if (overfull(spots, row(num), col(num))) {
                internalSet(num, spots - neighbors(num), player);
                addToQueue(num, player);
            }
            if (getWinner() != null) {
                announce();
                break;
            } else if (_workQueue.isEmpty()) {
                break;
            }
        }
    }

    /** Add neighbors to the workQueue to be processed
     * and adds a spot to each.
     * @param n is the Square number.
     * @param player is the current side. */
    void addToQueue(int n, Side player) {
        int size = size();
        int c = col(n) - 1;
        int r = row(n) - 1;
        if (c < (size - 1) && !_workQueue.contains(n + 1)) {
            internalSet(n + 1, get(n + 1).getSpots() + 1, player);
            _workQueue.push(n + 1);
        }
        if (r < (size - 1) && !_workQueue.contains(n + size)) {
            internalSet(n + size, get(n + size).getSpots() + 1, player);
            _workQueue.push(n + size);
        }
        if (c > 0 && !_workQueue.contains(n - 1))  {
            internalSet(n - 1, get(n - 1).getSpots() + 1, player);
            _workQueue.push(n - 1);
        }
        if (r > 0 && !_workQueue.contains(n - size)) {
            internalSet(n - size, get(n - size).getSpots() + 1, player);
            _workQueue.push(n - size);
        }
    }

    /** Tells whether the square is overfull or not.
     * @param spots Number of spots in Square r, c.
     * @param r is the row #.
     * @param c is the column #.
     * @return whether the number of spots makes the square overfull. */
    boolean overfull(int spots, int r, int c) {
        return spots > neighbors(r, c);
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white). Does not announce changes. */
    private void internalSet(int n, int num, Side player) {
        if (num > 0) {
            _squares[row(n) - 1][col(n) - 1] = Square.square(player, num);
        } else {
            _squares[row(n) - 1][col(n) - 1] = Square.square(Side.WHITE, num);
        }
        announce();
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        assert _numMoves > 0;
        Square[][] sub = _history.pop();
        _currentPlayer = _currentPlayer.opposite();
        _squares = copyData(sub);
        _workQueue.clear();
        announce();
        _numMoves--;
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===");
        out.format("\n");
        Square cur;
        int spots;
        Side side;
        for (int l = 0; l < size(); l++) {
            out.format("    ");
            for (int w = 0; w < size(); w++) {
                cur = _squares[l][w];
                String toOutput = "";
                spots = cur.getSpots();
                side = cur.getSide();
                toOutput += spots;
                if (side == Side.RED) {
                    toOutput += "r ";
                } else if (side == Side.BLUE) {
                    toOutput += "b ";
                } else {
                    toOutput += "- ";
                }
                out.format(toOutput);
            }
            out.format("\n");
        }
        out.format("===");
        return out.toString();
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    /** Returns all the neighbors of a square #N. */
    int[] allNeighbors(int n) {
        int size = size();
        int c = col(n) - 1;
        int r = row(n) - 1;
        int i = 0;
        int[] neighbors = new int[neighbors(n)];
        if (c < size - 1) {
            neighbors[i] = n + 1;
            i++;
        }
        if (r < size - 1) {
            neighbors[i] = n + size;
            i++;
        }
        if (c > 0) {
            neighbors[i] = n - 1;
            i++;
        }
        if (r > 0) {
            neighbors[i] = n - size;
        }
        return neighbors;
    }

    /** Return all neighbors of a square.
     * @param c is the column number.
     * @param r is the row number.*/
    int[] allNeighbors(int r, int c) {
        return allNeighbors(sqNum(r, c));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            return this == obj;
        }
    }


    @Override
    public int hashCode() {
        return numPieces();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Return number of moves that have been made so far. */
    public int numMoves() {
        return _numMoves;
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }


    /** Used in jump to keep track of squares needing processing.  Allocated
     *  here to cut down on allocations. */
    private ArrayDeque<Integer> _workQueue;

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board. */
    private Consumer<Board> _notifier;

    /** Representation of the board in a 2 dimensional array of squares. */
    private Square[][] _squares;

    /** Number of moves that have been made. */
    private int _numMoves;

    /** Current player who is making a move. */
    private Side _currentPlayer;

    /** History of board states stored in a deque. */
    private ArrayDeque<Square[][]> _history;


}
