package model;

import controller.DrawManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * It is the system for a chess game. It has fields to store the condition of
 * the chess and method to access and modify those information.
 * 
 * @author zhangq2
 *
 */
public class Chess {
	private int time;
	private Board board;
	ArrayList<Piece> white;
	ArrayList<Piece> black;
	private Record records;

	private Collection<Square> list;
	private DrawManager drawManager;
	private Piece chosen;

	// ----------------------------------------------------------------------------------------------------------------------------
	// constructors and methods used to create and initializes the chess game.
	/**
	 * construct a default chess with start setting.
	 * 
	 */
	public Chess() {
		time = 0;
		records = new Record();
		drawManager = new DrawManager();
		board = new Board();
		white = new ArrayList<Piece>();
		black = new ArrayList<Piece>();
		list = new ArrayList<Square>();

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Square t = board.spotAt(i, j);
				int y = t.getY();
				if (y == 1) {
					white.add(startSet(t.getX(), true, t));
				} else if (y == 2) {
					white.add(new Pawn(true, t, this));
				} else if (y == 7) {
					black.add(new Pawn(false, t, this));
				} else if (y == 8) {
					black.add(startSet(t.getX(), false, t));
				}
				list.add(t);
			}
		}
		Collections.sort(white);
		Collections.sort(black);
	}

	/**
	 * 
	 * @param x
	 *            which file of this piece
	 * @param c
	 *            White or Black
	 * @param p
	 *            original position of this piece
	 * @return creates new pieces to put into the start chessboard.
	 */
	private Piece startSet(int x, boolean c, Square p) {
		if (x == 1 || x == 8)
			return new Rook(c, p, this);
		else if (x == 2 || x == 7)
			return new Knight(c, p, this);
		else if (x == 3 || x == 6)
			return new Bishop(c, p, this);
		else if (x == 4)
			return new Queen(c, p, this);
		else if (x == 5)
			return new King(c, p, this);
		else
			return null;
	}

	// ---------------------------------------------------------------------------
	// Accessors

	public boolean getWhoseTurn() {
		return time % 2 == 0;
	}

	public int getRound() {
		return time / 2 + 1;
	}

	/**
	 * 
	 * @return true if the game has terminated.
	 */
	public boolean hasEnd() {
		return records.hasEnd();
	}

	/**
	 * 
	 * @return the board of the chess
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * 
	 * @param file
	 *            the file of the square
	 * @param rank
	 *            the rank of the square
	 * @return the position of the square
	 */
	public Square spotAt(int file, int rank) {
		return board.spotAt(file, rank);
	}

	public Record getRecords() {
		return records;
	}

	@Override
	public String toString() {
		return board.toString();
	}

	/**
	 * 
	 * @param type
	 *            type of the piece
	 * @param end
	 *            square to move to
	 * @return all possible pieces that can move to given square
	 */
	public ArrayList<Piece> possibleMovers(Class<? extends Piece> type, Square end) {
		ArrayList<Piece> possible = new ArrayList<Piece>();
		ArrayList<Piece> set;
		if (getWhoseTurn())
			set = white;
		else
			set = black;

		for (int j = 0; j < set.size(); j++) {
			Piece i = set.get(j);
			if (i.isType(type) && i.canGo(end))
				possible.add(i);
		}
		return possible;
	}

	/**
	 * Find out whether a certain move will put your own king in check.
	 * 
	 * The concept is simple here, presumably make the first and check if the
	 * opponent is checking {@link Chess#checkOrNot}. Then undo the move
	 * 
	 * @param move
	 * @return true if this move will give away the king
	 */
	public boolean giveAwayKing(Move move) {
		move.performMove(this);
		boolean giveAway = checkOrNot(!move.getWhoseTurn());
		move.undo(this);
		return giveAway;
	}

	/**
	 * checkOrNot(true) will return true if the white is checking the black's
	 * king
	 * 
	 * checkOrNot(false) will return true if the black is checking the white's
	 * king
	 * 
	 * @param attacker
	 *            who is attacking and tries to make this check
	 * @return whether it is checking
	 */
	public boolean checkOrNot(boolean attacker) {
		if (attacker)
			return isAttacked(true, black.get(0).getSpot());
		else
			return isAttacked(false, white.get(0).getSpot());
	}

	/**
	 * 
	 * @param whiteOrBlack
	 *            who is attacking (true, the while, false, the black)
	 * @param square
	 *            the square to check for
	 * @return true if square is attacked
	 */
	public boolean isAttacked(boolean whiteOrBlack, Square square) {
		ArrayList<Piece> attacker;
		if (whiteOrBlack)
			attacker = white;
		else
			attacker = black;

		for (int j = 0; j < attacker.size(); j++) {
			Piece i = attacker.get(j);
			if (i.canAttack(square) != null)
				return true;
		}
		return false;
	}

	/**
	 * 
	 * used to find if there is a checkmake, and stalemate. if this method
	 * returns true, and it is now in check, it is a checkmate; but if it is now
	 * not in check, the opponent cannot make any legitimate move, so it is a
	 * draw by stalemate.
	 * 
	 * @param checked
	 *            the side that can be potentially checkmated
	 * @return after the opponent makes one move, is it possible for his king
	 *         not to be in check
	 */
	boolean checkMate(boolean checked) {
		ArrayList<Piece> inCheck;
		if (checked)
			inCheck = white;
		else
			inCheck = black;
		for (int j = 0; j < inCheck.size(); j++) {
			Piece i = inCheck.get(j);
			Iterator<Square> iter = board.iterator();
			while (iter.hasNext()) {
				Square p = iter.next();
				if (i.canGo(p)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check if both players do not have enough pieces to make a checkmate.
	 * 
	 * @return True if both players lack the material to checkmate the other.
	 */
	boolean impossibleCheckMate() {
		// King versus king
		if (this.white.size() == 1 && this.black.size() == 1) {
			return true;
		}

		// King and Bishop or King and Knight against King
		final char kingAndBishop[] = { 'K', 'B' };
		final char kingAndKnight[] = { 'K', 'N' };
		if (this.white.size() == 1) {
			if (containsPieces(this.black, kingAndBishop) || containsPieces(this.black, kingAndKnight)) {
				return true;
			}
		} else if (this.black.size() == 1) {
			if (containsPieces(this.white, kingAndBishop) || containsPieces(this.white, kingAndKnight)) {
				return true;
			}
			// King and Bishop against King and Bishop same color bishops
		} else if (containsPieces(this.white, kingAndBishop) && containsPieces(this.black, kingAndBishop)) {
			boolean bishopColor = false;
			for (int i = 0; i < this.white.size(); i++) {
				Piece p = this.white.get(i);
				if (p instanceof Bishop) {
					bishopColor = ((Bishop) p).getBishopType();
				}
			}

			for (int i = 0; i < this.black.size(); i++) {
				Piece p = this.black.get(i);
				if (p instanceof Bishop) {
					return ((Bishop) p).getBishopType() == bishopColor;
				}
			}
		}

		return false;
	}

	/**
	 * Check if a list of pieces contains certain piece types.
	 * 
	 * @return True if all of the piece types match.
	 */
	private boolean containsPieces(ArrayList<Piece> pieces, char[] pieceTypes) {
		if (pieces.size() == pieceTypes.length) {
			for (int i = 0; i < pieces.size(); i++) {
				boolean found = false;
				for (int j = 0; j < pieceTypes.length; j++) {
					if (pieces.get(i).isType(getPieceClass(pieceTypes[j]))) {
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}
			return true;
		}

		return false;
	}

	/**
	 * whether I can claim draw according to the rules. if it meets the
	 * requirement of 'Fifty-move rule', or 'threefold repetition rule', add
	 * marks to canClaimDraw. So both players can claim draw if they want.
	 * 
	 * @return the type of draw if the game meets the automatic requirement for
	 *         draw, null if requirements not met
	 */
	public Draw canClaimDraw() {
		if (isFiftySilentMove())
			return Draw.FIFTY_MOVE;
		if (isThreeFoldRepetition())
			return Draw.REPETITION;
		return null;
	}

	private boolean isThreeFoldRepetition() {
//		circleLoop: for (int circle = 4; circle < 50 && circle * 2 > time; circle += 2) {
//			for (int j = 1; j <= circle; j++) {
//				if (!records.get(time - j).equals(records.get(time - j - circle))) {
//					continue circleLoop;
//				}
//			}
//			return true;
//		}
		return false;
	}

	private boolean isFiftySilentMove() {
		if (time > 50) {
			for (int i = time - 50; i < time; i++) {
				if (records.get(i).notQuiet()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 * En Passant is a very special rule of chess. An En Passant move can only
	 * be made immediately after the opponent moves his pawn by two squares. So
	 * this method is called to examine whether the records indicate a
	 * legitimate En Passant move.
	 * 
	 * @param end
	 *            the square the opponent's pawn just went through, and my pawn
	 *            try to go as an En Passant move.
	 * @return whether the En Passant move is legal right now.
	 */
	public boolean canEnPassant(Square end) {
		Move move = lastMove();
		if (move == null)
			return false;
		return move.canEnPassant(end);
	}

	/**
	 * Castling is a very special rule of chess. Castling move can only be made
	 * if the king and rook in this castling have never moved yet, plus any
	 * squares the king goes through cannot be attacked.
	 * 
	 * find out whether castling is legal.
	 * 
	 * @param k
	 *            the king that tries to do this castling.
	 * @param longOrShort
	 *            long Casting happens on the Queen side, while the short
	 *            castling happens on the King side.
	 * @return the castling move if it is legal to make the castling of this
	 *         side right now, null otherwise
	 */
	public Move canShortCastling(King k) {
		if (canNotShortCastling(k.getY(), !k.getWhiteOrBlack()))
			return null;
		return new Castling(k, k.getSpot(), spotAt(7, k.getY()), (Rook) (spotAt(8, k.getY()).getPiece()),
				spotAt(8, k.getY()), getRound());
	}

	public Move canLongCastling(King k) {
		if (canNotLongCastling(k.getY(), !k.getWhiteOrBlack()))
			return null;
		return new Castling(k, k.getSpot(), spotAt(3, k.getY()), (Rook) (spotAt(1, k.getY()).getPiece()),
				spotAt(1, k.getY()), getRound());
	}

	private boolean canNotLongCastling(int y, boolean attack) {
		return spotAt(2, y).isOccupied() || spotAt(3, y).isOccupied() || spotAt(4, y).isOccupied()
				|| isAttacked(attack, spotAt(5, y)) || isAttacked(attack, spotAt(3, y))
				|| isAttacked(attack, spotAt(4, y)) || records.hasMoved(spotAt(1, y), Rook.class, time)
				|| records.hasMoved(spotAt(5, y), King.class, time);
	}

	private boolean canNotShortCastling(int y, boolean attack) {
		return spotAt(6, y).isOccupied() || spotAt(7, y).isOccupied() || isAttacked(attack, spotAt(5, y))
				|| isAttacked(attack, spotAt(6, y)) || isAttacked(attack, spotAt(7, y))
				|| records.hasMoved(spotAt(8, y), Rook.class, time) || records.hasMoved(spotAt(5, y), King.class, time);
	}

	/**
	 * 
	 * @return the output of the last move, which will be displayed in the box
	 *         or as part of the records. null if the game has not started yet
	 */
	public String lastMoveOutPrint() {
		Move move = lastMove();
		if (move == null)
			return null;
		return move.getPrintOut();
	}

	/**
	 * 
	 * @return the description of the last move, which will be printed out in
	 *         the top label.
	 */
	public String lastMoveDiscript() {
		if (records.hasEnd())
			return records.getEndGameDescript();
		Move move = lastMove();
		if (move == null)
			return "Hasn't start the chess yet!";
		return move.getDescript();
	}

	public Move lastMove() {
		if (time == 0)
			return null;
		return records.get(time - 1);
	}

	// modifiers

	/**
	 * 
	 * If a piece is captured, and we need to put it off the board, we call this
	 * method. This method is usually called with moveTo(Spot s) method in
	 * Piece.
	 * 
	 * @param taken
	 *            the piece that is captured
	 */
	public void takeOffBoard(Piece taken) {
		Square p = taken.getSpot();
		if (p == null)
			return;
		taken.getSpot().setOccupied(null);
		if (taken.getWhiteOrBlack())
			white.remove(taken);
		else
			black.remove(taken);
	}

	/**
	 * This method is only used to undo previous move.
	 * 
	 * @param taken
	 *            the piece that is returned to the board
	 */
	public void putBackToBoard(Piece taken, Square spot) {
		if (taken != null) {
			if (taken.getWhiteOrBlack()) {
				white.add(taken);
			} else {
				black.add(taken);
			}
			taken.moveTo(spot);
		}
	}

	// ----------------------------------------------------------------------------------------------------------
	// Methods to deal with the commands and requested moves by the user.
	/**
	 * changes the records and the chessboard, so everything will return back to
	 * the state of previous round.
	 */
	public boolean undoLastMove() {
		Move lastMove = lastMove();
		if (lastMove == null)
			return false;
		lastMove.undo(this);
		records.removeLast();// TODO: records can be improved
		time--;
		return true;
	}

	// ----------------------------------------------------------------------------------------------------------
	// Methods to deal with the commands and requested moves by the user.

	/**
	 * perform command to move piece to certain spot
	 * 
	 * @param piece
	 * @param end
	 * @return true if move is valid, false if not allowed by chess rule
	 */
	public Move performMove(Piece piece, Square end) {
		Move move = piece.getMove(end);
		if (move != null) {
			makeMove(move);
		}

		return move;
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// methods that send message to control

	/**
	 * All moves from the user input will go throw this method
	 * 
	 * @param move
	 */
	public void makeMove(Move move) {
		// make the move
		move.performMove(this);
		// add rocord
		records.add(move);

		// update time
		time++;

		// check end game situations
		if (checkOrNot(!getWhoseTurn())) {
			if (checkMate(getWhoseTurn())) {
				move.note = MoveNote.CHECKMATE;
				if (getWhoseTurn())
					endGame(Win.BLACKCHECKMATE);
				else
					endGame(Win.WHITECHECKMATE);
				return;
			}
			move.note = MoveNote.CHECK;
		} else {
			if (checkMate(getWhoseTurn()) || impossibleCheckMate()) {
				endGame(Draw.STALEMATE);
				return;
			}
		}
	}

	/**
	 * record end game information and send message to control
	 * 
	 * @param endgame
	 *            the kind of end game occurred
	 */
	public void endGame(EndGame endgame) {
		records.endGame(endgame);
	}

	/**
	 * Create a move object for the standard chess record.
	 * 
	 * @param moveCommand
	 *            String for the chess record
	 * 
	 * @return Move object representing the desired move
	 * 
	 */
	public Move interpreteMoveCommand(String moveCommand) throws InvalidMoveException {
		Move move = null;
		if (moveCommand.startsWith("O")) {
			King king;
			if (getWhoseTurn()) {
				king = (King) white.get(0);
			} else {
				king = (King) black.get(0);
			}
			if (moveCommand.equals("O-O")) {
				move = canShortCastling(king);
			} else if (moveCommand.equals("O-O-O")) {
				move = canLongCastling(king);
			} else {
				throw new InvalidMoveException(moveCommand, InvalidMoveException.invalidFormat);
			}
			if (move != null) {
				return move;
			} else {
				throw new InvalidMoveException(moveCommand, InvalidMoveException.castleNotAllowed);
			}
		}

		IMovePatternMatcher m = new MovePatternMatcher(moveCommand);
		if (m.matches()) {
			Class<? extends Piece> type = m.getGroup(1) == null ? Pawn.class : getPieceClass(m.getGroup(1).charAt(0));
			Square start = null;
			if ((m.getGroup(2) != null) && (m.getGroup(3) != null)) {
				start = board.getSquare(m.getGroup(2) + m.getGroup(3));
			}
			Square end = board.getSquare(m.getGroup(5));
			if (start != null) {
				Piece movedPiece = start.getPiece();
				if (movedPiece == null) {
					throw new InvalidMoveException(moveCommand, InvalidMoveException.pieceNotPresent);
				} else if (!(movedPiece.isType(type))) {
					throw new InvalidMoveException(moveCommand, InvalidMoveException.incorrectPiece);
				}
				move = movedPiece.getMove(end);
			} else {
				ArrayList<Piece> possible = possibleMovers(type, end);
				if (possible.size() == 0) {
					throw new InvalidMoveException(moveCommand, InvalidMoveException.impossibleMove);
				} else if (possible.size() == 1) {
					move = possible.get(0).getMove(end);
				} else {
					throw new InvalidMoveException(moveCommand, InvalidMoveException.ambiguousMove);
				}
			}
			if (move instanceof Promotion) {
				Promotion promotion = (Promotion) move;
				if (m.getGroup(6) == null)
					throw new InvalidMoveException(moveCommand, InvalidMoveException.promotionTo);
				Class<? extends Piece> promotToClass = getPieceClass(m.getGroup(6).charAt(1));
				promotion.setPromoteTo(promotToClass);
			}
			return move;
		} else {
			throw new InvalidMoveException(moveCommand, InvalidMoveException.invalidFormat);
		}
	}

	/**
	 * This method convert the character to one type of {@link Piece} class. It
	 * used mostly for parsing commands like
	 * <p>
	 * e2-e4 <br />
	 * Nb1-c3
	 * </p>
	 * 
	 * @param character
	 *            the character representing the piece
	 * @return the corresponding class
	 */
	public static Class<? extends Piece> getPieceClass(char character) {
		switch (Character.toUpperCase(character)) {
		case 'P':
			return Pawn.class;
		case 'R':
			return Rook.class;
		case 'N':
			return Knight.class;
		case 'B':
			return Bishop.class;
		case 'Q':
			return Queen.class;
		case 'K':
			return King.class;
		default:
			return Piece.class;
		}
	}

  public DrawManager getDrawManager() {
		return this.drawManager;
	}

	public Piece getChosen() {
		return chosen;
	}

	public void setChosen(Piece chosen) {
		this.chosen = chosen;
	}
}