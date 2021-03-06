package edu.cornell.az.gomoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ubiety.ubigraph.UbigraphClient;

public class GomokuAI {
	private BoardState myIdentity;
	// private List<Location> moves = new ArrayList<>();
	private volatile int maxLevel = 5;
	private volatile int maxCandidateLocations = 12;
	private volatile boolean drawFullTree = false;
	private volatile boolean defend = false;
	private ArrayList<Integer> shouldPrune = new ArrayList<>();
	private int edgeStyleId;
	private Evaluate evaluator;
	private boolean cmdEntry = false;
	// private static final int threshold = -200;

	UbigraphClient visualClient = new UbigraphClient();

	public GomokuAI(BoardState me) {
		myIdentity = me;
		evaluator = new EvaluateNaive(me);
		defend = false;
	}
	
	public GomokuAI(BoardState me, Evaluate eval) {
		myIdentity = me;
		evaluator = eval;
		cmdEntry = true;
		// defend = true;
	}
	
	public BoardState getMyIdentity() {
		return myIdentity;
	}

	public void setMyIdentity(BoardState myIdentity) {
		this.myIdentity = myIdentity;
		this.evaluator.setEvalFor(myIdentity);
	}

	public Evaluate getEvaluator() {
		return evaluator;
	}
	

	public int getMaxCandidateLocations() {
		return maxCandidateLocations;
	}

	public void setMaxCandidateLocations(int maxCandidateLocations) {
		this.maxCandidateLocations = maxCandidateLocations;
	}

	public boolean isDefend() {
		return defend;
	}

	public void setDefend(boolean defend) {
		this.defend = defend;
	}

	private volatile boolean draw = false;

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public boolean isDraw() {
		return draw;
	}

	public void setDraw(boolean draw) {
		this.draw = draw;
	}

	public boolean isDrawFullTree() {
		return drawFullTree;
	}

	public void setDrawFullTree(boolean drawFullTree) {
		this.drawFullTree = drawFullTree;
	}

	private List<Location> getFeasibleLocations(Board board, int number,
			Location lastMove) {
		return getFeasibleLocations(board, number, lastMove, defend);
	}
	
	public void prune() {
		for (int i=shouldPrune.size()-1;i>=0;i--) {
			int vId = shouldPrune.get(i);
			visualClient.setVertexAttribute(vId, "color", "#D0F16C");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			visualClient.removeVertex(vId);
		}
	}
	
	private List<Location> getFeasibleLocations(Board board, int number,
			Location lastMove, boolean alwaysDefend) {
		/* Defend first */
		List<Location> defend = new ArrayList<>(number);
		List<Location> attack = new ArrayList<>();
		List<Location> vacant = new ArrayList<>();
		BoardState opponent = Board.opponentOf(myIdentity);
		int[] di = new int[] { -1, -1, -1, 0, 0, 1, 1, 1 };
		int[] dj = new int[] { -1, 0, 1, -1, 1, -1, 0, 1 };
		for (int i=0;i<Board.BOARD_SIZE;i++) {
			for (int j=0;j<Board.BOARD_SIZE;j++) {
				if (board.getLocation(i, j) == BoardState.EMPTY) {
					Location cur = new Location(i, j);
					boolean mark = false;
					for (int k=0;k<di.length;k++) {
						int ni = i+di[k];
						int nj = j+dj[k];
						if (Board.onBoard(ni, nj)) {
							if (board.getLocation(ni, nj) == opponent) {
								defend.add(cur);
								mark = true;
								break;
							} else if (board.getLocation(ni, nj) == myIdentity) {
								attack.add(cur);
								mark = true;
								break;
							}
						}
					}
					if (!mark) {
						vacant.add(cur);
					}
				}
			}
		}
		for (Location l : attack) {
			if (defend.size() >= number) {
				break;
			}
			defend.add(l);
		}
		for (Location l : vacant) {
			if (defend.size() >= number) {
				break;
			}
			defend.add(l);
		}
		if (!alwaysDefend && defend.size() > number) {
			return defend.subList(0, number);
		} else {
			return defend;
		}
	}
	
	/*
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
	*/

	public Location calculateNextMove(Board board, Location opponentMove) {
		shouldPrune.clear();
		// TODO opponentMove could be null
		int root = 0;
		if (draw) {
			visualClient.clear();
			edgeStyleId = visualClient.newEdgeStyle(0);
			visualClient.setEdgeStyleAttribute(edgeStyleId, "oriented", "true");
			root = drawVertex(shapeFromTurn(myIdentity), "#FF0000", null);
			visualClient.setVertexAttribute(root, "size", "4.0");
		}
		double maximum_score = Double.NEGATIVE_INFINITY;
		Location maximum_loc = null;
		Board newBoard = board.clone();
		List<Location> feasibleLocs = getFeasibleLocations(newBoard, maxCandidateLocations, opponentMove);
		if (cmdEntry) {
			Collections.shuffle(feasibleLocs);
		}
		for (Location l : feasibleLocs) {
			newBoard.setLocation(l, myIdentity);
			double score = evaluate(newBoard, Board.opponentOf(myIdentity),
					l, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY, 0, root);
			if (score > maximum_score) {
				maximum_score = score;
				maximum_loc = l;
			}
			newBoard.setLocation(l, BoardState.EMPTY);
		}
		//System.out.println("Max_Score = " + maximum_score);
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
	
	private String colorFromTurn(BoardState turn) {
		if (turn != myIdentity) {
			return "#00A0B0";
		} else {
			return "#EB6841";
		}
	}

	private double evaluate(Board board, BoardState turn, Location lastMove,
			double alpha, double beta, int level, int parent) {
		// Connect current node to parent
		int currentNode = parent;
		if (draw) {
			currentNode = createAndConnect(parent, shapeFromTurn(turn),
					colorFromTurn(turn), null);
			if (drawFullTree && alpha >= beta) {
				shouldPrune.add(currentNode);
			}
		}

		int res = evaluator.evaluateBoard(board, lastMove);
		if (level >= maxLevel || Math.abs(res) > 5000) {
			return res;
		}

		if (turn == myIdentity) {
			// Maximizer
			for (Location l : getFeasibleLocations(board,
					maxCandidateLocations, lastMove)) {
				if (alpha >= beta) {
					if (!draw || !drawFullTree) {
						break;
					}
				}
				board.setLocation(l, turn);
				alpha = Math.max(
						alpha,
						evaluate(board, Board.opponentOf(turn), l, alpha, beta,
								level + 1, currentNode));
				board.setLocation(l, BoardState.EMPTY);
			}
			return alpha;
		} else {
			// Minimizer
			for (Location l : getFeasibleLocations(board,
					maxCandidateLocations, lastMove)) {
				if (alpha >= beta) {
					break;
				}
				board.setLocation(l, turn);
				beta = Math.min(
						beta,
						evaluate(board, Board.opponentOf(turn), l, alpha, beta,
								level + 1, currentNode));
				board.setLocation(l, BoardState.EMPTY);
			}
			return beta;
		}
	}

	public void reset() {

	}
}
