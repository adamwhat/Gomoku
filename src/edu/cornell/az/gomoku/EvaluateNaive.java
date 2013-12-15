package edu.cornell.az.gomoku;

public class EvaluateNaive extends Evaluate {

	public EvaluateNaive(BoardState evalFor) {
		super(evalFor);
	}
	
	private int getScores(int[][] tally) {
		int score = 0;
		if (tally[5][0] > 0 || tally[5][1] > 0) score = 100000;
		else if (tally[4][1] > 0 || tally[4][0] > 1 || (tally[4][0] > 0 && tally[3][1] > 0)) score = 10000;
		else if (tally[3][1] > 1) score = 5000;
		else if (tally[3][0] > 0 && tally[3][1] > 0) score = 1000;
		else if (tally[4][0] > 0) score = 500;
		else if (tally[3][1] > 0) score = 200;
		else if (tally[2][1] > 0) score = 100;
		else if (tally[3][0] > 0) score = 50;
		else if (tally[2][1] > 0 && tally[2][0] > 0) score = 10;
		else if (tally[2][1] > 0) score = 5;
		else if (tally[2][0] > 0) score = 3;
		return score;
	}
	
	public int turnToInt(BoardState s) {
		if (s == BoardState.BLACK) {
			return 1;
		}
		return 0;
	}

	@Override
	public int evaluateBoard(Board b, Location loc) {
		boolean[][] visited = new boolean[Board.BOARD_SIZE][Board.BOARD_SIZE];
		int counts[][][] = new int[2][6][2];
		for (int i=0;i<Board.BOARD_SIZE;i++) {
			for (int j=0;j<Board.BOARD_SIZE;j++) {
				if (b.getLocation(i, j) != BoardState.EMPTY && !visited[i][j]) {
					// Direction
					for (int k=0;k<DX.length;k++) {
						int di = DX[k], dj = DY[k];
						int count = -1;
						boolean livePositive = false, liveNegative = false;
						int ii=i, jj=j;
						BoardState turn = b.getLocation(i, j);
						while (Board.onBoard(ii, jj) && b.getLocation(ii, jj) == turn) {
							count++;
							visited[ii][jj] = true;
							ii += di;
							jj += dj;
						}
						if (Board.onBoard(ii, jj) && b.getLocation(ii, jj) == BoardState.EMPTY) {
							livePositive = true;
						}
						ii=i; jj=j;
						while (Board.onBoard(ii, jj) && b.getLocation(ii, jj) == turn) {
							count++;
							visited[ii][jj] = true;
							ii -= di;
							jj -= dj;
						}
						if (Board.onBoard(ii, jj) && b.getLocation(ii, jj) == BoardState.EMPTY) {
							liveNegative = true;
						}
						if (livePositive || liveNegative) {
							if (count > 5) count = 5;
							counts[turnToInt(turn)][count][livePositive == liveNegative ? 1 : 0]++;
						}
					}
				}
			}
		}
		return getScores(counts[turnToInt(evalFor)]) - getScores(counts[turnToInt(Board.opponentOf(evalFor))]);
	}
	
}
