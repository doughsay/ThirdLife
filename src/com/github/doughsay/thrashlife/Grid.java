package com.github.doughsay.thrashlife;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Grid {

	private static final int MIN_SIZE = 10;
	private static final int MAX_SIZE = 200;
	private final Camera camera;

	public Grid(Camera camera) {
		this.camera = camera;
	}

	public void draw() {
		Plane plane = plane();
		int size = gridSize();
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
			GL11.glPushName(i);

			if(i <= size / 2) {
				for(int j = -size / 2; j <= size / 2; j++) {
					GL11.glPushName(j);
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
					GL11.glPopName();
				}
			}

			GL11.glPopName();

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

	public Point pick(int x, int y) {
		IntBuffer selBuf = BufferUtils.createIntBuffer(512);
		IntBuffer viewport = BufferUtils.createIntBuffer(16);

		GL11.glSelectBuffer(selBuf);
		GL11.glRenderMode(GL11.GL_SELECT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
		GLU.gluPickMatrix(x, viewport.get(3) - y, 5, 5, viewport);
		GLU.gluPerspective(45f, (float) viewport.get(2) / (float) viewport.get(3), 1.0f, 1000f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glInitNames();

		GL11.glLoadIdentity();

		camera.position();

		draw();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glFlush();

		int hits = GL11.glRenderMode(GL11.GL_RENDER);

		if(hits != 0) {
			int index = 0;
			for(int i = 0; i < hits; i++) {
				int numNames = selBuf.get(index);
				if(numNames == 2) {

					int px, py, pz;
					Plane plane = plane();
					Point origin = camera.getOrigin();

					switch(plane) {
						case XY:
							px = origin.x + selBuf.get(index + 3);
							py = origin.y + selBuf.get(index + 4);
							pz = origin.z;
							return new Point(px, py, pz);
						case YZ:
							px = origin.x;
							py = origin.y + selBuf.get(index + 4);
							pz = origin.z + selBuf.get(index + 3);
							return new Point(px, py, pz);
						case XZ:
							px = origin.x + selBuf.get(index + 3);
							py = origin.y;
							pz = origin.z + selBuf.get(index + 4);
							return new Point(px, py, pz);
					}

				}
				index += (3 + numNames);
			}
		}

		return null;
	}

	private Plane plane() {
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

	private int gridSize() {
		int gridSize = camera.getDistance() - 20;
		if(gridSize < MIN_SIZE) { gridSize = MIN_SIZE; }
		if(gridSize > MAX_SIZE) { gridSize = MAX_SIZE; }
		return gridSize;
	}

	public enum Plane { XY, YZ, XZ }
}
