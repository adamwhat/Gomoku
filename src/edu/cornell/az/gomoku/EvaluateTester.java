package edu.cornell.az.gomoku;

import static org.junit.Assert.*;

import org.junit.Test;

public class EvaluateTester {

	private static void setBoard(Board b, String cmd) {
		String[] t = cmd.trim().split(";");
		for(String s : t) {
			String[] p = s.trim().split(",");
			if (p.length <= 1) {
				continue;
			}
			assert(p.length == 3);
			assert(p[2].toUpperCase().equals("B") || p[2].toUpperCase().equals("W"));
			b.placeAtLocation(Integer.parseInt(p[0].trim()),
							  Integer.parseInt(p[1].trim()), 
							  p[2].toUpperCase().equals("B")?BoardState.BLACK:BoardState.WHITE);
		}
	}
	@Test
	public void testCountHorizontal() {
		Board b = new Board();
		b.placeAtLocation(3, 3, BoardState.BLACK);
		b.placeAtLocation(3, 2, BoardState.BLACK);
		b.placeAtLocation(3, 1, BoardState.BLACK);
		b.placeAtLocation(3, 5, BoardState.BLACK);
		assertEquals(new BoardStats(5,0), Evaluate.count(b, new Location(3,4),0,1,BoardState.BLACK));
		assertEquals(new BoardStats(5,0), Evaluate.count(b, new Location(3,4),0,-1,BoardState.BLACK));
	}
	@Test
	public void testCountDiagonal() {
		Board b = new Board();
		b.placeAtLocation(3, 3, BoardState.BLACK);
		b.placeAtLocation(4, 4, BoardState.BLACK);
		b.placeAtLocation(5, 5, BoardState.BLACK);
		b.placeAtLocation(6, 6, BoardState.BLACK);
		assertEquals(new BoardStats(5,2), Evaluate.count(b, new Location(2,2),1,1,BoardState.BLACK));
		assertEquals(new BoardStats(5,2), Evaluate.count(b, new Location(2,2),-1,-1,BoardState.BLACK));
	}
	@Test
	public void testEvaluateDefault() {
		Board b = new Board();
		setBoard(b, "3,3,b; 3,4,b");
		assertEquals(0, Evaluate.evaluateBoard(b, new Location(3,3)));
	}
	
	@Test
	public void testFiveConnect() {
		Board b = new Board();
		setBoard(b, "2,2,b; 3,3,b; 4,4,b; 5,5,b; 6,6,b; 7,7,b;");
	}

}
