package edu.cornell.az.gomoku;

import java.util.Arrays;

enum BoardState {
	BLACK, WHITE, EMPTY;

}

class Location {
	public int i;
	public int j;
	public Location() {
		i = j = 0;
	}
	
	public Location(int i, int j) {
		this.i = i;
		this.j = j;
	}
	
	public String toString() {
		return "Location: (" + i + ", " + j +")";
	}
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
	
	public BoardState getLocation(Location l) {
		return getLocation(l.i, l.j);
	}
	
	public void setLocation(Location l, BoardState state) {
		// System.out.println("Set (" + l.i + ", " + l.j + ") = " + state);
		setLocation(l.i, l.j, state);
	}
	
	public static boolean onBoard(int i, int j) {
		return i>=0 && j>=0 && i < BOARD_SIZE && j < BOARD_SIZE;
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

	public void placeAtLocation(int i, int j, BoardState state) {
		if (i < 0 || j < 0 || i >= BOARD_SIZE || j>= BOARD_SIZE) {
			throw new IllegalArgumentException("(" + i + ", " + j + ") was out of bound");
		}
		if (board[i][j] != BoardState.EMPTY) {
			throw new IllegalArgumentException("(" + i + ", " + j + ") was occupied");
		}
		setLocation(i, j, state);
	}
	
	public void setLocation(int i, int j, BoardState state) {
		if (state == BoardState.EMPTY) {
			assert(board[i][j] != BoardState.EMPTY);
		} else {
			assert(board[i][j] == BoardState.EMPTY);
		}
		board[i][j] = state;
	}
	
	@Override
	public String toString() {
		String res = "";
		for (int i=0;i<BOARD_SIZE;i++) {
			for (int j=0;j<BOARD_SIZE;j++) {
				if (board[i][j] != BoardState.EMPTY) {
					res += String.format("%d,%d,%s;", i, j, board[i][j] == BoardState.BLACK ? "b" : "w");
				}
			}
		}
		return res;
	}
}
