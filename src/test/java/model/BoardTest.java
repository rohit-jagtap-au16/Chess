package model;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoardTest {

	@Test
	public void testSpotAt() {
		Board board = new Board();

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Square square = board.spotAt(i, j);
				char a = (char) (i - 1 + 'a');
				char b = (char) (j - 1 + '1');

				assertEquals("" + a + b, square.toString());
			}
		}
	}

	@Test
	public void testgetSquare() {
		Board board = new Board();
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Square square = board.spotAt(i, j);
				char a = (char) (i - 1 + 'a');
				char b = (char) (j - 1 + '1');
				Square squareGot = board.getSquare("" + a + b);

				assertEquals(square, squareGot);
			}
		}
	}

}
