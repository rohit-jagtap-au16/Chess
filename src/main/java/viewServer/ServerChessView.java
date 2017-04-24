package viewServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import controller.DualViewChessControl;
import view.IChessViewer;
import view.IChessViewerControl;

public class ServerChessView implements IChessViewer {
	private BoardData board;
	private ActionData action;
	private String roomID;
	private boolean isTurn;

	@Exclude
	private DatabaseReference ref;

	public ServerChessView() {
	}

	public static ServerChessView newInstance(DatabaseReference firebaseReference, String roomID) {
		ServerChessView p = new ServerChessView();
		p.board = BoardData.newInstance(firebaseReference.child("board"));
		p.ref = firebaseReference;
		p.roomID = roomID;
		return p;
	}

	@Override
	public void printOut(String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void printTemp(String temp) {
		// TODO Auto-generated method stub
	}

	@Override
	public void cleanTemp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatusLabelText(String str) {
		// TODO Auto-generated method stub
	}

	@Override
	public void highLight(int file, int rank) {
		this.board.highLight(file, rank);
	}

	@Override
	public void deHighLightWholeBoard() {
		this.board.deHighLightWholeBoard();
	}

	@Override
	public String getResponse(String message) {
		return null;
	}

	@Override
	public void repaint() {
		this.board.pushToFirebase();
	}

	@Override
	public void upDatePiece(int file, int rank, char pieceType, boolean whiteOrBlack) {
		this.board.updatePiece(file, rank, PieceData.newInstance("" + pieceType, whiteOrBlack));
	}

	@Override
	public void clearLabel(int file, int rank) {
		this.board.updatePiece(file, rank, PieceData.newInstance(null, false));
	}

	@Override
	public void initializeViewController(IChessViewerControl controller) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws FileNotFoundException {
		String roomId = "MyRoomID2";
		ServerChessView whiteview = ServerChessView
				.newInstance(FirebaseDatabase.getInstance().getReference(roomId + "/white"), roomId);
		ServerChessView blackview = ServerChessView
				.newInstance(FirebaseDatabase.getInstance().getReference(roomId + "/black"), roomId);
		new DualViewChessControl(whiteview, blackview);
		while (true)
			;
	}

	static {
		try {
			FileInputStream serviceAccount = new FileInputStream("ServiceAccount.json");
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
					.setDatabaseUrl("https://chess-49b54.firebaseio.com/").build();

			FirebaseApp.initializeApp(options);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
