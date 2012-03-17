package com.github.doughsay.thrashlife;

public class DrawingState {

	private Brush brush;
	private boolean drawing = false;

	public int x, y, z, oldX, oldY, oldZ;

	public DrawingState(Brush brush) {
		this.brush = brush;
	}

	public Brush getBrush() {
		return brush;
	}

	public void setBrush(Brush brush) {
		this.brush = brush;
	}

	public void start(int[] p) {
		drawing = true;
		x = oldX = p[0];
		y = oldY = p[1];
		z = oldZ = p[2];
	}

	public void stop() {
		drawing = false;
	}

	public void next(int[] p) {
		oldX = x; oldY = y; oldZ = z;
		x = p[0]; y = p[1]; z = p[2];
	}

	public boolean isDrawing() {
		return drawing;
	}

}
