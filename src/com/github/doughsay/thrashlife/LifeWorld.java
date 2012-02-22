package com.github.doughsay.thrashlife;

import java.util.ArrayList;
import java.util.HashMap;

public class LifeWorld {

	public int originx, originy, originz;
	public LifeNode[] single = new LifeNode[2];
	public HashMap<MemoKey, LifeNode> memo = new HashMap<MemoKey, LifeNode>();
	private ArrayList<LifeNode> empty = new ArrayList<LifeNode>();
	private int nextId;
	public LifeNode root;
	public int generation = 0;

	public LifeWorld() {
		LifeNode E, X;

		originx = 0;
		originy = 0;
		originz = 0;
		E = new LifeNode(this, 0);
		X = new LifeNode(this, 1);
		single[0] = E;
		single[1] = X;

		for(int i = 0; i < 256; i++) {
			MemoKey key = new MemoKey(i & 1, (i & 2) / 2, (i & 4) / 4, (i & 8) / 8, (i & 16) / 16, (i & 32) / 32, (i & 64) / 64, (i & 128) / 128);
			LifeNode children[] = new LifeNode[8];
			for(int k = 0; k < 8; k++) {
				children[k] = single[key.toArray()[k]];
			}
			memo.put(key, new LifeNode(this, i + 2, children));
		}

		empty.add(E);
		empty.add(memo.get(new MemoKey(0,0,0,0,0,0,0,0)));
		nextId = 258;
		root = E;
	}

	public LifeNode getNode(LifeNode fnw, LifeNode fne, LifeNode fsw, LifeNode fse, LifeNode bnw, LifeNode bne, LifeNode bsw, LifeNode bse) {
		MemoKey key = new MemoKey(fnw.id, fne.id, fsw.id, fse.id, bnw.id, bne.id, bsw.id, bse.id);
		if(memo.containsKey(key)) {
			return memo.get(key);
		}
		else {
			LifeNode result = new LifeNode(this, nextId, fnw, fne, fsw, fse, bnw, bne, bsw, bse);
			nextId++;
			memo.put(key, result);
			return result;
		}
	}

	public LifeNode getNode(LifeNode[] children) {
		return getNode(children[0], children[1], children[2], children[3], children[4], children[5], children[6], children[7]);
	}

	public LifeNode emptyNode(int level) {
		if(level < empty.size()) {
			return empty.get(level);
		}
		LifeNode e = emptyNode(level - 1);
		LifeNode result = getNode(e, e, e, e, e, e, e, e);
		empty.add(result);
		return result;
	}

	public LifeNode canonicalize(LifeNode node, HashMap<Integer, LifeNode> trans) {
		if(node.id < 258) {
			return node;
		}
		if(!trans.containsKey(node.id)) {
			trans.put(node.id, getNode(
				canonicalize(node.fnw, trans),
				canonicalize(node.fne, trans),
				canonicalize(node.fsw, trans),
				canonicalize(node.fse, trans),
				canonicalize(node.bnw, trans),
				canonicalize(node.bne, trans),
				canonicalize(node.bsw, trans),
				canonicalize(node.bse, trans)
			));
		}
		return trans.get(node.id);
	}

	public void clear() {
		root = single[0];
		collect();
		generation = 0;
	}

	public void collect() {
		trim();
		empty.clear();
		empty.add(single[0]);
		empty.add(memo.get(new MemoKey(0,0,0,0,0,0,0,0)));
		HashMap<MemoKey, LifeNode> oldMemo = memo;
		memo = new HashMap<MemoKey, LifeNode>();
		for(int i = 0; i < 256; i++) {
			MemoKey key = new MemoKey(i & 1, (i & 2) / 2, (i & 4) / 4, (i & 8) / 8, (i & 16) / 16, (i & 32) / 32, (i & 64) / 64, (i & 128) / 128);
			memo.put(key, oldMemo.get(key));
		}
		HashMap<Integer, LifeNode> trans = new HashMap<Integer, LifeNode>();
		root = canonicalize(root, trans);
	}

	public void trim() {
		while(true) {
			if(root.count == 0) {
				root = single[0];
			}
			if(root.level <= 1) {
				return;
			}
			boolean pyElse = true; // imitate python for...else loop
			for(int i = 0; i < 27; i++) {
				LifeNode sub = root.subQuad(i);
				if(sub.count == root.count) {
					originx += sub.width() / 2 * (i % 3);
					originy += sub.width() / 2 * ((i / 3) % 3);
					originz += sub.width() / 2 * (i / 9);
					root = sub;
					pyElse = false;
					break;
				}
			}
			if(pyElse) {
				return;
			}
		}
	}

	public void dbl() {
		if(root.level == 0) {
			root = memo.get(new MemoKey(root.id, 0, 0, 0, 0, 0, 0, 0));
			return;
		}
		originx -= root.width() / 2;
		originy -= root.width() / 2;
		originz -= root.width() / 2;
		LifeNode e = emptyNode(root.level - 1);
		root = getNode(
			getNode(e, e, e, e, e, e, e, root.fnw), getNode(e, e, e, e, e, e, root.fne, e),
			getNode(e, e, e, e, e, root.fsw, e, e), getNode(e, e, e, e, root.fse, e, e, e),
			getNode(e, e, e, root.bnw, e, e, e, e), getNode(e, e, root.bne, e, e, e, e, e),
			getNode(e, root.bsw, e, e, e, e, e, e), getNode(root.bse, e, e, e, e, e, e, e)
		);
	}

	public int get(int x, int y, int z) {
		if(x < originx || y < originy || z < originz || x >= originx + root.width() || y >= originy + root.width() || z >= originz + root.width()) {
			return 0;
		}
		else {
			return root.get(x - originx, y - originy, z - originz);
		}
	}

	public ArrayList<Point> getAll() {
		ArrayList<Point> cells = new ArrayList<Point>();
		root.getList(cells, originx, originy, originz);
		return cells;
	}

	public void set(int x, int y, int z, int value) {
		if(get(x,  y, z) == value) {
			return;
		}
		while(x < originx || y < originy || z < originz || x >= originx + root.width() || y >= originy + root.width() || z >= originz + root.width()) {
			dbl();
		}
		root = root.set(x - originx, y - originy, z - originz, value);
	}

	public void step(int steps) {
		if(steps == 0) {
			return;
		}
		dbl();
		dbl();
		/*while(steps > root.genSteps()) {
			steps -= root.genSteps();
			root = root.nextCenter(root.genSteps());
			originx = originx + root.width() / 2;
			originy = originy + root.width() / 2;
			originz = originz + root.width() / 2;
			dbl();
			dbl();
		}*/
		root = root.nextCenter(steps);
		originx = originx + root.width() / 2;
		originy = originy + root.width() / 2;
		originz = originz + root.width() / 2;

		generation += steps;
	}

	public int count() {
		return root.count;
	}
}
