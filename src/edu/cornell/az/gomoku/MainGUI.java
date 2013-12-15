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
		frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		frame.setTitle("Gomoku");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final GomokuPanel panel = new GomokuPanel(BoardState.BLACK);
		
		JPanel side = new JPanel();
		JButton saveButton = new JButton("Save Board");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.saveBoard();
			}});
		JButton clearButton = new JButton("Clear Board");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.clearBoard();
			}});
		JButton constructBoardButton = new JButton("Construct");
		constructBoardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog("Input Board String");
				panel.constructBoardFromString(s);
			}
		});
		
		JCheckBox cbox = new JCheckBox("Test Mode");
		cbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				panel.setTestMode(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		// cbox.doClick();
		
		JButton evaluateButton = new JButton("Evaluate");
		evaluateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.evaluateBoard();
			}
		});
		
		side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
		side.add(saveButton);
		side.add(clearButton);
		side.add(constructBoardButton);
		side.add(evaluateButton);
		side.add(cbox);
		
		JPanel totalPanel = new JPanel();
		totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.X_AXIS));
		totalPanel.add(panel);
		totalPanel.add(side);
		
		frame.add(totalPanel);
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
	
	private boolean testMode = false;

	private Random random = new Random();

	public GomokuPanel(BoardState identity) {
		super();
		board = new Board();
		this.identity = identity;
		this.testMode = false;
		working = false;
		playerLastMove = null;
		gomokuAI = new GomokuAI(Board.opponentOf(identity));
		addMouseListener(new GomokuListener());
	}
	
	public boolean getTestMode() {
		return testMode;
	}
	
	public void constructBoardFromString(String cmd) {
		Board.setBoard(board, cmd);
		repaint();
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}
	
	public void saveBoard() {
		System.out.println(board);
	}

	public void clearBoard() {
		board.clear();
		repaint();
	}
	
	public void evaluateBoard() {
		int res = gomokuAI.getEvaluator().evaluateBoard(board, playerLastMove);
		System.out.println("Score = " + res);
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
			if (!testMode) {
				new AIWorker().execute();
			} else {
				System.out.format("new Location(%d, %d),\n", row, col);
				identity = Board.opponentOf(identity);
			}
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