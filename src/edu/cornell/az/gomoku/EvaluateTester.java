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
		setBoard(b, "3,3,b; 3,4,w");
		assertEquals(0, Evaluate.evaluateBoard(b, new Location(3,3)));
		assertEquals(0, Evaluate.evaluateBoard(b, new Location(3,4)));
	}
	public void assertAllEqual(Board b, Location[] locations, int expected) {
		for (Location l : locations) {
			assertEquals(expected, Evaluate.evaluateBoard(b, l));
		}
	}
	@Test
	public void testMoreThanFive() {
		Board b = new Board();
		setBoard(b, "7,10,w;9,10,w;9,12,b;10,11,b;10,12,w;11,10,b;12,8,w;12,9,b;12,10,w;13,8,b;14,7,b;");
		assertAllEqual(b, new Location[]{new Location(9, 12),new Location(10, 11),new Location(11, 10),new Location(12, 9),new Location(13, 8),new Location(14, 7)}, 100000);
	}
	
	@Test
	public void testFiveConnect() {
		Board b = new Board();
		setBoard(b, "2,2,b; 3,3,b; 4,4,b; 5,5,b; 6,6,b;");
		assertEquals(100000, Evaluate.evaluateBoard(b, new Location(2, 2)));
		assertEquals(100000, Evaluate.evaluateBoard(b, new Location(3, 3)));
	}
	
	@Test
	public void testLiveFour() {
		Board b = new Board();
		setBoard(b, "10,12,b;11,13,b;11,15,w;12,13,w;12,14,b;13,13,w;13,15,b;");
		assertAllEqual(b, new Location[]{
				new Location(12, 14),
				new Location(11, 13),
				new Location(13, 15),
				new Location(10, 12)
		}, 10000);
	}
	
	@Test
	public void testTwoDeadFour() {
		
	}

}
