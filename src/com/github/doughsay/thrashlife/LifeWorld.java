package com.github.doughsay.thrashlife;

import java.util.ArrayList;
import java.util.HashMap;

public class LifeWorld {

	private int originx, originy;
	public LifeNode[] single = new LifeNode[2];
	public HashMap<MemoKey, LifeNode> memo = new HashMap<MemoKey, LifeNode>();
	private ArrayList<LifeNode> empty = new ArrayList<LifeNode>();
	private int nextId;
	public LifeNode root;

	public LifeWorld() {
		LifeNode E, X;

		originx = 0;
		originy = 0;
		E = new LifeNode(this, 0);
		X = new LifeNode(this, 1);
		single[0] = E;
		single[1] = X;

		for(int i = 0; i < 16; i++) {
			MemoKey key = new MemoKey(i & 1, (i & 2) / 2, (i & 4) / 4, (i & 8) / 8);
			LifeNode children[] = new LifeNode[4];
			for(int k = 0; k < 4; k++) {
				children[k] = single[key.toArray()[k]];
			}
			memo.put(key, new LifeNode(this, i + 2, children));
		}

		empty.add(E);
		empty.add(memo.get(new MemoKey(0,0,0,0)));
		nextId = 18;
		root = E;
	}

	public LifeNode getNode(LifeNode nw, LifeNode ne, LifeNode sw, LifeNode se) {
		MemoKey key = new MemoKey(nw.id, ne.id, sw.id, se.id);
		if(memo.containsKey(key)) {
			return memo.get(key);
		}
		else {
			LifeNode result = new LifeNode(this, nextId, nw, ne, sw, se);
			nextId++;
			memo.put(key, result);
			return result;
		}
	}

	public LifeNode getNode(LifeNode[] children) {
		return getNode(children[0], children[1], children[2], children[3]);
	}

	public LifeNode emptyNode(int level) {
		if(level < empty.size()) {
			return empty.get(level);
		}
		LifeNode e = emptyNode(level - 1);
		LifeNode result = getNode(e, e, e, e);
		empty.add(result);
		return result;
	}

	public LifeNode canonicalize(LifeNode node, HashMap<Integer, LifeNode> trans) {
		if(node.id < 18) {
			return node;
		}
		if(!trans.containsKey(node.id)) {
			trans.put(node.id, getNode(
				canonicalize(node.nw, trans),
				canonicalize(node.ne, trans),
				canonicalize(node.sw, trans),
				canonicalize(node.se, trans)
			));
		}
		return trans.get(node.id);
	}

	public void clear() {
		root = single[0];
		collect();
	}

	public void collect() {
		trim();
		empty.clear();
		empty.add(single[0]);
		empty.add(memo.get(new MemoKey(0,0,0,0)));
		HashMap<MemoKey, LifeNode> oldMemo = memo;
		memo = new HashMap<MemoKey, LifeNode>();
		for(int i = 0; i < 16; i++) {
			MemoKey key = new MemoKey(i & 1, (i & 2) / 2, (i & 4) / 4, (i & 8) / 8);
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
			for(int i = 0; i < 9; i++) {
				LifeNode sub = root.subQuad(i);
				if(sub.count == root.count) {
					originx += sub.width() / 2 * (i % 3);
					originy += sub.width() / 2 * (i / 3);
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
			root = memo.get(new MemoKey(root.id, 0, 0, 0));
			return;
		}
		originx -= root.width() / 2;
		originy -= root.width() / 2;
		LifeNode e = emptyNode(root.level - 1);
		root = getNode(
			getNode(e, e, e, root.nw), getNode(e, e, root.ne, e),
			getNode(e, root.sw, e, e), getNode(root.se, e, e, e)
		);
	}

	public int get(int x, int y) {
		if(x < originx || y < originy || x >= originx + root.width() || y >= originy + root.width()) {
			return 0;
		}
		else {
			return root.get(x - originx, y - originy);
		}
	}

	public ArrayList<Point> getAll(int[] rect) {
		ArrayList<Point> cells = new ArrayList<Point>();
		root.getList(cells, originx, originy, rect);
		return cells;
	}

	public void set(int x, int y, int value) {
		if(get(x,  y) == value) {
			return;
		}
		while(x < originx || y < originy || x >= originx + root.width() || y >= originy + root.width()) {
			dbl();
		}
		root = root.set(x - originx, y - originy, value);
	}

	public void step(int steps) {
		if(steps == 0) {
			return;
		}
		dbl();
		dbl();
		while(steps > root.genSteps()) {
			steps -= root.genSteps();
			root = root.nextCenter(root.genSteps());
			originx = originx + root.width() / 2;
			originy = originy + root.width() / 2;
			dbl();
			dbl();
		}
		root = root.nextCenter(steps);
		originx = originx + root.width() / 2;
		originy = originy + root.width() / 2;
	}

	public int count() {
		return root.count;
	}
}
