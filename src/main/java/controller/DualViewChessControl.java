package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.ChessGameException;
import model.Draw;
import model.EndGame;
import model.Move;
import model.Pawn;
import model.Piece;
import model.Piece.Player;
import model.Square;
import model.Win;
import view.ChessViewer;
import view.IChessViewerControl;
import view.SquareLabel;
import view.ViewController;

/**
 * 
 * A chess controller that control two ChessView, each views the board
 * separately
 * 
 * @author zhang
 *
 */
public class DualViewChessControl extends ViewController implements IChessViewerControl {
	private ChessViewer whiteView;
	private ChessViewer blackView;

	/**
	 * start my little chess game!!!!
	 * 
	 * @param args
	 *            ignored
	 */
	public DualViewChessControl() {
		super();
		whiteView = new ChessViewer(this, "The Great Chess Game white view", true);
		blackView = new ChessViewer(this, "The Great Chess Game black view", false);

		for (Square s : chess.getAllSquares()) {
			updateSquare(whiteView, s);
			updateSquare(blackView, s);
		}
		repaintBothViews();
	}

	public void restart() {
		restartView(whiteView);
		restartView(blackView);
		repaintBothViews();
	}

	private ChessViewer chooesView(boolean whiteOrBlack) {
		return whiteOrBlack ? whiteView : blackView;
	}

	/**
	 * This method will be called, if the user types a command to make a move.
	 * 
	 * Interpret the command, and find out if it is legal to do make this move.
	 * If it is, make this move.
	 * 
	 * @param s
	 *            the input command
	 * @return
	 */
	public boolean makeMove(String s) {
		if (s.startsWith("o")) {
			if (s.equals("o-o")) {
				if (!chess.castling(false))
					whiteView.printOut("You cannot do castling, please check the rules for castling.");
			} else if (s.equals("o-o-o")) {
				if (!chess.castling(true))
					whiteView.printOut("You cannot do castling, please check the rules for castling.");
			} else {
				whiteView.printOut("For short castling, enter \"O-O\" and for long castling, enter \"O-O-O\".");
			}
			return true;
		}

		Pattern p = Pattern.compile("([prnbqk])?([a-h])?([1-8])?.*?([a-h][1-8]).*");
		Matcher m = p.matcher(s);
		if (m.matches())

		{
			Class<? extends Piece> type = m.group(1) == null ? Pawn.class : Piece.getType(m.group(1).charAt(0));
			if (type == Piece.class) {
				whiteView.printOut(
						"Please enter valid initial of chessman -- R(Root), N(Knight), B(Bishop), Q(Queen), K(King). If you omit it, it is assumed as Pawn.");
				// return true;
			}
			Square start = null;
			if ((m.group(2) != null) && (m.group(3) != null)) {
				start = chess.getBoard().getSquare(m.group(2) + m.group(3));
			}
			Square end = chess.getBoard().getSquare(m.group(4));

			if (start != null) {
				Piece movedChessman = start.getPiece();
				if (movedChessman == null) {
					if (chess.getWhoseTurn() == Player.WHITE)
						whiteView.printOut("There should be a white chessman in the start Position!");
					else
						whiteView.printOut("There should be a black chessman in the start Position!");
				} else if (!(movedChessman.isType(type))) {
					whiteView.printOut(
							"The chessman in the start Position is not corret! \n R(Root), N(Knight), B(Bishop), Q(Queen), K(King), omission for pawn");
				}
				Move move;
				if ((move = chess.performMove(movedChessman, end)) == null) {
					whiteView.printOut("Illegal move! Please check the rule of " + movedChessman.getName() + "!");
				} else {
					updateGuiToMove(move);
				}
			} else {
				System.out.println(" " + type + " " + end);
				ArrayList<Piece> possible = chess.possibleMovers(type, end);
				System.out.println(possible);
				if (possible.size() == 0) {
					whiteView.printOut("Fail to guess move: No one can reach that spot.");
				} else if (possible.size() == 1) {
					Move move;
					if ((move = chess.performMove(possible.get(0), end)) == null) {
						throw new RuntimeException("OOOOO!");
					} else {
						updateGuiToMove(move);
					}
				} else {
					whiteView.printOut("Fail to guess move: There is ambiguity, multiple possible moves.");
				}

			}
			return true;
		}
		return false;

	}

	@Override
	public void handleCommand(String command, boolean whiteOrBlack) {
		String c = command.toLowerCase();
		if (c.length() == 0)
			return;
		if (c.equals("print")) {
			ChessViewer viewer = chooesView(whiteOrBlack);
			printRecords(viewer, chess);
		} else if (c.equals("help")) {
			chooesView(whiteOrBlack).printOut(HELP_MESSAGE);
		} else if (c.startsWith("rules for ")) {
			ChessViewer view = chooesView(whiteOrBlack);
			showRules(c.substring(10), view, rules);
		} else if (c.equals("quit")) {
			System.exit(0);
		} else if (c.equals("restart")) {
			restart();
		} else {
			chosen = null;
			chooesView(whiteOrBlack).deHighLightWholeBoard();
			if (c.equals("resign")) {
				resign(chooesView(whiteOrBlack), chess);
			} else if (c.equals("draw")) {
				askForDraw(whiteOrBlack);
			} else if (whiteOrBlack != (chess.getWhoseTurn() == Player.WHITE)) {
				if (c.equals("undo"))
					undo(chess, whiteView);
				else
					chooesView(whiteOrBlack).printOut("Please wait for " + side(!whiteOrBlack) + " to finish");
			} else if (!makeMove(c)) {
				// makeMove return false, so this move is not allowed.
				chooesView(whiteOrBlack).printOut(ERROR_MESSAGE);
			}
		}

		repaintBothViews();
	}

	public void repaintBothViews() {
		repaintAll(whiteView);
		repaintAll(blackView);
	}

	@Override
	public void click(SquareLabel label, boolean whiteOrBlack) {
		ChessViewer clickedView = chooesView(whiteOrBlack);
		if (chess.hasEnd()) {
			clickedView.printOut("Game is already over! Type restart to start a new game");
		} else {
			if (whiteOrBlack != (chess.getWhoseTurn() == Player.WHITE)) {
				clickedView.printOut("Please wait for your opponnet to finish");
				return;
			}
			Square spot = labelToSquare(label, chess);
			if (chosen != null) {
				if (label.isHighLight() && !spot.equals(chosen.getSpot())) {
					Move move;
					if ((move = chess.performMove(chosen, spot)) == null) {
						throw new ChessGameException(
								"Illegal move of " + chosen.getName() + " did not correctly caught from UI!");
					} else {
						updateGuiToMove(move);
					}
				} else
					clickedView.cleanTemp();
				chosen = null;
				clickedView.deHighLightWholeBoard();
			} else {
				if (spot.occupiedBy(chess.getWhoseTurn())) {
					chosen = spot.getPiece();
					ArrayList<Square> reachable = chess.reachable(chosen);
					reachable.add(spot);
					ArrayList<SquareLabel> hightlight = getAllViewLabels(reachable, chooesView(whiteOrBlack));
					clickedView.highLightAll(hightlight);

					if (spot.getPiece().isType(Pawn.class))
						clickedView.printTemp(spot.toString());
					else
						clickedView.printTemp(spot.getPiece().getType() + spot.toString());

				}
			}
		}

		repaintBothViews();
	}

	public void endGame(EndGame end) {
		if (end == Win.BLACKCHECKMATE || end == Win.WHITECHECKMATE || end == Draw.STALEMENT) {
			whiteView.cleanTemp();
			blackView.cleanTemp();
			whiteView.printOut(chess.lastMoveOutPrint());
			blackView.printOut(chess.lastMoveOutPrint());
		}

		whiteView.setStatusLabelText(end.getDescript());
		whiteView.printOut(end.getPrintOut());
		blackView.setStatusLabelText(end.getDescript());
		blackView.printOut(end.getPrintOut());
		if (end.getResult() > 0)
			whiteView.printOut("Congratuations! you win!");
		else if (end.getResult() < 0)
			blackView.printOut("Congratuations! you win!");
	}

	private void updateGuiToMove(Move previousMove) {
		Collection<Square> board = chess.getAllSquares();
		for (Square sq : board) {
			updateSquare(whiteView, sq);
			updateSquare(blackView, sq);
		}

		ChessViewer pre = chooesView(previousMove.getWhoseTurn() == Player.WHITE);
		ChessViewer next = chooesView(previousMove.getWhoseTurn() == Player.BLACK);

		whiteView.setStatusLabelText(chess.lastMoveDiscript());
		blackView.setStatusLabelText(chess.lastMoveDiscript());
		pre.cleanTemp();
		whiteView.printOut(chess.lastMoveOutPrint());
		blackView.printOut(chess.lastMoveOutPrint());
		next.printOut("Please make your move.");
		pre.printOut("Wait for the " + side(previousMove.getWhoseTurn() == Player.BLACK) + " to make a move");

	}

	public void askForDraw(boolean whiteOrBlack) {
		Draw canClaimDraw = chess.canClaimDraw();
		if (canClaimDraw == null) {
			ChessViewer request = chooesView(whiteOrBlack);
			ChessViewer response = chooesView(!whiteOrBlack);
			if (canAskFordraw(whiteOrBlack)) {
				while (true) {
					response.printOut(side(whiteOrBlack) + " ask for draw, do you agreed?");
					String command = response.getResponse("Do you agree draw?");
					if (command.isEmpty())
						continue;
					if (command.toLowerCase().startsWith("yes")) {
						chess.endGame(Draw.AGREEMENT);
						return;
					} else if (command.toLowerCase().startsWith("no")) {
						setRightToRequestDraw(whiteOrBlack);
						request.printOut("Request declined");
						return;
					}
				}
			} else {
				request.printOut("You cannot request for draw again now.");
			}
		} else {
			chess.endGame(canClaimDraw);
		}
	}

	public static void main(String[] args) {
		new DualViewChessControl();
	}

}
