package com.github.doughsay.thrashlife;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ThrashLife {

	private Camera camera = new Camera();
	private FastCubes cubes;

	private int screenX = 1280;
	private int screenY = 1024;

	private boolean playing = false;

	private LifeWorld world = new LifeWorld();
	private DrawingHelper draw = new DrawingHelper(world);

	public ThrashLife() {
		draw.line(200, 0, 0, 0);

		initDisplay();
		initGL(); // init OpenGL
		cubes = new FastCubes(); // GL has to init before we can init the FastCubes class
		cubes.load(world.getAll()); // load the current world state as geometry

		updateTitle();

		// initial render
		renderGL();

		while (!Display.isCloseRequested()) {

			catchEvents();

			if(playing) {
				step(1);
			}

			Display.update();
			Display.sync(60); // cap fps to 60fps
		}

		Display.destroy();
	}

	public void step(int steps) {
		world.step(steps);
		updateTitle();
		cubes.load(world.getAll());
		renderGL();
	}

	private void updateTitle() {
		Display.setTitle("Thrashlife - Generation: " + world.generation + " - Population: " + world.count());
	}

	public void initDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(screenX, screenY));
			Display.create();
			Display.setTitle("Thrashlife");
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void initGL() {
		float aspectRatio = (float)screenX / (float)screenY;
		GL11.glViewport(0, 0, screenX, screenY);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45f, aspectRatio, 1f, 1000f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glClearColor(0.6f, 0.77f, 0.95f, 1f);
		GL11.glClearDepth(1f);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DITHER);

		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glCullFace(GL11.GL_BACK);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glLineWidth(2.0f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
	}

	public void catchEvents() {
		/*if(Mouse.isButtonDown(0)) {
			int mouseX = Mouse.getX();
			int mouseY = -(Mouse.getY() - 600);
			Point point = grid.pick(mouseX, mouseY);
			if(point != null) {
				world.set(point.x, point.y, point.z, 1);
				cubes.load(world.getAll());
				renderGL();
			}
		}*/

		if(Mouse.isButtonDown(1)) {
			int dx = Mouse.getDX();
			int dy = Mouse.getDY();
			if(dx != 0 || dy != 0) {
				camera.rotate(dx, dy);
				renderGL();
			}
		}

		int dw = Mouse.getDWheel();
		if(dw != 0) {
			camera.zoom(dw);
			renderGL();
		}

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					step(1);
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_D) {
					if(world.generation == 0) {
						step(1);
					}
					else {
						step(world.generation * 2);
					}
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_S) {
					playing = true;
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					playing = false;
				}
			}
		}
	}

	public void renderGL() {

		// Clear the screen / depth buffers and load identity modelview matrix
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();

		// Position the camera
		camera.position();

		// Draw the cubes
		cubes.draw();
	}

	public static void main(String[] argv) {
		new ThrashLife();
	}
}