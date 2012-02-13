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

	private LifeWorld world = new LifeWorld();

	public void start() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		for(int x = -100; x <= 100; x++) {
			for(int y = 0; y <= 1; y++) {
				for(int z = 0; z <= 1; z++) {
					world.set(x, y, z, 1);
				}
			}
		}

		initGL(); // init OpenGL
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer

		while (!Display.isCloseRequested()) {
			int delta = getDelta();

			update(delta);
			renderGL();

			Display.update();
			Display.sync(60); // cap fps to 60fps
		}

		Display.destroy();
	}

	public void step(int steps) {
		if(world.root.width() > Math.pow(2, 28)) {
			System.out.println("Collecting...");
			world.collect();
			System.out.println("new origin: {" + world.originx + "," + world.originy + "," + world.originz + "}");
		}
		world.step(steps);
	}

	public void initGL() {
		DisplayMode DM = Display.getDisplayMode();
		int width = DM.getWidth();
		int height = DM.getHeight();
		float aspectRatio = (float)width / (float)height;
		GL11.glViewport(0, 0, width, height);
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
			Display.setTitle("FPS: " + fps);
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