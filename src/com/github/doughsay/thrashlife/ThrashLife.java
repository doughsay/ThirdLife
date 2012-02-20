package com.github.doughsay.thrashlife;

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

	private int cameraX = 0;
	private int cameraY = 0;
	private int cameraDist = 10;

	private int screenX = 800;
	private int screenY = 600;

	private boolean playing = false;

	private LifeWorld world = new LifeWorld();

	public void start() {
		line(100);

		initGL(); // init OpenGL
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

	public void line(int width) {
		int half = width / 2;
		for(int x = -half; x <= half; x++) {
			for(int y = 0; y <= 1; y++) {
				for(int z = 0; z <= 1; z++) {
					set(x, y, z);
				}
			}
		}
	}

	public void blinker() {
		set(-1,0,0);
		set(0,0,0);
		set(1,0,0);
		set(-1,1,0);
		set(0,1,0);
		set(1,1,0);
	}

	public void glider() {
		set(0, 0, 0);
		set(0, 1, 0);
		set(0, 2, 0);
		set(1, 2, 0);
		set(2, 1, 0);
		set(0, 0, 1);
		set(0, 1, 1);
		set(0, 2, 1);
		set(1, 2, 1);
		set(2, 1, 1);
	}

	public void step(int steps) {
		if(world.root.width() > Math.pow(2, 14)) {
			world.collect();
		}
		world.step(steps);
		System.out.println("root level: " + world.root.level);
		System.out.println("root width: " + world.root.width());
		System.out.println("origin: " + world.originx + "," + world.originy + "," + world.originz);
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
	}

	public void update(int delta) {
		if(Mouse.isButtonDown(1)) {
			cameraX += Mouse.getDX();
			cameraY -= Mouse.getDY();

			if(cameraX > 360) { cameraX -= 360; }
			if(cameraX < 0) { cameraX += 360; }

			if(cameraY > 90) { cameraY = 90; }
			if(cameraY < -90) { cameraY = -90; }
		}

		cameraDist -= (Mouse.getDWheel() / 120);

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					step(1);
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
			Display.setTitle("FPS: " + fps + " - Population: " + world.count());
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public void renderGL() {
		// Clear the screen and depth buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GL11.glLoadIdentity();
		GL11.glTranslatef(0f, 0f, -cameraDist);

		GL11.glRotatef(cameraY, 1f, 0f, 0f);
		GL11.glRotatef(cameraX, 0f, 1f, 0f);

		for(Point cell : world.getAll()) {
			Cube.wireCubeAt(0.9f, cell.x, cell.y, cell.z);
		}
	}

	public static void main(String[] argv) {
		ThrashLife thrashLife = new ThrashLife();
		thrashLife.start();
	}
}