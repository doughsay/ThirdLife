package com.github.doughsay.thrashlife;

import org.lwjgl.opengl.GL11;

public class Grid {

	public Grid() { }

	public void draw(Camera camera) {
		Plane plane = plane(camera);
		int size = gridSize(camera.getDistance());
		Point origin = camera.getOrigin();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);

		float red = 0, green = 0, blue = 0;
		switch(plane) {
			case XY:
				red=1.0f; green=0.5f; blue=0.5f;
				break;
			case YZ:
				red=0.5f; green=0.5f; blue=1.0f;
				break;
			case XZ:
				red=0.5f; green=1.0f; blue=0.5f;
				break;
		}

		for(int i = -size / 2; i <= size / 2 + 1; i++) {
			//glPushName(i);

			if(i <= size / 2) {
				for(int j = -size / 2; j <= size / 2; j++) {
					//glPushName(j);
					GL11.glColor4f(red, green, blue, 0.5f);
					GL11.glBegin(GL11.GL_QUADS);
					switch(plane) {
						case XY:
							GL11.glVertex3f(-0.48f+i-origin.x, 0.48f+j-origin.y, 0.0f-origin.z);
							GL11.glVertex3f( 0.48f+i-origin.x, 0.48f+j-origin.y, 0.0f-origin.z);
							GL11.glVertex3f( 0.48f+i-origin.x,-0.48f+j-origin.y, 0.0f-origin.z);
							GL11.glVertex3f(-0.48f+i-origin.x,-0.48f+j-origin.y, 0.0f-origin.z);
							break;
						case YZ:
							GL11.glVertex3f( 0.0f-origin.x, 0.48f+j-origin.y,-0.48f+i-origin.z);
							GL11.glVertex3f( 0.0f-origin.x, 0.48f+j-origin.y, 0.48f+i-origin.z);
							GL11.glVertex3f( 0.0f-origin.x,-0.48f+j-origin.y, 0.48f+i-origin.z);
							GL11.glVertex3f( 0.0f-origin.x,-0.48f+j-origin.y,-0.48f+i-origin.z);
							break;
						case XZ:
							GL11.glVertex3f(-0.48f+i-origin.x, 0.0f-origin.y, 0.48f+j-origin.z);
							GL11.glVertex3f( 0.48f+i-origin.x, 0.0f-origin.y, 0.48f+j-origin.z);
							GL11.glVertex3f( 0.48f+i-origin.x, 0.0f-origin.y,-0.48f+j-origin.z);
							GL11.glVertex3f(-0.48f+i-origin.x, 0.0f-origin.y,-0.48f+j-origin.z);
							break;
					}
					GL11.glEnd();
					//glPopName();
				}
			}

			//glPopName();

			GL11.glColor4f(red, green, blue, 1.0f);
			GL11.glBegin(GL11.GL_LINES);
			switch(plane) {
				case XY:
					GL11.glVertex3f(i-0.5f-origin.x,  size/2+0.5f-origin.y, 0.0f-origin.z);
					GL11.glVertex3f(i-0.5f-origin.x, -size/2-0.5f-origin.y, 0.0f-origin.z);
					GL11.glVertex3f( size/2+0.5f-origin.x, i-0.5f-origin.y, 0.0f-origin.z);
					GL11.glVertex3f(-size/2-0.5f-origin.x, i-0.5f-origin.y, 0.0f-origin.z);
					break;
				case YZ:
					GL11.glVertex3f(0.0f-origin.x,  size/2+0.5f-origin.y, i-0.5f-origin.z);
					GL11.glVertex3f(0.0f-origin.x, -size/2-0.5f-origin.y, i-0.5f-origin.z);
					GL11.glVertex3f(0.0f-origin.x, i-0.5f-origin.y,  size/2+0.5f-origin.z);
					GL11.glVertex3f(0.0f-origin.x, i-0.5f-origin.y, -size/2-0.5f-origin.z);
					break;
				case XZ:
					GL11.glVertex3f(i-0.5f-origin.x, 0.0f-origin.y,  size/2+0.5f-origin.z);
					GL11.glVertex3f(i-0.5f-origin.x, 0.0f-origin.y, -size/2-0.5f-origin.z);
					GL11.glVertex3f( size/2+0.5f-origin.x, 0.0f-origin.y, i-0.5f-origin.z);
					GL11.glVertex3f(-size/2-0.5f-origin.x, 0.0f-origin.y, i-0.5f-origin.z);
					break;
			}
			GL11.glEnd();
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	private Plane plane(Camera camera) {
		int xRotation = camera.getXRotation();
		int yRotation = camera.getYRotation();
		Plane plane = Plane.XY;

		if(yRotation > 45) {
			plane = Plane.XZ;
		}
		else if(yRotation < -45) {
			plane = Plane.XZ;
		}
		else if(xRotation > 315 || xRotation < 45) {
			plane = Plane.XY;
		}
		else if(xRotation > 45 && xRotation < 135) {
			plane = Plane.YZ;
		}
		else if(xRotation > 135 && xRotation < 225) {
			plane = Plane.XY;
		}
		else if(xRotation > 225 && xRotation < 315) {
			plane = Plane.YZ;
		}

		return plane;
	}

	private int gridSize(int distance) {
		int gridSize = distance - 20;
		if(gridSize < 10) { gridSize = 10; }
		if(gridSize > 100) { gridSize = 100; }
		return gridSize;
	}

	private enum Plane { XY, YZ, XZ }
}
