package com.github.doughsay.thrashlife;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;

public class FastGrid {

	private static final float xyR = 1.0f;
	private static final float xyG = 0.5f;
	private static final float xyB = 0.5f;

	private static final float yzR = 0.5f;
	private static final float yzG = 0.5f;
	private static final float yzB = 1.0f;

	private static final float xzR = 0.5f;
	private static final float xzG = 1.0f;
	private static final float xzB = 0.5f;

	// all of this is in bytes, not "items"
	private static final int intSize = Integer.SIZE / 8;
	private static final int floatSize = Float.SIZE / 8;
	private static final int positionSize = floatSize * 3; // a vertex position is 3 floats
	private static final int colorSize = floatSize * 3; // a color is 3 floats
	private static final int vertexSize = positionSize + colorSize; // a vertex is one position plus one color
	private static final int verticesPerQuad = 4; // There are 4 vertex + color elements per square (the 4 corners)
	private static final int quadSize = vertexSize * verticesPerQuad; // This is the size of a square in bytes
	private static final int indicesPerQuad = 4; // the number of integer indices per square
	private static final int quadIndexSize = intSize * indicesPerQuad; // This is the size of a square's indices in bytes
	private static final int stride = vertexSize; // the stride is the size of the vertex (for interleaving the buffer)

	private static final int size = 255; // must be odd and no bigger than 255
	private static final int numQuadsPerPlane = size * size;
	private static final int numPlanes = 3;
	private static final int numQuadsTotal = numQuadsPerPlane * numPlanes;

	private static final int vboCapacity = quadSize * numQuadsTotal;
	private static final int iboCapacity = quadIndexSize * numQuadsTotal;

	// pointers to the VBO and IBO
	private FloatBuffer vertexBuffer;
	private IntBuffer indexBuffer;

	/*
	 _ _ _ _
	|3      |2
	|       |
	|       |
	|_ _ _  |
	 0       1

	*/
	private final int vertexBufferID = ARBVertexBufferObject.glGenBuffersARB();
	private final int indexBufferID  = ARBVertexBufferObject.glGenBuffersARB();

	private Camera camera;

	public FastGrid(Camera camera) {
		this.camera = camera;
		// initial allocation
		allocateVertexBuffer();
		allocateIndexBuffer();
		load();
	}

	public void draw() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

		bindBuffers();

		Plane plane = plane();
		float r = 1.0f, g = 1.0f, b = 1.0f;
		switch(plane) {
			case XY:
				r = xyR; g = xyG; b = xyB;
				GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
				break;
			case YZ:
				r = yzR; g = yzG; b = yzB;
				GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, numQuadsPerPlane * verticesPerQuad * vertexSize);
				break;
			case XZ:
				r = xzR; g = xzG; b = xzB;
				GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, numQuadsPerPlane * verticesPerQuad * vertexSize * 2);
				break;
		}

		// draw the grid squares
		GL11.glColor4f(r, g, b, 0.5f);
		GL11.glDrawElements(GL11.GL_QUADS, numQuadsPerPlane * indicesPerQuad, GL11.GL_UNSIGNED_INT, 0);

		// draw the grid square outlines
		GL11.glColor4f(r, g, b, 1.0f);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glDrawElements(GL11.GL_QUADS, numQuadsPerPlane * indicesPerQuad, GL11.GL_UNSIGNED_INT, 0);

		// reset back to normal polygon fill mode
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	public int[] colorPick(int x, int y) {
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DITHER);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

		bindBuffers();

		Plane plane = plane();
		switch(plane) {
			case XY:
				GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
				GL11.glColorPointer(3, GL11.GL_FLOAT, stride, positionSize);
				break;
			case YZ:
				GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, numQuadsPerPlane * verticesPerQuad * vertexSize);
				GL11.glColorPointer(3, GL11.GL_FLOAT, stride, (numQuadsPerPlane * verticesPerQuad * vertexSize) + positionSize);
				break;
			case XZ:
				GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, numQuadsPerPlane * verticesPerQuad * vertexSize * 2);
				GL11.glColorPointer(3, GL11.GL_FLOAT, stride, (numQuadsPerPlane * verticesPerQuad * vertexSize * 2) + positionSize);
				break;
		}

		// draw the grid squares
		GL11.glDrawElements(GL11.GL_QUADS, numQuadsPerPlane * indicesPerQuad, GL11.GL_UNSIGNED_INT, 0);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		GL11.glEnable(GL11.GL_DITHER);
		GL11.glEnable(GL11.GL_CULL_FACE);

		ByteBuffer pixels = BufferUtils.createByteBuffer(3);
		GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);

		int px = pixels.get(0);
		int py = pixels.get(1);
		int pz = pixels.get(2);

		if(pz == -1) {
			return null;
		}

		// correct for java's stupid signedness
		int cx = (px >= 0) ? px - 127 : px + 129;
		int cy = (py >= 0) ? py - 127 : py + 129;

		Point origin = camera.getOrigin();

		switch(plane) {
		case XY:
			return new int[] {cx + origin.x, cy + origin.y, origin.z};
		case YZ:
			return new int[] {origin.x, cy + origin.y, cx + origin.z};
		case XZ:
			return new int[] {cx + origin.x, origin.y, cy + origin.z};
		}

		// should never happen
		return null;
	}

	// build the buffers (we could optimize here, but it's not so important as it's only called once)
	private void load() {

		mapVertexBuffer();

		float[] color;
		int half = size / 2;

		for(int x = -half; x <= half; x++) {
			for(int y = -half; y <= half; y++) {
				color = new float[] {(float)(x + half) / 255f, (float)(y + half) / 255f, 0f};

				vertexBuffer.put(-0.5f + x);
				vertexBuffer.put(-0.5f + y);
				vertexBuffer.put(0);

				vertexBuffer.put(color);

				vertexBuffer.put(0.5f + x);
				vertexBuffer.put(-0.5f + y);
				vertexBuffer.put(0);

				vertexBuffer.put(color);

				vertexBuffer.put(0.5f + x);
				vertexBuffer.put(0.5f + y);
				vertexBuffer.put(0);

				vertexBuffer.put(color);

				vertexBuffer.put(-0.5f + x);
				vertexBuffer.put(0.5f + y);
				vertexBuffer.put(0);

				vertexBuffer.put(color);
			}
		}

		for(int z = -half; z <= half; z++) {
			for(int y = -half; y <= half; y++) {
				color = new float[] {(float)(z + half) / 255f, (float)(y + half) / 255f, 0f};

				vertexBuffer.put(0);
				vertexBuffer.put(-0.5f + y);
				vertexBuffer.put(-0.5f + z);

				vertexBuffer.put(color);

				vertexBuffer.put(0);
				vertexBuffer.put(-0.5f + y);
				vertexBuffer.put(0.5f + z);

				vertexBuffer.put(color);

				vertexBuffer.put(0);
				vertexBuffer.put(0.5f + y);
				vertexBuffer.put(0.5f + z);

				vertexBuffer.put(color);

				vertexBuffer.put(0);
				vertexBuffer.put(0.5f + y);
				vertexBuffer.put(-0.5f + z);

				vertexBuffer.put(color);
			}
		}

		for(int x = -half; x <= half; x++) {
			for(int z = -half; z <= half; z++) {
				color = new float[] {(float)(x + half) / 255f, (float)(z + half) / 255f, 0f};

				vertexBuffer.put(-0.5f + x);
				vertexBuffer.put(0);
				vertexBuffer.put(-0.5f + z);

				vertexBuffer.put(color);

				vertexBuffer.put(0.5f + x);
				vertexBuffer.put(0);
				vertexBuffer.put(-0.5f + z);

				vertexBuffer.put(color);

				vertexBuffer.put(0.5f + x);
				vertexBuffer.put(0);
				vertexBuffer.put(0.5f + z);

				vertexBuffer.put(color);

				vertexBuffer.put(-0.5f + x);
				vertexBuffer.put(0);
				vertexBuffer.put(0.5f + z);

				vertexBuffer.put(color);
			}
		}

		vertexBuffer.flip();

		unmapVertexBuffer();

		mapIndexBuffer();

		for(int i = 0; i < numQuadsPerPlane * indicesPerQuad; i++) {
			indexBuffer.put(i);
		}

		indexBuffer.flip();

		unmapIndexBuffer();
	}

	private void allocateVertexBuffer() {
		//Allocate the vertex buffer for the elements
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboCapacity, ARBVertexBufferObject.GL_STREAM_DRAW_ARB);
	}

	private void mapVertexBuffer() {
		//Map the vertex buffer to a ByteBuffer
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);
		vertexBuffer = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, ARBVertexBufferObject.GL_WRITE_ONLY_ARB, vboCapacity, null).asFloatBuffer();
	}

	private void unmapVertexBuffer() {
		ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
	}

	private void allocateIndexBuffer() {
		//Allocate the index buffer for the elements
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBufferID);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, iboCapacity, ARBVertexBufferObject.GL_STREAM_DRAW_ARB);
	}

	private void mapIndexBuffer() {
		//Map the index buffer to a ByteBuffer
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBufferID);
		indexBuffer = ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, ARBVertexBufferObject.GL_WRITE_ONLY_ARB, iboCapacity, null).asIntBuffer();
	}

	private void unmapIndexBuffer() {
		ARBVertexBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB);
	}

	private void bindBuffers() {
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBufferID);
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

	private enum Plane { XY, YZ, XZ }
}

