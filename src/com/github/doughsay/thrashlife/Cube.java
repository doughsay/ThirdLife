package com.github.doughsay.thrashlife;

import org.lwjgl.opengl.GL11;

/**
 * Utility class to draw a cube. Some examples use the GLUT function
 * glutWireCube(). This class can be used to replace these calls.
 *
 * @author Ciardhubh
 */
public class Cube {

	// Cube data from example 2-16
	private static final float[][] vertices = {
		{ -0.5f, -0.5f, -0.5f }, // 0
		{  0.5f, -0.5f, -0.5f },
		{  0.5f,  0.5f, -0.5f },
		{ -0.5f,  0.5f, -0.5f }, // 3
		{ -0.5f, -0.5f,  0.5f }, // 4
		{  0.5f, -0.5f,  0.5f },
		{  0.5f,  0.5f,  0.5f },
		{ -0.5f,  0.5f,  0.5f }  // 7
	};

	private static final float[][] normals = {
		{  0,  0, -1 }, { 0, 0, 1 },
		{  0, -1,  0 }, { 0, 1, 0 },
		{ -1,  0,  0 }, { 1, 0, 0 }
	};

	private static final byte[][] indices = {
		{ 0, 3, 2, 1 },
		{ 4, 5, 6, 7 },
		{ 0, 1, 5, 4 },
		{ 3, 7, 6, 2 },
		{ 0, 4, 7, 3 },
		{ 1, 2, 6, 5 }
	};

	/**
	 * Draws a wireframe cube with the current set transformations. The cube's
	 * size can be adjusted.
	 *
	 * Size should be positive. Behaviour for negative values is undefined. The
	 * parameter size defines the resulting cube's edge length, e.g. a size of
	 * 1.0f means that the cube will have an edge length of 1.0f.
	 *
	 * @param size
	 *            Length of the cube's edges.
	 */
	public static void wireCube(final float size) {
		// Draw all six sides of the cube.
		for (int i = 0; i < 6; i++) {
			GL11.glBegin(GL11.GL_LINE_LOOP);
			// Draw all four vertices of the current side.
			for (int m = 0; m < 4; m++) {
				float[] temp = vertices[indices[i][m]];
				GL11.glNormal3f(normals[i][0], normals[i][1], normals[i][2]);
				GL11.glVertex3f(temp[0] * size, temp[1] * size, temp[2] * size);
			}
			GL11.glEnd();
		}
	}

	public static void wireCubeAt(final float size, final int x, final int y, final int z) {
		final float[][] vertices = {
			{ -0.5f * size + x, -0.5f * size + y, -0.5f * size + z }, // 0
			{  0.5f * size + x, -0.5f * size + y, -0.5f * size + z },
			{  0.5f * size + x,  0.5f * size + y, -0.5f * size + z },
			{ -0.5f * size + x,  0.5f * size + y, -0.5f * size + z }, // 3
			{ -0.5f * size + x, -0.5f * size + y,  0.5f * size + z }, // 4
			{  0.5f * size + x, -0.5f * size + y,  0.5f * size + z },
			{  0.5f * size + x,  0.5f * size + y,  0.5f * size + z },
			{ -0.5f * size + x,  0.5f * size + y,  0.5f * size + z }  // 7
		};

		// Draw all six sides of the cube.
		for (int i = 0; i < 6; i++) {
			GL11.glBegin(GL11.GL_LINE_LOOP);
			// Draw all four vertices of the current side.
			for (int m = 0; m < 4; m++) {
				float[] temp = vertices[indices[i][m]];
				GL11.glNormal3f(normals[i][0], normals[i][1], normals[i][2]);
				GL11.glVertex3f(temp[0], temp[1], temp[2]);
			}
			GL11.glEnd();
		}
	}

	/**
	 * Draws a solid cube with the current set transformations. The cube's size
	 * can be adjusted.
	 *
	 * Size should be positive. Behaviour for negative values is undefined. The
	 * parameter size defines the resulting cube's edge length, e.g. a size of
	 * 1.0f means that the cube will have an edge length of 1.0f.
	 *
	 * @param size
	 *            Length of the cube's edges.
	 */
	public static void solidCube(final float size) {
		// Draw all six sides of the cube.
		for (int i = 0; i < 6; i++) {
			GL11.glBegin(GL11.GL_QUADS);
			// Draw all four vertices of the current side.
			for (int m = 0; m < 4; m++) {
				float[] temp = vertices[indices[i][m]];
				GL11.glNormal3f(normals[i][0], normals[i][1], normals[i][2]);
				GL11.glVertex3f(temp[0] * size, temp[1] * size, temp[2] * size);
			}
			GL11.glEnd();
		}
	}
}