package edu.cornell.az.gomoku;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ubiety.ubigraph.UbigraphClient;

public class GomokuAI {
	private BoardState myIdentity;
	private List<Location> moves = new ArrayList<>();
	private int maxLevel = 1;
	public static final int MAX_CANDIDATE_LOCATIONS = 20;
	private volatile boolean draw = false;
	private volatile boolean drawFullTree = false;
	private List<Integer> shouldPrune = new LinkedList<>();
	private int edgeStyleId;
	private Evaluate evaluator;
	private static final int threshold = -200;

	UbigraphClient visualClient = new UbigraphClient();

	public GomokuAI(BoardState me) {
		myIdentity = me;
		evaluator = new EvaluateNaive(me);
	}
	
	public Evaluate getEvaluator() {
		return evaluator;
	}

	private List<Location> getFeasibleLocations(Board board, int number,
			Location lastMove) {
		ArrayList<Location> locs = new ArrayList<>();
		LinkedList<Location> queue = new LinkedList<>();
		queue.add(lastMove);
		boolean[][] visited = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];

		while (!queue.isEmpty() && locs.size() < number) {
			Location loc = queue.poll();
			if (board.getLocation(loc) == BoardState.EMPTY) {
				locs.add(loc);
			}
			int[] di = new int[] { -1, -1, -1, 0, 0, 1, 1, 1 };
			int[] dj = new int[] { -1, 0, 1, -1, 1, -1, 0, 1 };
			for (int i = 0; i < 8; i++) {
				if (Board.onBoard(loc.i + di[i], loc.j + dj[i])
						&& !visited[loc.i + di[i]][loc.j + dj[i]]) {
					visited[loc.i + di[i]][loc.j + dj[i]] = true;
					queue.add(new Location(loc.i + di[i], loc.j + dj[i]));
				}
			}
		}

		return locs;
	}

	public Location calculateNextMove(Board board, Location opponentMove) {
		// TODO opponentMove could be null
		int root = 0;
		if (draw) {
			visualClient.clear();
			edgeStyleId = visualClient.newEdgeStyle(0);
			visualClient.setEdgeStyleAttribute(edgeStyleId, "oriented", "true");
			visualClient.setEdgeStyleAttribute(edgeStyleId, "stroke", "dashed");
			root = drawVertex(shapeFromTurn(myIdentity), "#FF0000", null);
		}
		double maximum_score = Double.NEGATIVE_INFINITY;
		Location maximum_loc = null;
		Board newBoard = board.clone();
		for (Location l : getFeasibleLocations(newBoard, MAX_CANDIDATE_LOCATIONS,
				opponentMove)) {
			newBoard.setLocation(l, myIdentity);
			double score = evaluate(newBoard, Board.opponentOf(myIdentity),
					l, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY, maxLevel, root);
			if (score > maximum_score) {
				maximum_score = score;
				maximum_loc = l;
			}
			newBoard.setLocation(l, BoardState.EMPTY);
		}
		System.out.println("Max_Score = " + maximum_score);
		return maximum_loc;
	}

	private int drawVertex(String shape, String color, String label) {
		int curNode = visualClient.newVertex();
		if (shape != null)
			visualClient.setVertexAttribute(curNode, "shape", shape);
		if (color != null)
			visualClient.setVertexAttribute(curNode, "color", color);
		if (label != null)
			visualClient.setVertexAttribute(curNode, "label", label);
		return curNode;
	}

	private int createAndConnect(int parent, String shape, String color,
			String label) {
		int curNode = drawVertex(shape, color, label);
		visualClient.changeEdgeStyle(visualClient.newEdge(parent, curNode),
				edgeStyleId);
		return curNode;
	}

	private String shapeFromTurn(BoardState turn) {
		if (turn == myIdentity) {
			return "cube";
		} else {
			return "sphere";
		}
	}

	private double evaluate(Board board, BoardState turn, Location lastMove,
			double alpha, double beta, int level, int parent) {
		// Connect current node to parent
		int currentNode = parent;
		if (draw) {
			currentNode = createAndConnect(currentNode, shapeFromTurn(turn),
					null, null);
			if (drawFullTree && alpha >= beta) {
				shouldPrune.add(currentNode);
			}
		}
		if (level == 0) {
			int res = evaluator.evaluateBoard(board, lastMove);
			// System.out.println(res);
			return res;
		}


		int res = evaluator.evaluateBoard(board, lastMove);
		if (Math.abs(res) > 5000) {
			return res;
		}

		if (turn == myIdentity) {
			// Maximizer
			for (Location l : getFeasibleLocations(board,
					MAX_CANDIDATE_LOCATIONS, lastMove)) {
				if (alpha >= beta) {
					if (!draw || !drawFullTree) {
						break;
					}
				}
				board.setLocation(l, turn);
				alpha = Math.max(
						alpha,
						evaluate(board, Board.opponentOf(turn), l, alpha, beta,
								level - 1, currentNode));
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
								level - 1, currentNode));
				board.setLocation(l, BoardState.EMPTY);
			}
			return beta;
		}
	}

	public void reset() {

	}
}
