package com.github.doughsay.thrashlife;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;

public class FastCubes {

	private static final float R = 1.0f;
	private static final float G = 1.0f;
	private static final float B = 1.0f;

	// all of this is in bytes, not "items"
	private static final int intSize = Integer.SIZE / 8;
	private static final int floatSize = Float.SIZE / 8;
	private static final int positionSize = floatSize * 3; // a vertex position is 3 floats
	private static final int colorSize = floatSize * 3; // the color for a vertex is 3 floats
	private static final int vertexSize = positionSize + colorSize; // an vertex in our VBO will be one position plus one color
	private static final int verticesPerCube = 8; // There are 8 vertex + color elements per cube (the 8 corners)
	private static final int cubeSize = vertexSize * verticesPerCube; // This is the size of a cube in bytes
	private static final int indicesPerCube = 24; // the number of integer indices per cube
	private static final int cubeIndexSize = intSize * indicesPerCube; // This is the size of a cube's indices in bytes
	private static final int stride = vertexSize; // the stride is the size of the vertex (for interleaving the buffer)

	private int numCubes = 0;

	// let's give it an initial capacity for 1000 cubes (these are also in bytes)
	private int vboCapacity = cubeSize * 1000;
	private int iboCapacity = cubeIndexSize * 1000;

	// pointers to the VBO and IBO
	private ByteBuffer vertexBuffer;
	private ByteBuffer indexBuffer;

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
	private final float[][] baseVertices = new float[][] {
		{-0.45f, -0.45f, -0.45f }, // 0
		{ 0.45f, -0.45f, -0.45f }, // 1
		{-0.45f, -0.45f,  0.45f }, // 2
		{ 0.45f, -0.45f,  0.45f }, // 3
		{-0.45f,  0.45f, -0.45f }, // 4
		{ 0.45f,  0.45f, -0.45f }, // 5
		{-0.45f,  0.45f,  0.45f }, // 6
		{ 0.45f,  0.45f,  0.45f }  // 7
	};

	private final int[] baseIndices = new int[] {
		4, 5, 1, 0,
		5, 7, 3, 1,
		7, 6, 2, 3,
		6, 4, 0, 2,
		0, 1, 3, 2,
		6, 7, 5, 4
	};

	private final float[][] colors = new float[][] {
		{0.4f * R, 0.4f * G, 0.4f * B}, // 0
		{0.2f * R, 0.2f * G, 0.2f * B}, // 1
		{0.6f * R, 0.6f * G, 0.6f * B}, // 2
		{0.4f * R, 0.4f * G, 0.4f * B}, // 3
		{0.6f * R, 0.6f * G, 0.6f * B}, // 4
		{0.4f * R, 0.4f * G, 0.4f * B}, // 5
		{1.0f * R, 1.0f * G, 1.0f * B}, // 6
		{0.6f * R, 0.6f * G, 0.6f * B}  // 7
	};

	private final int vertexBufferID = ARBVertexBufferObject.glGenBuffersARB();
	private final int indexBufferID  = ARBVertexBufferObject.glGenBuffersARB();

	public FastCubes() {
		// initial allocation
		allocateVertexBuffer();
		allocateIndexBuffer();
	}

	public void draw() {
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

		bindBuffers();

		GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
		GL11.glColorPointer(3, GL11.GL_FLOAT, stride, positionSize);

		if(numCubes > 0) {
			GL11.glDrawElements(GL11.GL_QUADS, numCubes * indicesPerCube, GL11.GL_UNSIGNED_INT, 0);
		}

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}

	public void load(ArrayList<Point> points) {

		numCubes = points.size();
		if(numCubes == 0) {
			return;
		}

		int neededVboCapacity = cubeSize * numCubes;

		if(vboCapacity < neededVboCapacity) {
			while(vboCapacity < neededVboCapacity) {
				vboCapacity *= 2;
				iboCapacity *= 2;
			}
			allocateVertexBuffer();
			allocateIndexBuffer();
		}

		mapVertexBuffer();

		for(Point point : points) {

			for(int j = 0; j < 8; j++) {
				vertexBuffer.putFloat(baseVertices[j][0] + point.x);
				vertexBuffer.putFloat(baseVertices[j][1] + point.y);
				vertexBuffer.putFloat(baseVertices[j][2] + point.z);

				vertexBuffer.putFloat(colors[j][0]);
				vertexBuffer.putFloat(colors[j][1]);
				vertexBuffer.putFloat(colors[j][2]);
			}
		}

		vertexBuffer.flip();

		unmapVertexBuffer();

		mapIndexBuffer();

		for(int i = 0; i < numCubes; i++) {
			for(int k = 0; k < 24; k++) {
				indexBuffer.putInt(baseIndices[k] + (i * 8));
			}
		}

		indexBuffer.flip();

		unmapIndexBuffer();
	}

	private void allocateVertexBuffer() {
		//Allocate the vertex buffer for the elements
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboCapacity, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
	}

	private void mapVertexBuffer() {
		//Map the vertex buffer to a ByteBuffer
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);
		vertexBuffer = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, ARBVertexBufferObject.GL_WRITE_ONLY_ARB, vboCapacity, null);
	}

	private void unmapVertexBuffer() {
		ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
	}

	private void allocateIndexBuffer() {
		//Allocate the index buffer for the elements
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBufferID);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, iboCapacity, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
	}

	private void mapIndexBuffer() {
		//Map the index buffer to a ByteBuffer
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBufferID);
		indexBuffer = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, ARBVertexBufferObject.GL_WRITE_ONLY_ARB, iboCapacity, null);
	}

	private void unmapIndexBuffer() {
		ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB);
	}

	private void bindBuffers() {
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBufferID);
	}
}

