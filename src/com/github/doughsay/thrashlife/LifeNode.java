package com.github.doughsay.thrashlife;

import java.util.ArrayList;
import java.util.HashMap;

public class LifeNode {

	public int level;
	public int count;
	public int id;
	public LifeNode nw, ne, sw, se;
	private LifeWorld world;
	private HashMap<Integer, LifeNode> cache = new HashMap<Integer, LifeNode>();

	public LifeNode(LifeWorld world, int id, LifeNode nw, LifeNode ne, LifeNode sw, LifeNode se) {
		if(id <= 1) {
			level = 0;
			count = id;
		}
		else {
			level = nw.level + 1;
			count = nw.count + ne.count + sw.count + se.count;
		}
		this.id = id;
		this.nw = nw;
		this.ne = ne;
		this.sw = sw;
		this.se = se;
		this.world = world;
	}

	public LifeNode(LifeWorld world, int id, LifeNode[] children) {
		this(world, id, children[0], children[1], children[2], children[3]);
	}

	public LifeNode(LifeWorld world, int id) {
		this(world, id, null, null, null, null);
	}

	public LifeNode[] childrenArray() {
		return new LifeNode[] {nw, ne, sw, se};
	}

	public int get(int x, int y) {
		if(level == 0) {
			return count;
		}
		int half = width() / 2;
		LifeNode child = childrenArray()[x / half + y / half * 2];
		return child.get(x % half, y % half);
	}

	public void getList(ArrayList<Point> result, int x, int y, int[] rect) {
		if(count == 0) {
			return;
		}
		if(rect != null) {
			int minx, miny, maxx, maxy;
			minx = rect[0];
			miny = rect[1];
			maxx = rect[2];
			maxy = rect[3];
			if(x >= maxx || x + width() <= minx || y >= maxy || y + width() <= miny) {
				return;
			}
		}
		if(level == 0) {
			result.add(new Point(x, y));
		}
		else {
			int half = width() / 2;
			nw.getList(result, x, y, rect);
			ne.getList(result, x + half, y, rect);
			sw.getList(result, x, y + half, rect);
			se.getList(result, x + half, y + half, rect);
		}
	}

	public LifeNode set(int x, int y, int value) {
		if(level == 0) {
			return world.single[value];
		}
		int half = width() / 2;
		int index = x / half + y / half * 2;
		LifeNode[] children = childrenArray();
		children[index] = children[index].set(x % half, y % half, value);
		return world.getNode(children);
	}

	public LifeNode nextCenter(int steps) {
		if(steps == 0) {
			return center();
		}
		if(cache.containsKey(steps)) {
			return cache.get(steps);
		}
		LifeNode result;
		if(level == 2) {
			int aa, ab, ba, bb;
			int ac, ad, bc, bd;
			int ca, cb, da, db;
			int cc, cd, dc, dd;
			aa = nw.nw.id; ab = nw.ne.id; ba = nw.sw.id; bb = nw.se.id;
			ac = ne.nw.id; ad = ne.ne.id; bc = ne.sw.id; bd = ne.se.id;
			ca = sw.nw.id; cb = sw.ne.id; da = sw.sw.id; db = sw.se.id;
			cc = se.nw.id; cd = se.ne.id; dc = se.sw.id; dd = se.se.id;
			int nwscore = Life.score(bb, aa + ab + ac + ba + bc + ca + cb + cc);
			int nescore = Life.score(bc, ab + ac + ad + bb + bd + cb + cc + cd);
			int swscore = Life.score(cb, ba + bb + bc + ca + cc + da + db + dc);
			int sescore = Life.score(cc, bb + bc + bd + cb + cd + db + dc + dd);
			result = world.memo.get(new MemoKey(nwscore, nescore, swscore, sescore));
		}
		else {
			int halfsteps = genSteps() / 2;
			int step1;
			if(steps <= halfsteps) {
				step1 = 0;
			}
			else {
				step1 = halfsteps;
			}
			int step2 = steps - step1;
			LifeNode n00 = subQuad(0).nextCenter(step1);
			LifeNode n01 = subQuad(1).nextCenter(step1);
			LifeNode n02 = subQuad(2).nextCenter(step1);
			LifeNode n10 = subQuad(3).nextCenter(step1);
			LifeNode n11 = subQuad(4).nextCenter(step1);
			LifeNode n12 = subQuad(5).nextCenter(step1);
			LifeNode n20 = subQuad(6).nextCenter(step1);
			LifeNode n21 = subQuad(7).nextCenter(step1);
			LifeNode n22 = subQuad(8).nextCenter(step1);
			result = world.getNode(
				world.getNode(n00, n01, n10, n11).nextCenter(step2),
				world.getNode(n01, n02, n11, n12).nextCenter(step2),
				world.getNode(n10, n11, n20, n21).nextCenter(step2),
				world.getNode(n11, n12, n21, n22).nextCenter(step2)
			);
		}
		cache.put(steps, result);
		return result;
	}

	public LifeNode center() {
		if(cache.containsKey(0)) {
			return cache.get(0);
		}
		LifeNode result = world.getNode(
			nw.se,
			ne.sw,
			sw.ne,
			se.nw
		);
		cache.put(0, result);
		return result;
	}

	public LifeNode subQuad(int i) {
		if(i == 0) { return nw; }
		if(i == 1) { return world.getNode(nw.ne, ne.nw, nw.se, ne.sw); }
		if(i == 2) { return ne; }
		if(i == 3) { return world.getNode(nw.sw, nw.se, sw.nw, sw.ne); }
		if(i == 4) { return center(); }
		if(i == 5) { return world.getNode(ne.sw, ne.se, se.nw, se.ne); }
		if(i == 6) { return sw; }
		if(i == 7) { return world.getNode(sw.ne, se.nw, sw.se, se.sw); }
		if(i == 8) { return se; }
		return null; // this should never happen, right?
	}

	public int width() {
		return 1 << level;
	}

	public int genSteps() {
		return 1 << (level - 2);
	}

}
