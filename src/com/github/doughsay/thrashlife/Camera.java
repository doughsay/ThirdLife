package com.github.doughsay.thrashlife;

import org.lwjgl.opengl.GL11;

public class Camera {
	private int xRotation, yRotation, distance;
	private Point origin;

	public Camera() {
		xRotation = yRotation = 0;
		distance = 50;
		origin = new Point(0,0,0);
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
		if(distance < 5) { distance = 5; }
	}

	public void position() {
		GL11.glTranslatef(0f, 0f, -distance);
		GL11.glRotatef(yRotation, 1f, 0f, 0f);
		GL11.glRotatef(xRotation, 0f, 1f, 0f);
		GL11.glTranslatef(origin.x, origin.y, origin.z);
	}

	public Point getOrigin() {
		return origin;
	}

	public int getDistance() {
		return distance;
	}

	public int getXRotation() {
		return xRotation;
	}

	public int getYRotation() {
		return yRotation;
	}
}
