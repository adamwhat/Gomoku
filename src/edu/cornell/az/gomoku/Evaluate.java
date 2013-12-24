package edu.cornell.az.gomoku;

import java.util.ArrayList;
import java.util.List;

public class Evaluate {
	
	protected static final int[] DX = new int[] {0, 1, 1, 1};
	protected static final int[] DY = new int[] {1, 0, 1, -1};
	
	protected BoardState evalFor;
	
	public Evaluate(BoardState evalFor) {
		setEvalFor(evalFor);
	}
	
	public void setEvalFor(BoardState evalFor) {
		this.evalFor = evalFor;
	}
	
	private void floodFill(Board b, Location loc, BoardState identity, boolean[][] visited, List<Location> res) {
		if (!Board.onBoard(loc.i, loc.j) || visited[loc.i][loc.j] || b.getLocation(loc) != identity) {
			return;
		}
		visited[loc.i][loc.j] = true;
		res.add(loc);
		for (int i = 0; i < DX.length; i++) {
			floodFill(b, new Location(loc.i + DX[i], loc.j + DY[i]), identity, visited, res);
			floodFill(b, new Location(loc.i - DX[i], loc.j - DY[i]), identity, visited, res);
		}
	}

	public int evaluateBoard(Board b, Location loc) {
		List<Location> res = new ArrayList<Location>();
		boolean[][] vis = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];
		floodFill(b, loc, b.getLocation(loc), vis, res);
		int maxScore = 0;
		
		for (Location l : res) {
			maxScore = Math.max(maxScore, evaluateLocation(b, l));
		}
		return maxScore  * (b.getLocation(loc) == evalFor? 1 : -1);
	}

	private int evaluateLocation(Board b, Location loc) {
		assert(b.getLocation(loc) != BoardState.EMPTY);
		int[] s = new int[6];
		int[] h = new int[6];
		int[] score = new int[] {10000000, 10000, 5000, 1000, 500, 200, 100, 50, 10, 5, 3, 0};

		for (int i = 0; i < DX.length; i++) {
			BoardStats stats = count(b, loc, DX[i], DY[i], b.getLocation(loc));
			if (stats.piece < 2) {
				continue;
			}
			
			if (stats.piece >= 5) {
				return score[0];
			}

			if (stats.empty == 2) {
				h[stats.piece]++;
			} else {
				s[stats.piece]++;
			}
		}
		
/*		if (s[5] + h[5] >= 1) {
			return score[0];
		}
*/		
		if (h[4] != 0 || s[4] > 2 || s[4] != 0 && h[3] != 0) {
			return score[1];
		}
		
		if (h[3] >= 2) {
			return score[2];
		}
		
		if (s[3] != 0 && h[3] != 0) {
			return score[3];
		}
		
		if (s[4] != 0) {
			return score[4];
		}
		
		if (h[3] != 0) {
			return score[5];
		}
		
		if (h[2] >= 2) {
			return score[6];
		}
		
		if (s[3] != 0) {
			return score[7];
		}
		
		if (h[2] != 0 && s[2] != 0) {
			return score[8];
		}
		
		if (h[2] != 0) {
			return score[9];
		}
		
		if (s[2] != 0) {
			return score[10];
		}

		return 0;
	}
	
	
	public BoardStats count(Board b, Location loc, int dx, int dy, BoardState myIdentity) {
		int piece = 1;
		int empty = 0;
		int i = 0;
		for (i = 1; Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) == myIdentity; i++, piece++);
		if (Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) == BoardState.EMPTY) empty++;
		for (i = -1; Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) == myIdentity; i--, piece++);
		if (Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) == BoardState.EMPTY) empty++;
		// for (i = 1; Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) == BoardState.EMPTY; i++, empty++);
		// for (i = -1; Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) == BoardState.EMPTY; i--, empty++);
		/*
		if (piece < 5 && !makeSense(b, loc, dx, dy, myIdentity)) {
			return new BoardStats(0, 1);
		}*/
		return new BoardStats(piece, empty);
	}
	
	public boolean makeSense(Board b, Location loc, int dx, int dy, BoardState myIdentity) {
		int piece = 1;
		for (int i = 1; Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) != Board.opponentOf(myIdentity) ; i++, piece++);
		for (int i = -1; Board.onBoard(loc.i + dx * i, loc.j + dy * i) && b.getLocation(loc.i+dx*i,loc.j+dy*i) == Board.opponentOf(myIdentity); i--, piece++);
		return piece >= 5;
	}
}

class BoardStats {
	public int piece;
	public int empty;
	public BoardStats(int piece, int empty) {
		this.piece = piece;
		this.empty = empty;
	}

	public String toString() {
		return "(" + piece + ", " + empty + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + empty;
		result = prime * result + piece;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoardStats other = (BoardStats) obj;
		if (empty != other.empty)
			return false;
		if (piece != other.piece)
			return false;
		return true;
	}
}