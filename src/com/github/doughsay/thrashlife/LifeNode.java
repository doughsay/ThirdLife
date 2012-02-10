package com.github.doughsay.thrashlife;

import java.util.ArrayList;
import java.util.HashMap;

public class LifeNode {

	public int level;
	public int count;
	public int id;
	public LifeNode[] children;
	private LifeWorld world;
	private HashMap<Integer, LifeNode> cache = new HashMap<Integer, LifeNode>();

	public LifeNode(LifeWorld world, int id, LifeNode[] children) {
		if(id <= 1) {
			this.level = 0;
			this.count = id;
		}
		else {
			LifeNode nw, ne, sw, se;
			nw = children[0];
			ne = children[1];
			sw = children[2];
			se = children[3];
			this.level = nw.level + 1;
			this.count = nw.count + ne.count + sw.count + se.count;
		}
		this.id = id;
		this.children = children;
		this.world = world;
	}

	public int get(int x, int y) {
		if(this.level == 0) {
			return this.count;
		}
		int half = this.width() / 2;
		LifeNode child = this.children[x / half + y / half * 2];
		return child.get(x % half, y % half);
	}

	public void getList(ArrayList<Point> result, int x, int y, int[] rect) {
		if(this.count == 0) {
			return;
		}
		if(rect != null) {
			int minx, miny, maxx, maxy;
			minx = rect[0];
			miny = rect[1];
			maxx = rect[2];
			maxy = rect[3];
			if(x >= maxx || x + this.width() <= minx || y >= maxy || y + this.width() <= miny) {
				return;
			}
		}
		if(this.level == 0) {
			result.add(new Point(x, y));
		}
		else {
			int half = this.width() / 2;
			LifeNode nw, ne, sw, se;
			nw = this.children[0];
			ne = this.children[1];
			sw = this.children[2];
			se = this.children[3];
			nw.getList(result, x, y, rect);
			ne.getList(result, x + half, y, rect);
			sw.getList(result, x, y + half, rect);
			se.getList(result, x + half, y + half, rect);
		}
	}

	public LifeNode set(int x, int y, int value) {
		if(this.level == 0) {
			return this.world.single[value];
		}
		int half = this.width() / 2;
		int index = x / half + y / half * 2;
		LifeNode[] children = {this.children[0], this.children[1], this.children[2], this.children[3]};
		children[index] = children[index].set(x % half, y % half, value);
		return this.world.getNode(children[0], children[1], children[2], children[3]);
	}

	public LifeNode nextCenter(int steps) {
		if(steps == 0) {
			return this.center();
		}
		if(this.cache.containsKey(steps)) {
			return this.cache.get(steps);
		}
		LifeNode nw, ne, sw, se, result;
		nw = this.children[0];
		ne = this.children[1];
		sw = this.children[2];
		se = this.children[3];
		if(this.level == 2) {
			int aa, ab, ba, bb;
			int ac, ad, bc, bd;
			int ca, cb, da, db;
			int cc, cd, dc, dd;
			aa = nw.children[0].id; ab = nw.children[1].id; ba = nw.children[2].id; bb = nw.children[3].id;
			ac = ne.children[0].id; ad = ne.children[1].id; bc = ne.children[2].id; bd = ne.children[3].id;
			ca = sw.children[0].id; cb = sw.children[1].id; da = sw.children[2].id; db = sw.children[3].id;
			cc = se.children[0].id; cd = se.children[1].id; dc = se.children[2].id; dd = se.children[3].id;
			int nwscore = Life.score(bb, aa + ab + ac + ba + bc + ca + cb + cc);
			int nescore = Life.score(bc, ab + ac + ad + bb + bd + cb + cc + cd);
			int swscore = Life.score(cb, ba + bb + bc + ca + cc + da + db + dc);
			int sescore = Life.score(cc, bb + bc + bd + cb + cd + db + dc + dd);
			result = this.world.memo.get(new MemoKey(nwscore, nescore, swscore, sescore));
		}
		else {
			int halfsteps = this.genSteps() / 2;
			int step1;
			if(steps <= halfsteps) {
				step1 = 0;
			}
			else {
				step1 = halfsteps;
			}
			int step2 = steps - step1;
			LifeNode n00 = this.subQuad(0).nextCenter(step1);
			LifeNode n01 = this.subQuad(1).nextCenter(step1);
			LifeNode n02 = this.subQuad(2).nextCenter(step1);
			LifeNode n10 = this.subQuad(3).nextCenter(step1);
			LifeNode n11 = this.subQuad(4).nextCenter(step1);
			LifeNode n12 = this.subQuad(5).nextCenter(step1);
			LifeNode n20 = this.subQuad(6).nextCenter(step1);
			LifeNode n21 = this.subQuad(7).nextCenter(step1);
			LifeNode n22 = this.subQuad(8).nextCenter(step1);
			result = this.world.getNode(
				this.world.getNode(n00, n01, n10, n11).nextCenter(step2),
				this.world.getNode(n01, n02, n11, n12).nextCenter(step2),
				this.world.getNode(n10, n11, n20, n21).nextCenter(step2),
				this.world.getNode(n11, n12, n21, n22).nextCenter(step2)
			);
		}
		this.cache.put(steps, result);
		return result;
	}

	public LifeNode center() {
		if(this.cache.containsKey(0)) {
			return this.cache.get(0);
		}
		LifeNode nw, ne, sw, se;
		nw = this.children[0];
		ne = this.children[1];
		sw = this.children[2];
		se = this.children[3];
		LifeNode result = this.world.getNode(
			nw.children[3],
			ne.children[2],
			sw.children[1],
			se.children[0]
		);
		this.cache.put(0, result);
		return result;
	}

	public LifeNode subQuad(int i) {
		LifeNode nw, ne, sw, se;
		nw = this.children[0];
		ne = this.children[1];
		sw = this.children[2];
		se = this.children[3];
		if(i == 0) { return nw; }
		if(i == 1) { return this.world.getNode(nw.children[1], ne.children[0], nw.children[3], ne.children[2]); }
		if(i == 2) { return ne; }
		if(i == 3) { return this.world.getNode(nw.children[2], nw.children[3], sw.children[0], sw.children[1]); }
		if(i == 4) { return this.center(); }
		if(i == 5) { return this.world.getNode(ne.children[2], ne.children[3], se.children[0], se.children[1]); }
		if(i == 6) { return sw; }
		if(i == 7) { return this.world.getNode(sw.children[1], se.children[0], sw.children[3], se.children[2]); }
		if(i == 8) { return se; }
		return null; // this should never happen, right?
	}

	public int width() {
		return 1 << this.level;
	}

	public int genSteps() {
		return 1 << (this.level - 2);
	}

}
