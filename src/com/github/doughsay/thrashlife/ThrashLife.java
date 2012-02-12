package com.github.doughsay.thrashlife;

import java.util.ArrayList;

public class ThrashLife {
	public static void main (String[] args) {
		LifeWorld world = new LifeWorld();
		world.set(0, 1, 1);
		world.set(0, 2, 1);
		world.set(1, 0, 1);
		world.set(1, 1, 1);
		world.set(2, 1, 1);
		world.set(4, 2, 1);
		world.set(5, 1, 1);
		world.set(5, 2, 1);
		world.set(6, 2, 1);

		int generation = 0;
		int population = world.count();
		System.out.println(generation + ": " + population);
		for(int i = 0, j = 1; i < 18000; i += j) {
			if(world.root.width() > (Math.pow(2, 28))) {
				world.collect();
			}
			world.step(j);
			generation += j;
			population = world.count();
			System.out.println(generation + ": " + population);
		}
	}

	public static void printWorld(LifeWorld world) {
		ArrayList<Point> cells = world.getAll(null);
		System.out.println(world.count() + " live cells:");
		for(Point cell : cells) {
			System.out.println("(" + cell.x + ", " + cell.y + ")");
		}
	}
}
