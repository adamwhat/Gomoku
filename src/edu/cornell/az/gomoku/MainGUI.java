package edu.cornell.az.gomoku;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;

public class MainGUI {
	public static void main(String args[]) {
		JFrame frame = new JFrame();

		final int FRAME_WIDTH = 600;
		final int FRAME_HEIGHT = 650;
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setTitle("Gomoku");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GomokuPanel panel = new GomokuPanel(BoardState.BLACK);
		frame.add(panel);

		frame.setVisible(true);
	}
}

class GomokuPanel extends JPanel {
	private final int MARGIN = 2;
	private final double PIECE_FRAC = 0.9;

	private Board board;
	private BoardState identity;
	private Location playerLastMove;
	private boolean working;
	private GomokuAI gomokuAI;

	private Random random = new Random();

	public GomokuPanel(BoardState identity) {
		super();
		board = new Board();
		this.identity = identity;
		working = false;
		playerLastMove = null;
		gomokuAI = new GomokuAI(Board.opponentOf(identity));
		addMouseListener(new GomokuListener());
	}

	class AIWorker extends SwingWorker<Boolean, String> {
		@Override
		protected Boolean doInBackground() throws Exception {
			try {
				if (working) {
					System.out.println("Error: Shouldn't populate a new thread.");
					return false;
				}
				working = true;
				Location loc = gomokuAI.calculateNextMove(board, playerLastMove);
				board.placeAtLocation(loc.i, loc.j, Board.opponentOf(identity));
				repaint();
				return true;
			} catch (Exception failure) {
				working = false;
				System.out.println(Arrays.toString(failure.getStackTrace()));
				return false;
			}
		}

		@Override
		protected void done() {
			working = false;
			repaint();
		}
	}

	class GomokuListener extends MouseAdapter {

		public void mouseReleased(MouseEvent e) {
			if (working) {
				return;
			}

			double panelWidth = getWidth();
			double panelHeight = getHeight();
			double boardWidth = Math.min(panelWidth, panelHeight) - 2 * MARGIN;
			double squareWidth = boardWidth / Board.BOARD_SIZE;
			double pieceDiameter = PIECE_FRAC * squareWidth;
			double xLeft = (panelWidth - boardWidth) / 2 + MARGIN;
			double yTop = (panelHeight - boardWidth) / 2 + MARGIN;
			int col = (int) Math.round((e.getX() - xLeft) / squareWidth - 0.5);
			int row = (int) Math.round((e.getY() - yTop) / squareWidth - 0.5);
			board.placeAtLocation(row, col, identity);
			playerLastMove = new Location(row, col);
			repaint();
			new AIWorker().execute();
		}
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double panelWidth = getWidth();
		double panelHeight = getHeight();

		g2.setColor(new Color(0.925f, 0.670f, 0.34f)); // light wood
		g2.fill(new Rectangle2D.Double(0, 0, panelWidth, panelHeight));

		double boardWidth = Math.min(panelWidth, panelHeight) - 2 * MARGIN;
		double squareWidth = boardWidth / Board.BOARD_SIZE;
		double gridWidth = (Board.BOARD_SIZE - 1) * squareWidth;
		double pieceDiameter = PIECE_FRAC * squareWidth;
		boardWidth -= pieceDiameter;
		double xLeft = (panelWidth - boardWidth) / 2 + MARGIN;
		double yTop = (panelHeight - boardWidth) / 2 + MARGIN;

		g2.setColor(Color.BLACK);
		for (int i = 0; i < Board.BOARD_SIZE; i++) {
			double offset = i * squareWidth;
			g2.draw(new Line2D.Double(xLeft, yTop + offset, xLeft + gridWidth, yTop + offset));
			g2.draw(new Line2D.Double(xLeft + offset, yTop, xLeft + offset, yTop + gridWidth));
		}

		for (int row = 0; row < Board.BOARD_SIZE; row++) {
			for (int col = 0; col < Board.BOARD_SIZE; col++) {
				BoardState piece = board.getLocation(row, col);
				if (piece != BoardState.EMPTY) {
					Color c = (piece == BoardState.BLACK) ? Color.BLACK : Color.WHITE;
					g2.setColor(c);
					double xCenter = xLeft + col * squareWidth;
					double yCenter = yTop + row * squareWidth;
					Ellipse2D.Double circle = new Ellipse2D.Double(xCenter - pieceDiameter / 2, 
							yCenter - pieceDiameter / 2, 
							pieceDiameter, 
							pieceDiameter);
					g2.fill(circle);
					g2.setColor(Color.black);
					g2.draw(circle);
				}
			}
		}
	}
}