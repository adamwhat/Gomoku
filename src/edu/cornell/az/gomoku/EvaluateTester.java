package edu.cornell.az.gomoku;

import static org.junit.Assert.*;

import org.junit.Test;

public class EvaluateTester{

	private static Evaluate evaluator = new Evaluate(BoardState.BLACK);

	public void assertAllEqual(Board b, Location[] locations, int expected) {
		for (Location l : locations) {
			try {
				evaluator.setEvalFor(b.getLocation(l));
				assertEquals(expected, evaluator.evaluateBoard(b, l));
			} catch (AssertionError e) {
				System.out.println("Test Failed for the case " + b + "\nAt " + l);
				throw e;
			} finally {
				evaluator.setEvalFor(BoardState.BLACK);
			}
		}
	}

	@Test
	public void testCountHorizontal() {
		Board b = new Board("3,3,b;3,2,b;3,1,b;3,4,b;3,5,b;");
		assertEquals(new BoardStats(5,2), evaluator.count(b, new Location(3,4),0,1,BoardState.BLACK));
		assertEquals(new BoardStats(5,2), evaluator.count(b, new Location(3,4),0,-1,BoardState.BLACK));
		assertEquals(new BoardStats(5,2), evaluator.count(b, new Location(3,5),0,1,BoardState.BLACK));
	}
	@Test
	public void testCountDiagonal() {
		Board b = new Board("3,3,b;2,2,b;4,4,b;5,5,b;6,6,b;");
		assertEquals(new BoardStats(5,2), evaluator.count(b, new Location(2,2),1,1,BoardState.BLACK));
		assertEquals(new BoardStats(5,2), evaluator.count(b, new Location(2,2),-1,-1,BoardState.BLACK));
		assertEquals(new BoardStats(5,2), evaluator.count(b, new Location(4,4),-1,-1,BoardState.BLACK));
	}
	@Test
	public void testevaluatorDefault() {
		Board b = new Board("3,3,b; 3,4,w");
		assertEquals(0, evaluator.evaluateBoard(b, new Location(3,3)));
		assertEquals(0, evaluator.evaluateBoard(b, new Location(3,4)));
	}
	@Test
	public void testMoreThanFive() {
		Board b = new Board("7,10,w;9,10,w;9,12,b;10,11,b;10,12,w;11,10,b;12,8,w;12,9,b;12,10,w;13,8,b;14,7,b;");
		assertAllEqual(b, new Location[]{new Location(9, 12),new Location(10, 11),new Location(11, 10),new Location(12, 9),new Location(13, 8),new Location(14, 7)}, 100000);
	}
	
	@Test
	public void testFiveConnect() {
		Board b = new Board("2,2,b; 3,3,b; 4,4,b; 5,5,b; 6,6,b;");
		assertEquals(100000, evaluator.evaluateBoard(b, new Location(2, 2)));
		assertEquals(100000, evaluator.evaluateBoard(b, new Location(3, 3)));
	}
	
	@Test
	public void testLiveFour() {
		Board b = new Board("6,2,b;6,3,b;6,4,b;6,5,b;11,3,w;11,6,w;12,4,w;");
		// assertEquals(10000, evaluator.evaluateBoard(b, new Location(11,13)));
		assertAllEqual(b, new Location[]{
				new Location(6, 2),
				new Location(6, 4),
				new Location(6, 3),
				new Location(6, 5)
		}, 10000);
	}
	
	
	//@Test
	/*
	public void testTwoDeadFour() {
		Board b = new Board("9,6,b;9,14,b;10,7,b;10,13,b;11,8,b;11,12,b;12,9,b;12,10,w;12,11,b;13,10,w;14,10,w;15,10,w;16,10,w;17,10,w;18,10,w;");
		assertAllEqual(b, new Location[]{
				new Location(12, 9),
				new Location(12, 11),
				new Location(11, 8),
				new Location(11, 12),
				new Location(10, 13),
				new Location(10, 7),
				new Location(9, 14),
				new Location(9, 6)
		}, 10000);
	}*/
	
	@Test
	public void testDeadFourLiveThree() {
		Board b = new Board();
		Board.setBoard(b, "5,9,w;5,11,w;6,10,w;7,9,w;7,11,w;8,8,w;9,6,b;9,7,b;9,8,b;9,9,b;10,8,b;");
		assertAllEqual(b, new Location[]{
				new Location(8, 8),
				new Location(7, 9),
				new Location(6, 10),
				new Location(5, 11),
				new Location(7, 11),
				new Location(5, 9)
		}, 10000);
	}
	
	@Test
	public void testTwoLiveThree() {
		Board b = new Board();
		Board.setBoard(b, "4,14,w;5,13,b;6,10,b;6,12,w;7,9,b;7,11,w;7,12,w;8,8,b;8,10,b;9,11,b;10,10,w;");
		assertAllEqual(b, new Location[]{
				new Location(8, 10),
				new Location(9, 11),
				new Location(7, 9),
				new Location(8, 8),
				new Location(6, 10)
		}, 5000);
	}
	
	@Test
	public void testH2() {
		Board b= new Board("7,11,b;7,12,b;8,11,w;8,12,w;10,13,b;11,10,w;");
		evaluator.setEvalFor(BoardState.BLACK);
		assertEquals(5, evaluator.evaluateBoard(b, new Location(7, 11)));
		assertEquals(5, evaluator.evaluateBoard(b, new Location(7, 12)));
		assertEquals(-5, evaluator.evaluateBoard(b, new Location(8, 11)));
		assertEquals(-5, evaluator.evaluateBoard(b, new Location(8, 12)));
	}
	
	@Test
	public void testS2() {
		Board b= new Board("3,10,w;4,8,b;4,9,w;4,10,b;4,11,w;5,10,b;");
		assertEquals(3, evaluator.evaluateBoard(b, new Location(4, 10)));
		assertEquals(3, evaluator.evaluateBoard(b, new Location(5, 10)));
	}

}
