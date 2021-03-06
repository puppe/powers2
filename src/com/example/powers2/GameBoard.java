package com.example.powers2;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class GameBoard {

	public Game theGame = null;
	
	// NOTE:
	// tile (row, col) is occupied iff 0 < squares(row, col)
	
	// private static final String TAG = GameBoard.class.getName();

	private int[] squares = new int[ROWS * COLS];
	private Game.Tile[] tiles = new Game.Tile[ROWS * COLS];
	public static final int ROWS = 4;
	public static final int COLS = ROWS;

	public void Init() {
		int occupied = 0;
		while (occupied < 2) {
			int r = (int) Math.floor(Math.random() * squares.length);
			Log.d("TAG", "" + r);
			if (squares[r] == 0) {
				squares[r] = 1;
				occupied++;
				
				int row = r / COLS;
				int col = r % COLS;
				tiles[r] = theGame.SpawnTile(row, col);
			}
		}
	}

	// NOTE: GameRenderer assumes column major layout
	public int[] getSquares() { return squares; }
	
	public int get(int row, int col) {
		return squares[row * COLS + col];
	}
	
	private int flatten(int row, int col, boolean transpose) {
		if(transpose) {
			int tmp = row;
			row = col;
			col = tmp;
		}
		return row * COLS + col;
	}

	private int get(int row, int col, boolean transpose) {
		return transpose ? get(col, row) : get(row, col);
	}

	private void set(int row, int col, int n) {
		squares[row * COLS + col] = n;
	}

	private void set(int row, int col, boolean transpose, int n) {
		if (transpose) {
			set(col, row, n);
		} else {
			set(row, col, n);
		}
	}

	public void left() {
		shift(true, true);
	}

	public void right() {
		shift(true, false);
	}

	public void up() {
		shift(false, true);
	}

	public void down() {
		shift(false, false);
	}

	private void shift(boolean horizontal, boolean inc) {
		List<Position> positions = new ArrayList<Position>();
		boolean transpose = !horizontal;
		int rows = horizontal ? ROWS : COLS;
		int cols = horizontal ? COLS : ROWS;
		for (int row = 0; row < rows; row++) {
			Deque<Integer> deque = new LinkedList<Integer>();
			Deque<Game.Tile> tdeq = new LinkedList<Game.Tile>();
			int col = inc ? 0 : cols - 1;
			while (inc ? col < cols : col > -1) {
				int n = get(row, col, transpose);
				if (n > 0) {
					deque.add(get(row, col, transpose));
					tdeq.add(tiles[flatten(row, col, transpose)]);
					tiles[flatten(row, col, transpose)] = null;
				}
				col = next(col, inc);
			}
			col = inc ? 0 : cols - 1;
			while (inc ? col < cols : col > -1) {
				if (deque.size() > 0) {
					int x = deque.pop();
					set(row, col, transpose, x);
					Game.Tile tile = tdeq.pop();
					tile.MoveTo(row, col, transpose);
					tiles[flatten(row, col, transpose)] = tile;
					if (deque.size() > 0 && deque.peek() == x) {
						set(row, col, transpose, deque.pop() + 1);
						Game.Tile slave = tdeq.pop();
						slave.MoveTo(row, col, transpose);
						slave.SetMaster(tile);
					}
				} else {
					set(row, col, transpose, 0);
					tiles[flatten(row, col, transpose)] = null;
					positions.add(new Position(row, col, transpose));
				}
				col = next(col, inc);
			}
		}
		if (positions.size() > 0) {
			int i = (int) Math.floor(Math.random() * positions.size());
			Position p = positions.get(i);
			set(p.row, p.col, p.transpose, 1);
			Game.Tile tile = theGame.SpawnTile(p.row, p.col, p.transpose);
			tiles[flatten(p.row, p.col, p.transpose)] = tile;
			tile.FadeIn();
		}
	}

	private static class Position {
		public final int row;
		public final int col;
		public final boolean transpose;

		public Position(int row, int col, boolean transpose) {
			super();
			this.row = row;
			this.col = col;
			this.transpose = transpose;
		}
	}

	private int next(int col, boolean inc) {
		return inc ? col + 1 : col - 1;
	}

}