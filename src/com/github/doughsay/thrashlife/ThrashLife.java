package com.github.doughsay.thrashlife;

import java.util.ArrayList;

public class ThrashLife {
	public static void main (String[] args) {
		LifeWorld world = new LifeWorld();
		world.set(0, 0, 1);
		world.set(0, 1, 1);
		world.set(0, 2, 1);
		printWorld(world);
		world.step(1);
		printWorld(world);
	}

	public static void printWorld(LifeWorld world) {
		ArrayList<Point> cells = world.getAll(null);
		System.out.println(cells.size() + " live cells:");
		for(Point cell : cells) {
			System.out.println("(" + cell.x + ", " + cell.y + ")");
		}
	}
}
