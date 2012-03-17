package com.github.doughsay.thrashlife;

public class PointBrush implements Brush {

	private LifeWorld world;

	public PointBrush(LifeWorld world) {
		this.world = world;
	}

	@Override
	public void draw(int x, int y, int z) {
		world.set(x, y, z);
	}

}
