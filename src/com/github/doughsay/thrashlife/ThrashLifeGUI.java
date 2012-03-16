package com.github.doughsay.thrashlife;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.github.doughsay.thrashlife.ThrashLife.LifeAction;

public class ThrashLifeGUI extends JFrame {

	private static final long serialVersionUID = 4398203863803397973L;

	private static final int preferredScreenX = 1280;
	private static final int preferredScreenY = 1024;
	private static final int minScreenX = 640;
	private static final int minScreenY = 480;

	private static final AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();

	private final Canvas canvas;
	private final ThrashLife lifeApp;

	private boolean closeRequested = false;

	public ThrashLifeGUI(ThrashLife lifeApp) {
		super("ThrashLife");

		this.lifeApp = lifeApp;

		initLookAndFeel();
		initMenus();

		/*
		JToolBar toolBar = new JToolBar();
		frame.add(toolBar, BorderLayout.PAGE_START);
		*/

		canvas = new Canvas();

		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				newCanvasSize.set(canvas.getSize());
			}
		});

		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				canvas.requestFocusInWindow();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeRequested = true;
			}
		});

		add(canvas, BorderLayout.CENTER);

		setPreferredSize(new Dimension(preferredScreenX, preferredScreenY));
		setMinimumSize(new Dimension(minScreenX, minScreenY));
		pack();

		setVisible(true);
	}

	public void initDisplay() throws LWJGLException {
		Display.setParent(canvas);
		Display.setVSyncEnabled(true);
		Display.create();
	}

	public boolean isCloseRequested() {
		return closeRequested;
	}

	// return the current dimensions of the canvas
	public Dimension getCanvasDimensions() {
		return canvas.getSize();
	}

	// return the new dimensions of the canvas, if they have changed, null otherwise
	public Dimension getNewCanvasDimensions() {
		return newCanvasSize.getAndSet(null);
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
				lifeApp.enqueueAction(LifeAction.STEP);
			}
		});

		final JMenuItem doubleStepItem = new JMenuItem("Double Step", KeyEvent.VK_D);
		doubleStepItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, mask));
		doubleStepItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lifeApp.enqueueAction(LifeAction.DOUBLE_STEP);
			}
		});

		final JMenuItem playPauseItem = new JMenuItem("Play", KeyEvent.VK_P);
		playPauseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
		playPauseItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(lifeApp.isPlaying()) {
					lifeApp.enqueueAction(LifeAction.PAUSE);
					playPauseItem.setText("Play");
					stepItem.setEnabled(true);
					doubleStepItem.setEnabled(true);
				}
				else {
					lifeApp.enqueueAction(LifeAction.PLAY);
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
				if(lifeApp.isPlaying()) {
					lifeApp.enqueueAction(LifeAction.PAUSE);
					playPauseItem.setText("Play");
					stepItem.setEnabled(true);
					doubleStepItem.setEnabled(true);
				}
				lifeApp.enqueueAction(LifeAction.CLEAR);
			}
		});

		JMenuItem openItem = new JMenuItem("Open...", KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));

		JMenuItem saveItem = new JMenuItem("Save As...", KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
		////

		menuBar.add(fileMenu);
		fileMenu.add(clearItem);
		fileMenu.addSeparator();
		fileMenu.add(openItem);
		fileMenu.add(saveItem);

		menuBar.add(editMenu);
		editMenu.add(drawModeItem);

		runMenu.add(playPauseItem);
		runMenu.addSeparator();
		runMenu.add(stepItem);
		runMenu.add(doubleStepItem);
		menuBar.add(runMenu);

		setJMenuBar(menuBar);
	}

	private void initLookAndFeel() {
		if(isMac()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
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

}
