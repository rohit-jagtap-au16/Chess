package viewServer;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BoardDataTest {
	private BoardData board;
	
	@Before
	public void setupBoardData() {
		board = BoardData.newInstance();
		
		// Check each piece
		for(List<PieceData> list : board.pieces) {
			for(PieceData p : list) {
				assertEquals(null, p.type);
			}
		}
	}
	
	@Test
	public void testSetupBoardData() { }
	
	@Test
	public void testHighlight11() {
		board.highLight(1, 1);
		
		assertTrue(board.pieces.get(0).get(0).isHightLight);
	}
	
	@Test
	public void testHighlight88() {
		board.highLight(8, 8);
		
		assertTrue(board.pieces.get(7).get(7).isHightLight);
	}
	
	@Test
	public void testHighlight57() {
		board.highLight(5, 7);
		
		assertTrue(board.pieces.get(4).get(6).isHightLight);
	}
	
	@Test
	public void testDeHighLightWholeBoard() {
		board.highLight(1, 1);
		board.highLight(2, 5);
		board.highLight(8, 8);
		
		board.deHighLightWholeBoard();
		
		for(List<PieceData> list : board.pieces) {
			for(PieceData p : list) {
				assertFalse(p.isHightLight);
			}
		}
	}
	
	@Test
	public void testUpdatePiece11() {
		PieceData p = PieceData.newInstance("K", false);
		board.updatePiece(1, 1, p);
		
		assertEquals(p, board.pieces.get(0).get(0));
	}
	
	@Test
	public void testUpdatePiece57() {
		PieceData p = PieceData.newInstance("N", true);
		board.updatePiece(5, 7, p);
		
		assertEquals(p, board.pieces.get(4).get(6));
	}

}
