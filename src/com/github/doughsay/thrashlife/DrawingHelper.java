package com.github.doughsay.thrashlife;

import java.util.Random;

public class DrawingHelper {
	private final LifeWorld world;

	public DrawingHelper(LifeWorld world) {
		this.world = world;
	}

	public void line(int width, int ox, int oy, int oz) {
		int half = width / 2;
		for(int x = -half; x <= half; x++) {
			for(int y = 0; y <= 1; y++) {
				for(int z = 0; z <= 1; z++) {
					world.set(ox+x, oy+y, oz+z);
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
						world.set(ox+x, oy+y, oz+z);
					}
				}
			}
		}
	}

	public void blinker(int x, int y, int z) {
		world.set(x-1, y,   z);
		world.set(  x, y,   z);
		world.set(x+1, y,   z);
		world.set(x-1, y+1, z);
		world.set(  x, y+1, z);
		world.set(x+1, y+1, z);
	}

	public void glider(int x, int y, int z) {
		world.set(  x,   y,   z);
		world.set(  x, y+1,   z);
		world.set(  x, y+2,   z);
		world.set(x+1, y+2,   z);
		world.set(x+2, y+1,   z);
		world.set(  x,   y, z+1);
		world.set(  x, y+1, z+1);
		world.set(  x, y+2, z+1);
		world.set(x+1, y+2, z+1);
		world.set(x+2, y+1, z+1);
	}
}
