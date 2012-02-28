package com.github.doughsay.thrashlife;

import org.lwjgl.opengl.GL11;

public class Camera {
	private int xRotation, yRotation, distance;
	public int axis, planeSize;
	public Point lookAt;

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

		if(yRotation > 45) {
			axis = 4;
		}
		else if(yRotation < -45) {
			axis = 5;
		}
		else if(xRotation > 315 || xRotation < 45) {
			axis = 0;
		}
		else if(xRotation > 45 && xRotation < 135) {
			axis = 1;
		}
		else if(xRotation > 135 && xRotation < 225) {
			axis = 2;
		}
		else if(xRotation > 225 && xRotation < 315) {
			axis = 3;
		}
	}

	public void zoom(int dw) {
		distance -= (dw / 120);
		if(distance < 5) { distance = 5; }
		planeSize = distance - 20;
		if(planeSize < 10) { planeSize = 10; }
		if(planeSize > 100) { planeSize = 100; }
	}

	public void position() {
		GL11.glTranslatef(0f, 0f, -distance);
		GL11.glRotatef(yRotation, 1f, 0f, 0f);
		GL11.glRotatef(xRotation, 0f, 1f, 0f);
		GL11.glTranslatef(lookAt.x, lookAt.y, lookAt.z);
	}
}
