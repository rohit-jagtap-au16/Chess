package model;

import model.Piece.Player;

/**
 * this class is a record of all the moves in this game, so we can undo steps or
 * check for special rules.
 * 
 * @author zhangq2
 *
 */
public abstract class Move {
	protected final Player playerColor;
	protected final Piece movedPiece;
	protected final Square startPosition;
	protected final Piece capturedPiece;
	protected final Square lastPosition;
	protected MoveNote note;

	/**
	 * constructs a record
	 * 
	 * @param movedPiece
	 *            the piece moved
	 * @param startPosition
	 *            the start position of this move
	 * @param capturedPiece
	 *            the piece that is captured, null if there is nothing captured.
	 * @param lastPosition
	 *            the end position of this move
	 * @param time
	 *            which round this move happens
	 */
	public Move(Piece movedPiece, Square startPosition, Piece capturedPiece, Square lastPosition) {
		this.playerColor = movedPiece.getWhiteOrBlack();
		this.movedPiece = movedPiece;
		this.startPosition = startPosition;
		this.capturedPiece = capturedPiece;
		this.lastPosition = lastPosition;
		this.note = MoveNote.NONE;
	}

	/**
	 * 
	 * @return start square
	 */
	public Square getStart() {
		return startPosition;
	}

	/**
	 * called when the program needs to find out whether it is legal to make a
	 * castling.
	 * 
	 * @param p
	 * @return true, this last move matches the rule, and it is legal to make a
	 *         castling
	 */
	public boolean canEnPassant(Square p) {
		return movedPiece.isType(Pawn.class) && (startPosition.getX() == p.getX() && lastPosition.getX() == p.getX()
				&& (startPosition.getY() + lastPosition.getY()) == (p.getY() * 2));
	}

	/**
	 * This methods is called to examine whether 'threefold repetition rule'.
	 * 
	 * @param x
	 * @return if two moves are exactly the same and repeatable.
	 */
	public boolean equals(Move x) {
		if (notQuiet() || x.notQuiet())
			return false;
		if (x instanceof Castling)
			return false;
		return movedPiece.equals(x.movedPiece) && startPosition.equals(x.startPosition)
				&& lastPosition.equals(x.lastPosition);
	}

	public String toString() {
		return getPrintOut() + " " + getDescript();
	}

	public Player getWhoseTurn() {
		return playerColor;
	}

	/**
	 * @return the documentation in standard chess convention
	 */
	public String getDoc() {
		String doc = "";
		if (!movedPiece.isType(Pawn.class))
			doc += movedPiece.getType();
		doc += startPosition.toString();
		if (capturedPiece == null)
			doc += "-";
		else
			doc += "x";
		doc += lastPosition.toString();
		doc += note.getDocEnd();
		return doc;
	}

	/**
	 * @return the messages necessary to printOut in the console
	 */
	public String getPrintOut() {
		return getDoc();
	}

	/**
	 * This method is called to examine whether the game has meets the
	 * requirement of 'Fifty move rule".
	 * 
	 * @return true if this move can be redo over and over again later.
	 */
	public boolean notQuiet() {
		return capturedPiece != null || movedPiece.isType(Pawn.class);
	}

	public void performMove(Chess chess) {
		if (capturedPiece != null)
			chess.takeOffBoard(capturedPiece);
		movedPiece.moveTo(lastPosition);
	}

	abstract public String getDescript();

	abstract public void undo(Chess chess);

}