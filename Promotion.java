public class Promotion extends Move {
	private Piece promotedTo;

	/**
	 * creates a record for pawn promotion
	 * 
	 * @param moved
	 *            the pawn that promotes
	 * @param start
	 *            the start square of this move
	 * @param taken
	 *            the piece that in taken when the pawn promotes, null if there
	 *            is not one.
	 * @param end
	 *            the square the pawn promotes
	 * @param time
	 *            which round the pawn promotes
	 * @param checkOrNot
	 *            whether this promotion cause a check
	 */
	public Promotion(Piece moved, Square start, Piece taken, Square end,
			int round) {
		super(moved, start, taken, end, round);
		this.promotedTo = null;
	}

	public String outPrint() {
		return super.outPrint() + "(" + promotedTo.getType() + ")";
	}

	public void undo(Chess chess) {
		chess.takeOffBoard(promotedTo);
		chess.putBackToBoard(moved, start);
		if (taken != null) {
			chess.putBackToBoard(taken, end);
		}
	}

	public boolean notQuiet() {
		return true;
	}

	public String getDescript() {
		String s = "";
		if (wb)
			s += "White ";
		else
			s += "Black ";
		s += "Pawn promotes to ";
		s += promotedTo.getName() + "!!";

		if (endGame != null)
			s += endGame;
		return s;
	}
	
	public void performMove(Chess chess) {
		if (taken != null)
			chess.takeOffBoard(taken);
		moved.moveTo(end);
		promotedTo = chess.promotion(wb , end);
		chess.takeOffBoard(taken);
		chess.putBackToBoard(promotedTo, end);
		checkOrNot = chess.checkOrNot(chess.getWhoseTurn());
	}
	
}
