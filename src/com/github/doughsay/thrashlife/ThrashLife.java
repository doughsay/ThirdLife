package com.github.doughsay.thrashlife;

import java.awt.Dimension;
import java.util.LinkedList;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.input.Mouse;

public class ThrashLife {

	private Camera camera = new Camera();
	private FastCubes cubes;
	private FastGrid grid;

	private boolean playing = false;

	private LifeWorld world = new LifeWorld();
	private DrawingHelper draw = new DrawingHelper(world);

	private boolean render = true;

	private ThrashLifeGUI gui;

	private LinkedList<LifeAction> actions = new LinkedList<LifeAction>();

	public ThrashLife() {

		gui = new ThrashLifeGUI(this);

		try {
			gui.initDisplay();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
		}

		initGL();
		cubes = new FastCubes(); // GL has to init before we can init the FastCubes class
		grid = new FastGrid(camera); // same

		// put some initial cells for testing
		//draw.thickLine(200, 0, 0, 0);
		draw.line(-10,0,0,10,0,0, BallBrush.class);
		cubes.load(world.getAll()); // load the current world state as geometry

		updateTitle();

		while(!Display.isCloseRequested() && !gui.isCloseRequested()) {

			processEvents();

			if(render) {
				renderGL();
				Display.update();
			}
			else {
				Display.processMessages();
			}
		}

		Display.destroy();
		gui.dispose();
		System.exit(0);
	}

	private void step(int steps) {
		world.step(steps);
		updateTitle();
		cubes.load(world.getAll());
	}

	private void updateTitle() {
		gui.setTitle("Thrashlife - Generation: " + world.generation + " - Population: " + world.count());
	}

	private void initGL() {
		Dimension d = gui.getCanvasDimensions();
		setViewport(d.width, d.height);

		GL11.glClearDepth(1f);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DITHER);

		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glCullFace(GL11.GL_BACK);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// I heard this was bad?
		GL11.glLineWidth(2.0f);
	}

	private void setViewport(int width, int height) {
		float aspectRatio = (float)width / (float)height;
		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45f, aspectRatio, 1f, 10000f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	private void processEvents() {

		// process our events
		while(!actions.isEmpty()) {
			LifeAction action = actions.pop();
			switch(action) {
			case CLEAR:
				world.clear();
				updateTitle();
				cubes.load(world.getAll());
				camera.reset();
				render = true;
				break;
			case PLAY:
				playing = true;
				break;
			case PAUSE:
				playing = false;
				break;
			case STEP:
				step(1);
				render = true;
				break;
			case DOUBLE_STEP:
				if(world.generation == 0) {
					step(1);
				}
				else {
					step(world.generation * 2);
				}
				render = true;
				break;
			}
		}

		// color pick
		if(Mouse.isButtonDown(0)) {
			int x = Mouse.getX();
			int y = Mouse.getY();

			int[] p = renderColorPick(x, y);

			if(p != null) {
				world.set(p[0], p[1], p[2]);
				updateTitle();
				cubes.load(world.getAll());
				render = true;
			}
		}

		// rotate camera
		if(Mouse.isButtonDown(1)) {
			int dx = Mouse.getDX();
			int dy = Mouse.getDY();
			if(dx != 0 || dy != 0) {
				camera.rotate(dx, dy);
				render = true;
			}
		}

		// zoom camera
		int dw = Mouse.getDWheel();
		if(dw != 0) {
			camera.zoom(dw);
			render = true;
		}

		// see if the canvas size was adjusted
		Dimension d = gui.getNewCanvasDimensions();

		if(d != null) {
			setViewport(d.width, d.height);
			render = true;
		}

		// step if playing
		if(playing) {
			step(1);
			render = true;
		}
	}

	private void renderGL() {
		GL11.glClearColor(0.6f, 0.77f, 0.95f, 1f);

		// Clear the screen / depth buffers and load identity modelview matrix
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();

		// Position the camera
		camera.position();

		// Draw the cubes
		cubes.draw();

		// Draw the grid
		grid.draw();
	}

	private int[] renderColorPick(int x, int y) {
		GL11.glClearColor(1f, 1f, 1f, 1f);

		// Clear the screen / depth buffers and load identity modelview matrix
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();

		// Position the camera
		camera.position();

		// Draw the color pick grid
		return grid.colorPick(x, y);
	}

	public void enqueueAction(LifeAction action) {
		actions.add(action);
	};

	public boolean isPlaying() {
		return playing;
	}

	public static void main(String[] argv) {
		new ThrashLife();
	}

	public enum LifeAction { CLEAR, PLAY, PAUSE, STEP, DOUBLE_STEP }
}