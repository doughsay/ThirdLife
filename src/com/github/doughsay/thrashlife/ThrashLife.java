package com.github.doughsay.thrashlife;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.input.Mouse;

public class ThrashLife {

	private Camera camera = new Camera();
	private FastCubes cubes;

	private static final int preferredScreenX = 1280;
	private static final int preferredScreenY = 1024;
	private static final int minScreenX = 640;
	private static final int minScreenY = 480;

	private boolean playing = false;

	private LifeWorld world = new LifeWorld();
	private DrawingHelper draw = new DrawingHelper(world);

	private boolean closeRequested = false;
	private boolean render = true;
	private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();

	private JFrame frame;

	private LinkedList<LifeAction> actions = new LinkedList<LifeAction>();

	public ThrashLife() {

		try {
			initDisplay();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
		}

		initGL();
		cubes = new FastCubes(); // GL has to init before we can init the FastCubes class

		// put some initial cells for testing
		draw.line(200, 0, 0, 0);
		cubes.load(world.getAll()); // load the current world state as geometry

		updateTitle();

		while(!Display.isCloseRequested() && !closeRequested) {

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
		frame.dispose();
		System.exit(0);
	}

	private void step(int steps) {
		world.step(steps);
		updateTitle();
		cubes.load(world.getAll());
	}

	private void updateTitle() {
		frame.setTitle("Thrashlife - Generation: " + world.generation + " - Population: " + world.count());
	}

	private void initDisplay() throws LWJGLException {
		initLookAndFeel();

		frame = new JFrame("Test");

		initMenus();

		final Canvas canvas = new Canvas();

		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				newCanvasSize.set(canvas.getSize());
			}
		});

		frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				canvas.requestFocusInWindow();
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeRequested = true;
			}
		});

		frame.getContentPane().add(canvas);

		Display.setParent(canvas);
		Display.setVSyncEnabled(true);

		frame.setPreferredSize(new Dimension(preferredScreenX, preferredScreenY));
		frame.setMinimumSize(new Dimension(minScreenX, minScreenY));
		frame.pack();
		frame.setVisible(true);
		Display.create();
	}

	private void initMenus() {

		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		//Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		//Edit menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);

		JMenuItem drawModeItem = new JCheckBoxMenuItem("Draw Mode");
		drawModeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, mask));
		////

		//Run menu
		JMenu runMenu = new JMenu("Run");
		runMenu.setMnemonic(KeyEvent.VK_R);

		final JMenuItem stepItem = new JMenuItem("Step", KeyEvent.VK_T);
		stepItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		stepItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actions.add(LifeAction.STEP);
			}
		});

		final JMenuItem doubleStepItem = new JMenuItem("Double Step", KeyEvent.VK_D);
		doubleStepItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, mask));
		doubleStepItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actions.add(LifeAction.DOUBLE_STEP);
			}
		});

		final JMenuItem playPauseItem = new JMenuItem("Play", KeyEvent.VK_P);
		playPauseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
		playPauseItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(playing) {
					actions.add(LifeAction.PAUSE);
					playPauseItem.setText("Play");
					stepItem.setEnabled(true);
					doubleStepItem.setEnabled(true);
				}
				else {
					actions.add(LifeAction.PLAY);
					playPauseItem.setText("Pause");
					stepItem.setEnabled(false);
					doubleStepItem.setEnabled(false);
				}
			}
		});
		////

		//File menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem clearItem = new JMenuItem("Clear", KeyEvent.VK_N);
		clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
		clearItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(playing) {
					actions.add(LifeAction.PAUSE);
					playPauseItem.setText("Play");
					stepItem.setEnabled(true);
					doubleStepItem.setEnabled(true);
				}
				actions.add(LifeAction.CLEAR);
			}
		});

		JMenuItem openItem = new JMenuItem("Open...", KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));

		JMenuItem saveItem = new JMenuItem("Save As...", KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
		////

		menuBar.add(fileMenu);
		fileMenu.add(clearItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);

		menuBar.add(editMenu);
		editMenu.add(drawModeItem);

		runMenu.add(playPauseItem);
		runMenu.add(stepItem);
		runMenu.add(doubleStepItem);
		menuBar.add(runMenu);

		frame.setJMenuBar(menuBar);
	}

	private void initLookAndFeel() {
		if(isMac()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ThrashLife");
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	private boolean isMac() {
		String osName = System.getProperty("os.name");
		return osName.startsWith("Mac OS X");
	}

	private void initGL() {
		setViewport(preferredScreenX, preferredScreenY);

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
		Dimension newDim = newCanvasSize.getAndSet(null);

		if(newDim != null) {
			setViewport(newDim.width, newDim.height);
			render = true;
		}

		// step if playing
		if(playing) {
			step(1);
			render = true;
		}
	}

	private void renderGL() {

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

	public enum LifeAction { CLEAR, PLAY, PAUSE, STEP, DOUBLE_STEP };
}