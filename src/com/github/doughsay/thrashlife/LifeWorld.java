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

		this.originx = 0;
		this.originy = 0;
		E = new LifeNode(this, 0, null);
		X = new LifeNode(this, 1, null);
		this.single[0] = E;
		this.single[1] = X;

		for(int i = 0; i < 16; i++) {
			MemoKey key = new MemoKey(i & 1, (i & 2) / 2, (i & 4) / 4, (i & 8) / 8);
			LifeNode children[] = new LifeNode[4];
			for(int k = 0; k < 4; k++) {
				children[k] = this.single[key.toArray()[k]];
			}
			this.memo.put(key, new LifeNode(this, i + 2, children));
		}

		this.empty.add(E);
		this.empty.add(this.memo.get(new MemoKey(0,0,0,0)));
		this.nextId = 18;
		this.root = E;
	}

	public LifeNode getNode(LifeNode nw, LifeNode ne, LifeNode sw, LifeNode se) {
		MemoKey key = new MemoKey(nw.id, ne.id, sw.id, se.id);
		if(this.memo.containsKey(key)) {
			return this.memo.get(key);
		}
		else {
			LifeNode[] children = {nw, ne, sw, se};
			LifeNode result = new LifeNode(this, this.nextId, children);
			this.nextId++;
			this.memo.put(key, result);
			return result;
		}
	}

	public LifeNode emptyNode(int level) {
		if(level < this.empty.size()) {
			return this.empty.get(level);
		}
		LifeNode e = this.emptyNode(level - 1);
		LifeNode result = this.getNode(e, e, e, e);
		this.empty.add(result);
		return result;
	}

	public LifeNode canonicalize(LifeNode node, HashMap<Integer, LifeNode> trans) {
		if(node.id < 18) {
			return node;
		}
		if(!trans.containsKey(node.id)) {
			LifeNode nw = node.children[0];
			LifeNode ne = node.children[1];
			LifeNode sw = node.children[2];
			LifeNode se = node.children[3];
			trans.put(node.id, this.getNode(
				this.canonicalize(nw, trans),
				this.canonicalize(ne, trans),
				this.canonicalize(sw, trans),
				this.canonicalize(se, trans)
			));
		}
		return trans.get(node.id);
	}

	public void clear() {
		this.root = this.single[0];
		this.collect();
	}

	public void collect() {
		this.trim();
		this.empty.clear();
		this.empty.add(this.single[0]);
		this.empty.add(this.memo.get(new MemoKey(0,0,0,0)));
		HashMap<MemoKey, LifeNode> oldMemo = this.memo;
		this.memo = new HashMap<MemoKey, LifeNode>();
		for(int i = 0; i < 16; i++) {
			MemoKey key = new MemoKey(i & 1, (i & 2) / 2, (i & 4) / 4, (i & 8) / 8);
			this.memo.put(key, oldMemo.get(key));
		}
		HashMap<Integer, LifeNode> trans = new HashMap<Integer, LifeNode>();
		this.root = this.canonicalize(this.root, trans);
	}

	public void trim() {
		while(true) {
			if(this.root.count == 0) {
				this.root = this.single[0];
			}
			if(this.root.level <= 1) {
				return;
			}
			boolean pyElse = true; // imitate python for...else loop
			for(int i = 0; i < 9; i++) {
				LifeNode sub = this.root.subQuad(i);
				if(sub.count == this.root.count) {
					this.originx += sub.width() / 2 * (i % 3);
					this.originy += sub.width() / 2 * (i / 3);
					this.root = sub;
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
		if(this.root.level == 0) {
			this.root = this.memo.get(new MemoKey(this.root.id, 0, 0, 0));
			return;
		}
		this.originx -= this.root.width() / 2;
		this.originy -= this.root.width() / 2;
		LifeNode e = this.emptyNode(this.root.level - 1);
		LifeNode[] children = this.root.children;
		LifeNode nw, ne, sw, se;
		nw = children[0];
		ne = children[1];
		sw = children[2];
		se = children[3];
		this.root = this.getNode(
			this.getNode(e, e, e, nw), this.getNode(e, e, ne, e),
			this.getNode(e, sw, e, e), this.getNode(se, e, e, e)
		);
	}

	public int get(int x, int y) {
		if(x < this.originx || y < this.originy || x >= this.originx + this.root.width() || y >= this.originy + this.root.width()) {
			return 0;
		}
		else {
			return this.root.get(x - this.originx, y - this.originy);
		}
	}

	public ArrayList<Point> getAll(int[] rect) {
		ArrayList<Point> cells = new ArrayList<Point>();
		this.root.getList(cells, this.originx, this.originy, rect);
		return cells;
	}

	public void set(int x, int y, int value) {
		if(this.get(x,  y) == value) {
			return;
		}
		while(x < this.originx || y < this.originy || x >= this.originx + this.root.width() || y >= this.originy + this.root.width()) {
			this.dbl();
		}
		this.root = this.root.set(x - this.originx, y - this.originy, value);
	}

	public void step(int steps) {
		if(steps == 0) {
			return;
		}
		this.dbl();
		this.dbl();
		while(steps > this.root.genSteps()) {
			steps -= this.root.genSteps();
			this.root = this.root.nextCenter(this.root.genSteps());
			this.originx = this.originx + this.root.width() / 2;
			this.originy = this.originy + this.root.width() / 2;
			this.dbl();
			this.dbl();
		}
		this.root = this.root.nextCenter(steps);
		this.originx = this.originx + this.root.width() / 2;
		this.originy = this.originy + this.root.width() / 2;
	}

	public int count() {
		return this.root.count;
	}
}
