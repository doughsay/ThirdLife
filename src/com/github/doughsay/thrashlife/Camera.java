package com.github.doughsay.thrashlife;

import org.lwjgl.opengl.GL11;

public class Camera {
	private int xRotation, yRotation, distance;
	public int axis, planeSize;
	private Point lookAt;

	public Camera() {
		xRotation = yRotation = 0;
		distance = 50;
		axis = 0;
		planeSize = 30;
		lookAt = new Point(0,0,0);
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
	}

	public void position() {
		GL11.glTranslatef(0f, 0f, -distance);
		GL11.glRotatef(yRotation, 1f, 0f, 0f);
		GL11.glRotatef(xRotation, 0f, 1f, 0f);
		GL11.glTranslatef(lookAt.x, lookAt.y, lookAt.z);
	}
}
