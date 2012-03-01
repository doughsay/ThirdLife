package com.github.doughsay.thrashlife;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;

public class FastCubes {

	private int size = 0;
	private static final float R = 1.0f;
	private static final float G = 0.65f;
	private static final float B = 0.45f;

	/*

	4_ _ _ _5
	|\      |\
	| \_ _ _|_\
	| |6    | |7
	|_|_ _ _| |
	\0|     \1|
	 \|_ _ _ \|
	  2       3

	*/
	private final float[] baseVertices = new float[] {
		-0.45f, -0.45f, -0.45f, // 0
		 0.45f, -0.45f, -0.45f, // 1
		-0.45f, -0.45f,  0.45f, // 2
		 0.45f, -0.45f,  0.45f, // 3
		-0.45f,  0.45f, -0.45f, // 4
		 0.45f,  0.45f, -0.45f, // 5
		-0.45f,  0.45f,  0.45f, // 6
		 0.45f,  0.45f,  0.45f  // 7
	};
	private float[] vertices = new float[24];

	private final int[] baseIndices = new int[] {
		4, 5, 1, 0,
		5, 7, 3, 1,
		7, 6, 2, 3,
		6, 4, 0, 2,
		0, 1, 3, 2,
		6, 7, 5, 4
	};
	private int[] indices = new int[24];

	private final float[] colors = new float[] {
		0.4f * R, 0.4f * G, 0.4f * B, // 0
		0.2f * R, 0.2f * G, 0.2f * B, // 1
		0.6f * R, 0.6f * G, 0.6f * B, // 2
		0.4f * R, 0.4f * G, 0.4f * B, // 3
		0.6f * R, 0.6f * G, 0.6f * B, // 4
		0.4f * R, 0.4f * G, 0.4f * B, // 5
		1.0f * R, 1.0f * G, 1.0f * B, // 6
		0.6f * R, 0.6f * G, 0.6f * B  // 7
	};

	private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(size);
	private FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(size);
	private IntBuffer indexBuffer = BufferUtils.createIntBuffer(size);

	private final int vertexBufferID = ARBVertexBufferObject.glGenBuffersARB();
	private final int colorBufferID  = ARBVertexBufferObject.glGenBuffersARB();
	private final int indexBufferID  = ARBVertexBufferObject.glGenBuffersARB();

	public FastCubes() {
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
	}

	public void draw() {
		if(size > 0) {
			GL11.glDrawElements(GL11.GL_QUADS, size, GL11.GL_UNSIGNED_INT, 0);
		}
	}

	public void load(ArrayList<Point> cells) {

		size = 24 * cells.size();
		if(size == 0) {
			return;
		}

		if(vertexBuffer.capacity() > size) {
			vertexBuffer.clear();
			colorBuffer.clear();
			indexBuffer.clear();
		}
		else {
			vertexBuffer = BufferUtils.createFloatBuffer(size);
			colorBuffer = BufferUtils.createFloatBuffer(size);
			indexBuffer = BufferUtils.createIntBuffer(size);
		}

		int i = 0;
		for(Point cell : cells) {

			for(int j = 0; j < 24; j += 3) {
				vertices[j] = baseVertices[j] + cell.x;
				vertices[j+1] = baseVertices[j+1] + cell.y;
				vertices[j+2] = baseVertices[j+2] + cell.z;
			}

			for(int k = 0; k < 24; k++) {
				indices[k] = baseIndices[k] + (i * 8);
			}

			vertexBuffer.put(vertices);
			colorBuffer.put(colors);
			indexBuffer.put(indices);

			i++;
		}

		vertexBuffer.rewind();
		colorBuffer.rewind();
		indexBuffer.rewind();

		bindBuffers();
	}

	private void bindBuffers() {
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBuffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, colorBufferID);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, colorBuffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
		GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0);

		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBufferID);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBuffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
	}
}

