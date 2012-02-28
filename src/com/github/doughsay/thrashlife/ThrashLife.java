package com.github.doughsay.thrashlife;

import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ThrashLife {

	private long lastFrame;
	private long lastFPS;
	private int fps;

	private boolean drawing = true, selecting = false;

	private Camera camera = new Camera();
	private FastCubes cubes;
	private Grid grid = new Grid();

	private int screenX = 800;
	private int screenY = 600;

	private boolean playing = false;

	private LifeWorld world = new LifeWorld();

	public void start() {
		line(200, 0, 0, 0);

		initGL(); // init OpenGL
		cubes = new FastCubes(); // GL has to init before we can init the FastCubes class
		cubes.load(world.getAll()); // load the current world state as geometry

		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer

		while (!Display.isCloseRequested()) {
			int delta = getDelta();

			update(delta);

			if(playing) {
				step(1);
			}

			renderGL();

			Display.update();
			Display.sync(60); // cap fps to 60fps
		}

		Display.destroy();
	}

	public void set(int x, int y, int z) {
		world.set(x, y, z, 1);
	}

	public void erase(int x, int y, int z) {
		world.set(x, y, z, 0);
	}

	public void toggle(int x, int y, int z) {
		world.set(x, y, z, world.get(x, y, z) == 1 ? 0 : 1);
	}

	public void line(int width, int ox, int oy, int oz) {
		int half = width / 2;
		for(int x = -half; x <= half; x++) {
			for(int y = 0; y <= 1; y++) {
				for(int z = 0; z <= 1; z++) {
					set(ox+x, oy+y, oz+z);
				}
			}
		}
	}

	public void randomFill(int width, int fillPercent, int ox, int oy, int oz) {
		Random randomGenerator = new Random();
		int half = width / 2;
		for(int x = -half; x <= half; x++) {
			for(int y = -half; y <= half; y++) {
				for(int z = -half; z <= half; z++) {
					if(randomGenerator.nextInt(100) < fillPercent) {
						set(ox+x, oy+y, oz+z);
					}
				}
			}
		}
	}

	public void blinker(int x, int y, int z) {
		set(x-1, y,   z);
		set(  x, y,   z);
		set(x+1, y,   z);
		set(x-1, y+1, z);
		set(  x, y+1, z);
		set(x+1, y+1, z);
	}

	public void glider(int x, int y, int z) {
		set(  x,   y,   z);
		set(  x, y+1,   z);
		set(  x, y+2,   z);
		set(x+1, y+2,   z);
		set(x+2, y+1,   z);
		set(  x,   y, z+1);
		set(  x, y+1, z+1);
		set(  x, y+2, z+1);
		set(x+1, y+2, z+1);
		set(x+2, y+1, z+1);
	}

	public void step(int steps) {
		if(world.root.width() > Math.pow(2, 14)) {
			world.collect();
		}
		world.step(steps);
		cubes.load(world.getAll());
	}

	public void initGL() {
		try {
			Display.setDisplayMode(new DisplayMode(screenX, screenY));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

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

	public void update(int delta) {
		if(Mouse.isButtonDown(1)) {
			camera.rotate(Mouse.getDX(), Mouse.getDY());
		}

		camera.zoom(Mouse.getDWheel());

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

		updateFPS(); // update FPS Counter
	}

	/**
	 * Calculate how many milliseconds have passed
	 * since last frame.
	 *
	 * @return milliseconds passed since last frame
	 */
	public int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;

		return delta;
	}

	/**
	 * Get the accurate system time
	 *
	 * @return The system time in milliseconds
	 */
	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps + " - Generation: " + world.generation + " - Population: " + world.count());
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public void renderGL() {

		// Clear the screen / depth buffers and load identity modelview matrix
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();

		// Position the camera
		camera.position();

		// Draw the cubes
		cubes.draw();

		// Draw the grid if needed
		if(drawing || selecting) {
			grid.draw(camera);
		}
	}

	public static void main(String[] argv) {
		ThrashLife thrashLife = new ThrashLife();
		thrashLife.start();
	}
}