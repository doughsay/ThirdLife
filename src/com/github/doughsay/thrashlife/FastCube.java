package com.github.doughsay.thrashlife;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;

public class FastCube {
	private static int _verticesID = 0;
	private static int _indicesID = 0;
	private static int _coloursID = 0;
	private static int size;

	public static void init() {
		_verticesID = VBOHandler.createVBO();
		_indicesID  = VBOHandler.createVBO();
		_coloursID  = VBOHandler.createVBO();

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, _verticesID);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, _coloursID);
		GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0);

		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, _indicesID);
	}

	public static void loadData(ArrayList<Point> cells) {
		float[] baseVertices = new float[] {
			//  X      Y      Z
			-0.5f, -0.5f, -0.5f, // 0 LBF
			 0.5f, -0.5f, -0.5f, // 1 RBF
			-0.5f, -0.5f,  0.5f, // 2 LBB
			 0.5f, -0.5f,  0.5f, // 3 RBB

			-0.5f,  0.5f, -0.5f, // 4 LTF
			 0.5f,  0.5f, -0.5f, // 5 RTF
			-0.5f,  0.5f,  0.5f, // 6 LTB
			 0.5f,  0.5f,  0.5f // 7 RTB
		};
		float[] vertices = new float[24];

		int[] baseIndices = new int[] {
			4, 5, 1, 0, // FRONT
			5, 7, 3, 1, // RIGHT
			7, 6, 2, 3, // BACK
			6, 4, 0, 2, // LEFT
			0, 1, 3, 2, // BOTTOM
			6, 7, 5, 4 // TOP
		};
		int[] indices = new int[24];

		float[] colours = new float[] {
			0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 1.0f
		};

		size = 24 * cells.size();
		FloatBuffer bufferedvertices = BufferUtils.createFloatBuffer(size);
		FloatBuffer bufferedColours = BufferUtils.createFloatBuffer(size);
		IntBuffer bufferedindices = BufferUtils.createIntBuffer(size);

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

			bufferedvertices.put(vertices);
			bufferedColours.put(colours);
			bufferedindices.put(indices);

			i++;
		}

		bufferedvertices.rewind();
		bufferedColours.rewind();
		bufferedindices.rewind();

		VBOHandler.bufferData(_verticesID, bufferedvertices);
		VBOHandler.bufferData(_coloursID, bufferedColours);
		VBOHandler.bufferElementData(_indicesID, bufferedindices);
	}

	public static void draw() {
		GL11.glDrawElements(GL11.GL_QUADS, size, GL11.GL_UNSIGNED_INT, 0);
		//GL12.glDrawRangeElements(GL11.GL_QUADS, 0, size, size, GL11.GL_UNSIGNED_INT, 0);
	}

	private static class VBOHandler {

		public static int createVBO()
		{
			if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object)
			{
				return ARBVertexBufferObject.glGenBuffersARB();
			}
			return -1;
		}

		public static void bufferData(int id, FloatBuffer buffer)
		{
			if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object)
			{
				ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
				ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
			}
		}

		public static void bufferElementData(int id, IntBuffer buffer)
		{
			if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object)
			{

				ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, id);
				ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
			}
		}
	}

}
