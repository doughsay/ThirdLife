package com.github.doughsay.thrashlife;

import java.util.Random;

import com.github.doughsay.thrashlife.Grid.Plane;

public class DrawingHelper {
	private final LifeWorld world;

	public DrawingHelper(LifeWorld world) {
		this.world = world;
	}

	public void thickLine(int width, int ox, int oy, int oz) {
		int half = width / 2;
		for(int x = -half; x <= half; x++) {
			for(int y = 0; y <= 1; y++) {
				for(int z = 0; z <= 1; z++) {
					world.set(ox+x, oy+y, oz+z);
				}
			}
		}
	}

	public void line(int x1, int y1, int z1, int x2, int y2, int z2) {
		line(x1, y1, z1, x2, y2, z2, PointBrush.class);
	}

	// fake 3d Bresenham line drawing (assume axis-aligned plane)
	public void line(int x1, int y1, int z1, int x2, int y2, int z2, Class<? extends Brush> brush) {
		if(x1 == x2) { line(z1, y1, z2, y2, x1, Plane.YZ, brush); return; }
		if(y1 == y2) { line(x1, z1, x2, z2, y1, Plane.XZ, brush); return; }
		if(z1 == z2) { line(x1, y1, x2, y2, z1, Plane.XY, brush); return; }
	}

	private void line(int x, int y, int x2, int y2, int other, Plane plane, Class<? extends Brush> brush) {
		Brush brushInstance = null;
		try {
			brushInstance = brush.getConstructor(LifeWorld.class).newInstance(world);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		int w = x2 - x;
		int h = y2 - y;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if(w<0) { dx1 = -1; } else if(w>0) { dx1 = 1; }
		if(h<0) { dy1 = -1; } else if(h>0) { dy1 = 1; }
		if(w<0) { dx2 = -1; } else if(w>0) { dx2 = 1; }
		int longest = Math.abs(w);
		int shortest = Math.abs(h);
		if(!(longest > shortest)) {
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if(h<0) { dy2 = -1; } else if(h>0) { dy2 = 1; }
			dx2 = 0;
		}
		int numerator = longest >> 1;
		for(int i = 0; i <= longest; i++) {
			switch(plane) {
			case XY:
				brushInstance.draw(x, y, other);
				//world.set(x, y, other);
				break;
			case YZ:
				brushInstance.draw(other, y, x);
				//world.set(other, y, x);
				break;
			case XZ:
				brushInstance.draw(x, other, y);
				//world.set(x, other, y);
				break;
			}
			numerator += shortest;
			if(!(numerator < longest)) {
				numerator -= longest;
				x += dx1;
				y += dy1;
			}
			else {
				x += dx2;
				y += dy2;
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
