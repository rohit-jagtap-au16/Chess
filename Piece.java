import javax.management.RuntimeErrorException;

/**
 * the super class for all different kinds of pieces.
 * @author zhangq2
 *
 */
public abstract class Piece implements Comparable<Piece> {
	protected Chess chess;
	protected boolean wb;
	protected Square spot;

	/**
	 * 
	 * @param wb
	 *            whether this piece is white or black
	 * @param p
	 *            the square this piece is at initially.
	 */
	public Piece( boolean wb, Square p) {
		this.chess = p.getChess();
		this.wb = wb;	
		moveTo(p);
	}

	// ------------------------------------------------------------------------------------------------------------------------
	// accessors
	
	public String getName(){
		return getClass().getName();
	}

	public Square getP() {
		return spot;
	}

	public boolean getWb() {
		return wb;
	}

	public abstract char getType();

	public boolean isType(Class<? extends Piece> p) {
		return getClass() == p;
	}

	public abstract int getValue();

	/**
	 * this method is implemented for the sort method in the constructor of
	 * chess.
	 */
	public int compareTo(Piece a) {
		return (a.getValue() - getValue());
	}

	public int getX() {
		return spot.X();
	}

	public int getY() {
		return spot.Y();
	}

	/**
	 * Only place in this program that can change the position of a spot.
	 * 
	 * @param p
	 */
	public void moveTo(Square p) {
		if (spot != null) {
			spot.setOccupied(null);
		}
		if (p != null) {
			p.setOccupied(this);
		}
		spot = p;
	}

	/**
	 * 
	 * @param end
	 *            the end position
	 * @param chess
	 * @return true if it is legal to move this piece to the end, regardless of
	 *         the piece at the end position
	 */
	public abstract Move legalPosition(Square end);

	public boolean canGo(Square end){
		return canCapture(end) !=null || canMove(end) != null;
//		Move move =  canMove(end) ;
//		Move capture =  canCapture(end);
//		if (move != null && capture == null)
//			return move;
//		else if (move==null && capture !=null)
//			return capture;
//		else if (move!=null && capture != null)	
//			throw new RuntimeException();
//		else
//			return null;
	}

	/**
	 * 
	 * @param end
	 * @param chess
	 * @return true if it is legal to move this piece to the end position
	 */
	protected Move canMove(Square end) {
		if (end.occupied() || chess.giveAwayKing(this, spot, end.getPiece(), end, wb))
			return null;
		return legalPosition(end) ;
	}

	/**
	 * 
	 * @param end
	 * @param chess
	 * @return true if it is legal to capture the piece at the end
	 */
	protected Move canCapture(Square end) {
		if ( ! end.occupiedBy(!wb) || chess.giveAwayKing(this, spot, end.getPiece(), end, wb) )
			return null;
		return canAttack(end) ;
	}

	/**
	 * 
	 * @param end
	 * @param chess
	 * @return true if it is attacking the end spot.
	 */
	protected Move canAttack(Square end) {
		if (spot == null)
			return null;
		return legalPosition(end);
	}

	/**
	 * 
	 * @param end
	 *            the square moves to
	 * @param taken
	 *            the piece that will be taken, null if there is no piece taken
	 * @param chess
	 * @param takeOrNot
	 *            whether it takes a piece or not
	 * @return the output information it needs to print out in the box
	 */
	public void makeMove(Square end, Piece taken) {
		if (taken != null)
			chess.takeOffBoard(taken);
		Square start = spot;
		moveTo(end);
		chess.addRecord(this, start, taken, end);
	}

	public static Class<?extends Piece> getType(char c){
		switch (Character.toUpperCase(c)) {
		case 'P':return Pawn.class;
		case 'R':return Rook.class;
		case 'N':return Knight.class;
		case 'B':return Bishop.class;
		case 'Q':return Queen.class;
		case 'K':return King.class;
		default:return Piece.class;
		}
	}
	
	public String toString(){
		return getName()+" at "+getP();
	}
}
