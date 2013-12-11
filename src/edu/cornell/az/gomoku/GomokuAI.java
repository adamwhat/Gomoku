package edu.cornell.az.gomoku;

import java.util.ArrayList;
import java.util.List;

public class GomokuAI {
	private BoardState myIdentity;
	private List<Location> moves = new ArrayList<>();
	private int maxLevel = 6;
	public static final int MAX_CANDIDATE_LOCATIONS = 10;
	public GomokuAI(BoardState me) {
		myIdentity = me;
	}
	
	public static BoardState opponentOf(BoardState x) {
		if (x == BoardState.BLACK) {
			return BoardState.WHITE;
		}
		if (x == BoardState.WHITE) {
			return BoardState.BLACK;
		}
		throw new AssertionError("Empty cell has no opponent");
	}
	
	private List<Location> getFeasibleLocations(Board board, int number) {
		return null;
	}
	public Location calculateNextMove(Board board, Location opponentMove) {
		// TODO opponentMove could be null
		double maximum_score = Double.NEGATIVE_INFINITY;
		Location maximum_loc = null;
		for (Location l : getFeasibleLocations(board, MAX_CANDIDATE_LOCATIONS)) {
			board.setLocation(l, myIdentity);
			double score = evaluate(board, opponentOf(myIdentity), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, maxLevel);
			if (score > maximum_score) {
				maximum_score = score;
				maximum_loc = l;
			}
			board.setLocation(l, BoardState.EMPTY);
		}
		return maximum_loc;
	}
	
	private double evaluate(Board board, BoardState turn, double alpha, double beta, int level) {
		if (level == 0) {
			return 0.0;
		}
		if (turn == myIdentity) {
			// Maximizer
			for (Location l : getFeasibleLocations(board, MAX_CANDIDATE_LOCATIONS)) {
				if (alpha >= beta) {
					break;
				}
				board.setLocation(l, turn);
				alpha = Math.max(alpha, evaluate(board, opponentOf(turn), alpha, beta, level-1));
				board.setLocation(l, BoardState.EMPTY);
			}
			return alpha;
		} else {
			// Minimizer
			for (Location l : getFeasibleLocations(board, MAX_CANDIDATE_LOCATIONS)) {
				if (alpha >= beta) {
					break;
				}
				board.setLocation(l, turn);
				beta = Math.min(alpha, evaluate(board, opponentOf(turn), alpha, beta, level-1));
				board.setLocation(l, BoardState.EMPTY);
			}
			return beta;
		}
	}
	
	public void reset() {
		
	}
}
