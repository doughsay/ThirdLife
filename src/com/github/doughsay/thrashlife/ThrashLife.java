package com.github.doughsay.thrashlife;

import java.util.ArrayList;

public class ThrashLife {
	public static void main (String[] args) {
		LifeWorld world = new LifeWorld();
		for(int x = -100; x <= 100; x++) {
			for(int y = 0; y <= 1; y++) {
				for(int z = 0; z <= 1; z++) {
					world.set(x, y, z, 1);
				}
			}
		}

		int generation = 0;
		int population = world.count();
		System.out.println(generation + ": " + population);
		for(int i = 0, j = 1; i < 125; i += j) {
			if(world.root.width() > (Math.pow(2, 28))) {
				System.out.println("Collecting...");
				world.collect();
			}
			world.step(j);
			generation += j;
			population = world.count();
			System.out.println(generation + ": " + population);
		}
	}

	public static void printWorld(LifeWorld world) {
		ArrayList<Point> cells = world.getAll();
		System.out.println(world.count() + " live cells:");
		for(Point cell : cells) {
			System.out.println("(" + cell.x + ", " + cell.y + ", " + cell.z + ")");
		}
	}
}
