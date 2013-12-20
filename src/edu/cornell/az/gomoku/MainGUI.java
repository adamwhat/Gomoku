package edu.cornell.az.gomoku;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.*;

public class MainGUI {
	
	/**
	 * Command-line Usage
	 * MainGUI <agent1> <agent2> <level1> <bFractor1> <level2> <bFractor2> <N>
	 * <agent> = Evaluate or EvaluateNaive
	 * <N> = Number of Games
	 * @param args
	 */
	public static void main(String args[]) {
		if (args.length < 1) {
			constructGUI();
		} else {
			if (args.length != 7) {
				System.out.println("Please see comments for usage.");
				return;
			}
			
			Evaluate firstEval = null, secondEval = null;
			switch (args[0]) {
			case "Evaluate":
				firstEval = new Evaluate(BoardState.BLACK);
				break;
			case "EvaluateNaive":
				firstEval = new EvaluateNaive(BoardState.BLACK);
				break;
			default:
				System.out.println("Illegal Agent 1");
				return;
			}
			switch (args[1]) {
			case "Evaluate":
				secondEval = new Evaluate(BoardState.WHITE);
				break;
			case "EvaluateNaive":
				secondEval = new EvaluateNaive(BoardState.WHITE);
				break;
			default:
				System.out.println("Illegal Agent 2");
				return;
			}
			
			GomokuAI firstAI = new GomokuAI(BoardState.BLACK, firstEval);
			GomokuAI secondAI = new GomokuAI(BoardState.WHITE, secondEval);
			firstAI.setMaxLevel(Integer.parseInt(args[2]));
			firstAI.setMaxCandidateLocations(Integer.parseInt(args[3]));
			secondAI.setMaxLevel(Integer.parseInt(args[4]));
			secondAI.setMaxCandidateLocations(Integer.parseInt(args[5]));
			int nGames = Integer.parseInt(args[6]);
			System.out.println(args[0] + " goes first");
			
			long time1 = 0, time2 = 0;
			int ai1_win_first = 0, ai2_win_second = 0; 
			for (int i=0;i<nGames;i++) {
				long[] result = matchAgainst(firstAI, secondAI);
				time1 += result[1];
				time2 += result[2];
				printResult((int)result[0]);
				if (result[0] > 0) {
					ai1_win_first++;
				} else {
					ai2_win_second++;
				}
			}
			int ai2_win_first = 0, ai1_win_second = 0;
			System.out.println(args[1] + " goes first");
			for (int i=0;i<nGames;i++) {
				long[] result = matchAgainst(secondAI, firstAI);
				time1 += result[2];
				time2 += result[1];
				printResult((int)result[0]);
				if (result[0] > 0) {
					ai2_win_first++;
				} else {
					ai1_win_second++;
				}
			}
			System.out.println("Statistics");
			System.out.format("%s Level:%s BF:%s\n", args[0], args[2], args[3]);
			System.out.format("First Win: %d Second Win: %d\n", ai1_win_first, ai1_win_second);
			System.out.format("%s Level:%s BF:%s\n", args[1], args[4], args[5]);
			System.out.format("First Win: %d Second Win: %d\n", ai2_win_first, ai2_win_second);
			System.out.format("%s(%s, %s) used %d ms\n%s(%s, %s) used %d ms\n", 
								args[0], args[2], args[3], time1, 
								args[1], args[4], args[5], time2);
			System.out.format("1 & %d & %d\n2 & %d & %d\n", ai1_win_first, ai1_win_second,
														    ai2_win_first, ai2_win_second);
		}

	}
	
	private static void swapAI(GomokuAI[] ais) {
		GomokuAI tmp = ais[0];
		ais[0] = ais[1];
		ais[1] = tmp;
	}
	
	private static void printResult(int r) {
		if (r > 0) {
			System.out.println("First won");
		} else {
			System.out.println("Second won");
		}
	}
	
	private static long[] matchAgainst(GomokuAI first, GomokuAI second) {
		GomokuAI[] ais = new GomokuAI[]{first, second};
		long[] res = new long[3];
		
		Board board = new Board();
		Location lastLocation = new Location(Board.BOARD_SIZE/2, Board.BOARD_SIZE/2);
		board.setLocation(lastLocation, first.getMyIdentity());
		swapAI(ais);
		int eval = 0;
		int i = 1;
		while (Math.abs(eval = first.getEvaluator().evaluateBoard(board, lastLocation)) <= 10000) {
			long t = System.currentTimeMillis();
			lastLocation = ais[0].calculateNextMove(board, lastLocation);
			res[i+1] += System.currentTimeMillis() - t;
			board.setLocation(lastLocation, ais[0].getMyIdentity());
			swapAI(ais);
			i = 1-i;
		}
		if (eval > 0) {
			res[0] = 1;
		} else {
			res[0] = -1;
		}
		return res;
	}
	
	private static void constructGUI() {
		JFrame frame = new JFrame();

		final int FRAME_WIDTH = 720;
		final int FRAME_HEIGHT = 650;
		final int BUTTON_WIDTH = 120;
		frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		frame.setTitle("Gomoku");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final GomokuPanel panel = new GomokuPanel(BoardState.BLACK);
		panel.setSize(600, 600);
		
		JPanel testPanel = new JPanel();
		JButton saveButton = new JButton("Save Board");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.saveBoard();
			}});
		// saveButton.setMaximumSize(new Dimension(BUTTON_WIDTH, saveButton.getMinimumSize().height));
		JButton clearButton = new JButton("Clear Board");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.clearBoard();
			}});
		// clearButton.setMaximumSize(new Dimension(BUTTON_WIDTH, clearButton.getMinimumSize().height));
		JButton constructBoardButton = new JButton("Construct");
		constructBoardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog("Input Board String");
				panel.constructBoardFromString(s);
			}
		});
		// constructBoardButton.setMaximumSize(new Dimension(BUTTON_WIDTH, constructBoardButton.getMinimumSize().height));
		
		JCheckBox cbox = new JCheckBox("Test Mode");
		cbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				panel.setTestMode(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		// cbox.setMaximumSize(new Dimension(BUTTON_WIDTH, cbox.getMinimumSize().height));
		// cbox.setSelected(true);
		
		JButton evaluateButton = new JButton("Evaluate");
		evaluateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.evaluateBoard();
			}
		});
		// evaluateButton.setMaximumSize(new Dimension(BUTTON_WIDTH, evaluateButton.getMinimumSize().height));
		
		testPanel.setLayout(new GridLayout(0, 1));
		testPanel.add(saveButton);
		testPanel.add(clearButton);
		testPanel.add(constructBoardButton);
		testPanel.add(evaluateButton);
		testPanel.add(cbox);
		testPanel.setBorder(BorderFactory.createTitledBorder("Debug"));
		
		JPanel visPanel = new JPanel();
		// visPanel.setLayout(new BoxLayout(visPanel, BoxLayout.Y_AXIS));
		visPanel.setLayout(new GridLayout(0, 1));
		JSlider bFactor = new JSlider(0, 30, panel.getGomokuAI().getMaxCandidateLocations());
		bFactor.setBorder(BorderFactory.createTitledBorder("Max Branching Factor"));
		bFactor.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				panel.getGomokuAI().setMaxCandidateLocations(((JSlider)(e.getSource())).getValue());
			}
		});
		bFactor.setMajorTickSpacing(10);
		bFactor.setMinorTickSpacing(1);
		bFactor.setPaintTicks(true);
		bFactor.setPaintLabels(true);
		bFactor.setMaximumSize(new Dimension(2*BUTTON_WIDTH, bFactor.getMinimumSize().height));
		
		JSlider searchLevel = new JSlider(0, 8, panel.getGomokuAI().getMaxLevel());
		searchLevel.setBorder(BorderFactory.createTitledBorder("Max Search Level"));
		searchLevel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				panel.getGomokuAI().setMaxLevel(((JSlider)(e.getSource())).getValue());
			}
		});
		searchLevel.setMaximumSize(new Dimension(2*BUTTON_WIDTH, searchLevel.getMinimumSize().height));
		searchLevel.setMajorTickSpacing(2);
		searchLevel.setMinorTickSpacing(1);
		searchLevel.setPaintTicks(true);
		searchLevel.setPaintLabels(true);
		

		final JButton ABPrune = new JButton("A/B Prune");
		ABPrune.setEnabled(panel.getGomokuAI().isDrawFullTree());
		ABPrune.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.getGomokuAI().prune();
			}
		});
		ABPrune.setMaximumSize(new Dimension(BUTTON_WIDTH, ABPrune.getMinimumSize().height));

		final JCheckBox drawFullTreeButton = new JCheckBox("Full Tree");
		drawFullTreeButton.setEnabled(panel.getGomokuAI().isDraw());
		drawFullTreeButton.setSelected(panel.getGomokuAI().isDrawFullTree());
		drawFullTreeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.getGomokuAI().setDrawFullTree(((JCheckBox)(e.getSource())).isSelected());
				ABPrune.setEnabled(((JCheckBox)(e.getSource())).isSelected());
			}
		});
		drawFullTreeButton.setMaximumSize(new Dimension(BUTTON_WIDTH, drawFullTreeButton.getMinimumSize().height));

		JCheckBox enableVis = new JCheckBox("Enable Vis");
		enableVis.setSelected(panel.getGomokuAI().isDraw());
		enableVis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.getGomokuAI().setDraw(((JCheckBox)(e.getSource())).isSelected());
				drawFullTreeButton.setEnabled(((JCheckBox)(e.getSource())).isSelected());
				ABPrune.setEnabled(((JCheckBox)(e.getSource())).isSelected() && drawFullTreeButton.isSelected());
			}
		});
		enableVis.setMaximumSize(new Dimension(BUTTON_WIDTH, enableVis.getMinimumSize().height));
		
		final JCheckBox alwaysDefend = new JCheckBox("Defend Strategy");
		alwaysDefend.setSelected(panel.getGomokuAI().isDefend());
		drawFullTreeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.getGomokuAI().setDefend(((JCheckBox)(e.getSource())).isSelected());
			}
		});
		
		visPanel.add(enableVis);
		visPanel.add(drawFullTreeButton);
		visPanel.add(ABPrune);
		visPanel.add(searchLevel);
		visPanel.add(bFactor);
		visPanel.setBorder(BorderFactory.createTitledBorder("Visualization"));
		visPanel.setSize(120, visPanel.getMinimumSize().height);

		testPanel.add(alwaysDefend);
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new GridLayout(0, 1));
		sidePanel.add(visPanel);
		sidePanel.add(testPanel);
		sidePanel.setMaximumSize(new Dimension(100, FRAME_HEIGHT));
		
		
		JPanel totalPanel = new JPanel();
		totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.X_AXIS));
		totalPanel.add(panel);
		totalPanel.add(sidePanel);
		
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

	private boolean terminate;
	
	private boolean testMode = false;

	private Random random = new Random();


	public GomokuPanel(BoardState identity) {
		super();
		board = new Board();
		this.identity = identity;
		this.testMode = false;
		terminate = false;
		working = false;
		playerLastMove = null;
		gomokuAI = new GomokuAI(Board.opponentOf(identity));
		addMouseListener(new GomokuListener());
	}
	
	public GomokuAI getGomokuAI() {
		return gomokuAI;
	}

	public boolean getTestMode() {
		return testMode;
	}
	
	public void constructBoardFromString(String cmd) {
		Board.setBoard(board, cmd);
		terminate = false;
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
		terminate = false;
		repaint();
	}
	
	public void evaluateBoard() {
		if (playerLastMove == null) {
			return;
		}
		int res = gomokuAI.getEvaluator().evaluateBoard(board, playerLastMove);
		System.out.println("PlayerLastMove = " + playerLastMove);
		System.out.println("Color = " + (board.getLocation(playerLastMove) == BoardState.BLACK?"Black":"White"));
		System.out.println("Score = " + res);
	}

	class AIWorker extends SwingWorker<Boolean, String> {
		@Override
		protected Boolean doInBackground() throws Exception {
			try {
				if (working || terminate) {
					System.out.println("Error: Shouldn't populate a new thread.");
					return false;
				}
				working = true;
				Location loc = gomokuAI.calculateNextMove(board, playerLastMove);
				setIgnoreRepaint(false);
				board.placeAtLocation(loc.i, loc.j, Board.opponentOf(identity));
				playerLastMove = loc;
				repaint();
				if (Math.abs(gomokuAI.getEvaluator().evaluateBoard(board, loc)) > 10000) {
					terminate = true;
					JOptionPane.showMessageDialog(null, "Computer Wins!");
				}
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
			if (working || terminate) {
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
			int score = Math.abs(gomokuAI.getEvaluator().evaluateBoard(board, playerLastMove)); 
			// System.out.println("Score = " + score);
			if (score > 10000) {
				terminate = true;
				JOptionPane.showMessageDialog(null, "You Win!");
			}
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
					if (playerLastMove != null && playerLastMove.i == row && playerLastMove.j == col) {
						g2.setColor(Color.RED);
					} else {
						g2.setColor(Color.black);
					}
					g2.draw(circle);
				}
			}
		}
	}
}