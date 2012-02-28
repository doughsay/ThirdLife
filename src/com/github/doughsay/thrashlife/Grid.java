package com.github.doughsay.thrashlife;

import org.lwjgl.opengl.GL11;

public class Grid {
	public Grid() {
		// TODO
	}

	public void draw(int axis, int size, Point origin) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);

		float red = 0, green = 0, blue = 0;
		switch(axis) {
			case 0:
			case 2:
				red=1.0f; green=0.5f; blue=0.5f;
				break;
			case 1:
			case 3:
				red=0.5f; green=0.5f; blue=1.0f;
				break;
			case 4:
			case 5:
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
					switch(axis) {
						case 0:
						case 2:
							GL11.glVertex3f(-0.48f+i-origin.x, 0.48f+j-origin.y, 0.0f-origin.z);
							GL11.glVertex3f( 0.48f+i-origin.x, 0.48f+j-origin.y, 0.0f-origin.z);
							GL11.glVertex3f( 0.48f+i-origin.x,-0.48f+j-origin.y, 0.0f-origin.z);
							GL11.glVertex3f(-0.48f+i-origin.x,-0.48f+j-origin.y, 0.0f-origin.z);
							break;
						case 1:
						case 3:
							GL11.glVertex3f( 0.0f-origin.x, 0.48f+j-origin.y,-0.48f+i-origin.z);
							GL11.glVertex3f( 0.0f-origin.x, 0.48f+j-origin.y, 0.48f+i-origin.z);
							GL11.glVertex3f( 0.0f-origin.x,-0.48f+j-origin.y, 0.48f+i-origin.z);
							GL11.glVertex3f( 0.0f-origin.x,-0.48f+j-origin.y,-0.48f+i-origin.z);
							break;
						case 4:
						case 5:
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
			switch(axis) {
				case 0:
				case 2:
					GL11.glVertex3f(i-0.5f-origin.x,  size/2+0.5f-origin.y, 0.0f-origin.z);
					GL11.glVertex3f(i-0.5f-origin.x, -size/2-0.5f-origin.y, 0.0f-origin.z);
					GL11.glVertex3f( size/2+0.5f-origin.x, i-0.5f-origin.y, 0.0f-origin.z);
					GL11.glVertex3f(-size/2-0.5f-origin.x, i-0.5f-origin.y, 0.0f-origin.z);
					break;
				case 1:
				case 3:
					GL11.glVertex3f(0.0f-origin.x,  size/2+0.5f-origin.y, i-0.5f-origin.z);
					GL11.glVertex3f(0.0f-origin.x, -size/2-0.5f-origin.y, i-0.5f-origin.z);
					GL11.glVertex3f(0.0f-origin.x, i-0.5f-origin.y,  size/2+0.5f-origin.z);
					GL11.glVertex3f(0.0f-origin.x, i-0.5f-origin.y, -size/2-0.5f-origin.z);
					break;
				case 4:
				case 5:
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
}
