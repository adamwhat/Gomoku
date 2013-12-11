package edu.cornell.az.gomoku;

import java.util.Arrays;

enum BoardState {
	BLACK, WHITE, EMPTY;

}

class Location {
	public int i;
	public int j;
}

public class Board {
	public static final int BOARD_SIZE = 19;

	private BoardState[][] board;
	
	public Board() {
		board = new BoardState[BOARD_SIZE][BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE; i++) {
			Arrays.fill(board[i], BoardState.EMPTY);
		}
	}
	
	public BoardState getLocation(int i, int j) {
		return board[i][j]; 
	}
	
	public void setLocation(Location l, BoardState state) {
		setLocation(l.i, l.j, state);
	}
	
	public boolean onBoard(int i, int j) {
		return i>=0 && j>=0 && i < BOARD_SIZE && j < BOARD_SIZE;
	}
	
	public void placeAtLocation(int i, int j, BoardState state) {
		if (board[i][j] != BoardState.EMPTY) {
			throw new IllegalArgumentException("(" + i + ", " + j + ") was occupied");
		}
		setLocation(i, j, state);
	}
	
	public void setLocation(int i, int j, BoardState state) {
		board[i][j] = state;
	}
}
