package com.github.doughsay.thrashlife;

import org.lwjgl.opengl.GL11;

public class Camera {
	private static final int MIN_DISTANCE = 3;
	private static final double POWER = 2;
	private int xRotation, yRotation, distance;
	private Point origin;

	public Camera() {
		xRotation = yRotation = 0;
		distance = 5;
		origin = new Point(0, 0, 0);
	}

	public void reset() {
		xRotation = yRotation = 0;
		distance = 5;
		origin = new Point(0, 0, 0);
	}

	public void rotate(int dx, int dy) {
		xRotation += dx;
		yRotation -= dy;

		if(xRotation > 360) { xRotation -= 360; }
		if(xRotation < 0) { xRotation += 360; }

		if(yRotation > 90) { yRotation = 90; }
		if(yRotation < -90) { yRotation = -90; }
	}

	public void zoom(int dw) {
		distance -= (dw / 120);
		if(distance < MIN_DISTANCE) { distance = MIN_DISTANCE; }
	}

	public void position() {
		GL11.glTranslatef(0f, 0f, (float) -Math.pow(distance, POWER));
		GL11.glRotatef(yRotation, 1f, 0f, 0f);
		GL11.glRotatef(xRotation, 0f, 1f, 0f);
		GL11.glTranslatef(origin.x, origin.y, origin.z);
	}

	public Point getOrigin() {
		return origin;
	}

	public int getDistance() {
		return (int) Math.pow(distance, POWER);
	}

	public int getXRotation() {
		return xRotation;
	}

	public int getYRotation() {
		return yRotation;
	}
}
