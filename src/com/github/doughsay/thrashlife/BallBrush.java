package com.github.doughsay.thrashlife;

public class BallBrush implements Brush {

	private LifeWorld world;

	public BallBrush(LifeWorld world) {
		this.world = world;
	}

	@Override
	public void draw(int x, int y, int z) {
		world.set(x, y, z);
		world.set(x+1, y, z);
		world.set(x-1, y, z);
		world.set(x, y+1, z);
		world.set(x, y-1, z);
		world.set(x, y, z+1);
		world.set(x, y, z-1);
	}

}
