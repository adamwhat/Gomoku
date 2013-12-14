package edu.cornell.az.gomoku;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ubiety.ubigraph.UbigraphClient;

public class GomokuAI {
	private BoardState myIdentity;
	private List<Location> moves = new ArrayList<>();
	private int maxLevel = 0;
	public static final int MAX_CANDIDATE_LOCATIONS = 10;
	// control visualization;
	private volatile boolean drawPrunedTree = false;
	private volatile boolean drawFullTree = false;

	UbigraphClient visualClient = new UbigraphClient();

	public GomokuAI(BoardState me) {
		myIdentity = me;
	}

	private List<Location> getFeasibleLocations(Board board, int number,
			Location lastMove) {
		ArrayList<Location> locs = new ArrayList<>();
		LinkedList<Location> queue = new LinkedList<>();
		queue.add(lastMove);
		boolean[][] visited = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];

		while (!queue.isEmpty() && locs.size() < number) {
			Location loc = queue.poll();
			visited[loc.i][loc.j] = true;
			if (board.getLocation(loc) == BoardState.EMPTY) {
				locs.add(loc);
			}
			int[] di = new int[] { -1, -1, -1, 0, 0, 1, 1, 1 };
			int[] dj = new int[] { -1, 0, 1, -1, 1, -1, 0, 1 };
			for (int i = 0; i < 8; i++) {
				if (Board.onBoard(loc.i+di[i], loc.j + dj[i]) && !visited[loc.i + di[i]][loc.j + dj[i]]) {
					queue.add(new Location(loc.i + di[i], loc.j + dj[i]));
				}
			}
		}

		return locs;
	}

	public Location calculateNextMove(Board board, Location opponentMove) {
		// TODO opponentMove could be null
		visualClient.clear();
		int root_fulltree = 0, root_prunedtree = 0;
		if (drawFullTree) {
			root_fulltree = visualClient.newVertex();
		}
		if (drawPrunedTree) {
			root_prunedtree = visualClient.newVertex();
		}
		double maximum_score = Double.NEGATIVE_INFINITY;
		Location maximum_loc = null;
		for (Location l : getFeasibleLocations(board, MAX_CANDIDATE_LOCATIONS,
				opponentMove)) {
			board.setLocation(l, myIdentity);
			double score = evaluate(board, Board.opponentOf(myIdentity),
					opponentMove, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY, maxLevel, root_fulltree,
					root_prunedtree);
			if (score > maximum_score) {
				maximum_score = score;
				maximum_loc = l;
			}
			board.setLocation(l, BoardState.EMPTY);
		}
		System.out.println("Max_Score = " + maximum_score);
		return maximum_loc;
	}

	private double evaluate(Board board, BoardState turn, Location lastMove,
			double alpha, double beta, int level, int parent_full,
			int parent_pruned) {
		// Connect current node to parent
		int current_node_full = parent_full, current_node_pruned = parent_pruned;
		if (drawPrunedTree && alpha < beta) {
			current_node_pruned = visualClient.newVertex();
			visualClient.newEdge(parent_pruned, current_node_pruned);
		}
		if (drawFullTree) {
			current_node_full = visualClient.newVertex();
			visualClient.newEdge(parent_full, current_node_full);
		}
		if (level == 0) {
			return Evaluate.evaluateBoard(board, lastMove, Board.opponentOf(turn)) * (Board.opponentOf(turn) == myIdentity? 1 : -1);
		}
		if (turn == myIdentity) {
			// Maximizer
			for (Location l : getFeasibleLocations(board,
					MAX_CANDIDATE_LOCATIONS, lastMove)) {
				if (alpha >= beta && !drawFullTree) {
					break;
				}
				board.setLocation(l, turn);
				alpha = Math.max(
						alpha,
						evaluate(board, Board.opponentOf(turn), l, alpha, beta,
								level - 1, current_node_full,
								current_node_pruned));
				board.setLocation(l, BoardState.EMPTY);
			}
			return alpha;
		} else {
			// Minimizer
			for (Location l : getFeasibleLocations(board,
					MAX_CANDIDATE_LOCATIONS, lastMove)) {
				if (alpha >= beta) {
					break;
				}
				board.setLocation(l, turn);
				beta = Math.min(
						beta,
						evaluate(board, Board.opponentOf(turn), l, alpha, beta,
								level - 1, current_node_full,
								current_node_pruned));
				board.setLocation(l, BoardState.EMPTY);
			}
			return beta;
		}
	}

	public void reset() {

	}
}
