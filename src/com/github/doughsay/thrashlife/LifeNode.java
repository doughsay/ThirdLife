package com.github.doughsay.thrashlife;

import java.util.ArrayList;
import java.util.HashMap;

public final class LifeNode {

	public final int level;
	public final int count;
	public final int id;
	public final LifeNode fnw, fne, fsw, fse, bnw, bne, bsw, bse;
	private LifeWorld world;
	private HashMap<Integer, LifeNode> cache = new HashMap<Integer, LifeNode>();

	public LifeNode(LifeWorld world, int id, LifeNode fnw, LifeNode fne, LifeNode fsw, LifeNode fse, LifeNode bnw, LifeNode bne, LifeNode bsw, LifeNode bse) {
		if(id <= 1) {
			level = 0;
			count = id;
		}
		else {
			level = fnw.level + 1;
			count = fnw.count + fne.count + fsw.count + fse.count + bnw.count + bne.count + bsw.count + bse.count;
		}
		this.id = id;
		this.fnw = fnw;
		this.fne = fne;
		this.fsw = fsw;
		this.fse = fse;
		this.bnw = bnw;
		this.bne = bne;
		this.bsw = bsw;
		this.bse = bse;
		this.world = world;
	}

	public LifeNode(LifeWorld world, int id, LifeNode[] children) {
		this(world, id, children[0], children[1], children[2], children[3], children[4], children[5], children[6], children[7]);
	}

	public LifeNode(LifeWorld world, int id) {
		this(world, id, null, null, null, null, null, null, null, null);
	}

	private LifeNode[] childrenArray() {
		return new LifeNode[] {fnw, fne, fsw, fse, bnw, bne, bsw, bse};
	}

	protected int get(int x, int y, int z) {
		if(level == 0) {
			return count;
		}
		int half = width() / 2;
		LifeNode child = childrenArray()[x / half + y / half * 2 + z / half * 4]; // TODO verify 3dification
		return child.get(x % half, y % half, z % half);
	}

	protected void getList(ArrayList<int[]> result, int x, int y, int z/*, int[] rect*/) {
		if(count == 0) {
			return;
		}
		/*
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
		*/
		if(level == 0) {
			result.add(new int[] {x, y, z});
		}
		else {
			int half = width() / 2;
			fnw.getList(result, x, y, z);
			fne.getList(result, x + half, y, z);
			fsw.getList(result, x, y + half, z);
			fse.getList(result, x + half, y + half, z);
			bnw.getList(result, x, y, z + half);
			bne.getList(result, x + half, y, z + half);
			bsw.getList(result, x, y + half, z + half);
			bse.getList(result, x + half, y + half, z + half);
		}
	}

	protected LifeNode set(int x, int y, int z, int value) {
		if(level == 0) {
			return world.single[value];
		}
		int half = width() / 2;
		int index = x / half + y / half * 2 + z / half * 4; // TODO verify 3dification
		LifeNode[] children = childrenArray();
		children[index] = children[index].set(x % half, y % half, z % half, value);
		return world.getNode(children);
	}

	protected LifeNode nextCenter(int steps) {
		if(steps == 0) {
			return center();
		}
		LifeNode result;
		result = cache.get(steps);
		if(result != null) {
			return result;
		}
		if(level == 2) {
			int aaa = fnw.fnw.id, aba = fnw.fne.id, baa = fnw.fsw.id, bba = fnw.fse.id;
			int aca = fne.fnw.id, ada = fne.fne.id, bca = fne.fsw.id, bda = fne.fse.id;
			int caa = fsw.fnw.id, cba = fsw.fne.id, daa = fsw.fsw.id, dba = fsw.fse.id;
			int cca = fse.fnw.id, cda = fse.fne.id, dca = fse.fsw.id, dda = fse.fse.id;

			int aab = fnw.bnw.id, abb = fnw.bne.id, bab = fnw.bsw.id, bbb = fnw.bse.id;
			int acb = fne.bnw.id, adb = fne.bne.id, bcb = fne.bsw.id, bdb = fne.bse.id;
			int cab = fsw.bnw.id, cbb = fsw.bne.id, dab = fsw.bsw.id, dbb = fsw.bse.id;
			int ccb = fse.bnw.id, cdb = fse.bne.id, dcb = fse.bsw.id, ddb = fse.bse.id;

			int aac = bnw.fnw.id, abc = bnw.fne.id, bac = bnw.fsw.id, bbc = bnw.fse.id;
			int acc = bne.fnw.id, adc = bne.fne.id, bcc = bne.fsw.id, bdc = bne.fse.id;
			int cac = bsw.fnw.id, cbc = bsw.fne.id, dac = bsw.fsw.id, dbc = bsw.fse.id;
			int ccc = bse.fnw.id, cdc = bse.fne.id, dcc = bse.fsw.id, ddc = bse.fse.id;

			int aad = bnw.bnw.id, abd = bnw.bne.id, bad = bnw.bsw.id, bbd = bnw.bse.id;
			int acd = bne.bnw.id, add = bne.bne.id, bcd = bne.bsw.id, bdd = bne.bse.id;
			int cad = bsw.bnw.id, cbd = bsw.bne.id, dad = bsw.bsw.id, dbd = bsw.bse.id;
			int ccd = bse.bnw.id, cdd = bse.bne.id, dcd = bse.bsw.id, ddd = bse.bse.id;

			int fnwscore = Life.score(bbb, aaa + aba + aca + baa + bba + bca + caa + cba + cca + aab + abb + acb + bab + bcb + cab + cbb + ccb + aac + abc + acc + bac + bbc + bcc + cac + cbc + ccc);
			int fnescore = Life.score(bcb, aba + aca + ada + bba + bca + bda + cba + cca + cda + abb + acb + adb + bbb + bdb + cbb + ccb + cdb + abc + acc + adc + bbc + bcc + bdc + cbc + ccc + cdc);
			int fswscore = Life.score(cbb, baa + bba + bca + caa + cba + cca + daa + dba + dca + bab + bbb + bcb + cab + ccb + dab + dbb + dcb + bac + bbc + bcc + cac + cbc + ccc + dac + dbc + dcc);
			int fsescore = Life.score(ccb, bba + bca + bda + cba + cca + cda + dba + dca + dda + bbb + bcb + bdb + cbb + cdb + dbb + dcb + ddb + bbc + bcc + bdc + cbc + ccc + cdc + dbc + dcc + ddc);

			int bnwscore = Life.score(bbc, aab + abb + acb + bab + bbb + bcb + cab + cbb + ccb + aac + abc + acc + bac + bcc + cac + cbc + ccc + aad + abd + acd + bad + bbd + bcd + cad + cbd + ccd);
			int bnescore = Life.score(bcc, abb + acb + adb + bbb + bcb + bdb + cbb + ccb + cdb + abc + acc + adc + bbc + bdc + cbc + ccc + cdc + abd + acd + add + bbd + bcd + bdd + cbd + ccd + cdd);
			int bswscore = Life.score(cbc, bab + bbb + bcb + cab + cbb + ccb + dab + dbb + dcb + bac + bbc + bcc + cac + ccc + dac + dbc + dcc + bad + bbd + bcd + cad + cbd + ccd + dad + dbd + dcd);
			int bsescore = Life.score(ccc, bbb + bcb + bdb + cbb + ccb + cdb + dbb + dcb + ddb + bbc + bcc + bdc + cbc + cdc + dbc + dcc + ddc + bbd + bcd + bdd + cbd + ccd + cdd + dbd + dcd + ddd);

			result = world.memo.get(new MemoKey(fnwscore, fnescore, fswscore, fsescore, bnwscore, bnescore, bswscore, bsescore));
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

			LifeNode n000 = subQuad(0).nextCenter(step1);
			LifeNode n010 = subQuad(1).nextCenter(step1);
			LifeNode n020 = subQuad(2).nextCenter(step1);
			LifeNode n100 = subQuad(3).nextCenter(step1);
			LifeNode n110 = subQuad(4).nextCenter(step1);
			LifeNode n120 = subQuad(5).nextCenter(step1);
			LifeNode n200 = subQuad(6).nextCenter(step1);
			LifeNode n210 = subQuad(7).nextCenter(step1);
			LifeNode n220 = subQuad(8).nextCenter(step1);

			LifeNode n001 = subQuad(9).nextCenter(step1);
			LifeNode n011 = subQuad(10).nextCenter(step1);
			LifeNode n021 = subQuad(11).nextCenter(step1);
			LifeNode n101 = subQuad(12).nextCenter(step1);
			LifeNode n111 = subQuad(13).nextCenter(step1);
			LifeNode n121 = subQuad(14).nextCenter(step1);
			LifeNode n201 = subQuad(15).nextCenter(step1);
			LifeNode n211 = subQuad(16).nextCenter(step1);
			LifeNode n221 = subQuad(17).nextCenter(step1);

			LifeNode n002 = subQuad(18).nextCenter(step1);
			LifeNode n012 = subQuad(19).nextCenter(step1);
			LifeNode n022 = subQuad(20).nextCenter(step1);
			LifeNode n102 = subQuad(21).nextCenter(step1);
			LifeNode n112 = subQuad(22).nextCenter(step1);
			LifeNode n122 = subQuad(23).nextCenter(step1);
			LifeNode n202 = subQuad(24).nextCenter(step1);
			LifeNode n212 = subQuad(25).nextCenter(step1);
			LifeNode n222 = subQuad(26).nextCenter(step1);

			result = world.getNode(
				world.getNode(n000, n010, n100, n110, n001, n011, n101, n111).nextCenter(step2),
				world.getNode(n010, n020, n110, n120, n011, n021, n111, n121).nextCenter(step2),
				world.getNode(n100, n110, n200, n210, n101, n111, n201, n211).nextCenter(step2),
				world.getNode(n110, n120, n210, n220, n111, n121, n211, n221).nextCenter(step2),
				world.getNode(n001, n011, n101, n111, n002, n012, n102, n112).nextCenter(step2),
				world.getNode(n011, n021, n111, n121, n012, n022, n112, n122).nextCenter(step2),
				world.getNode(n101, n111, n201, n211, n102, n112, n202, n212).nextCenter(step2),
				world.getNode(n111, n121, n211, n221, n112, n122, n212, n222).nextCenter(step2)
			);
		}

		cache.put(steps, result);
		return result;
	}

	private LifeNode center() {
		LifeNode result = cache.get(0);
		if(result != null) {
			return result;
		}

		result = world.getNode(
			fnw.bse,
			fne.bsw,
			fsw.bne,
			fse.bnw,
			bnw.fse,
			bne.fsw,
			bsw.fne,
			bse.fnw
		);
		cache.put(0, result);
		return result;
	}

	protected LifeNode subQuad(int i) {
		switch(i) {
			// front
			case 0: return fnw;
			case 1: return world.getNode(fnw.fne, fne.fnw, fnw.fse, fne.fsw, fnw.bne, fne.bnw, fnw.bse, fne.bsw);
			case 2: return fne;

			case 3: return world.getNode(fnw.fsw, fnw.fse, fsw.fnw, fsw.fne, fnw.bsw, fnw.bse, fsw.bnw, fsw.bne);
			case 4: return world.getNode(fnw.fse, fne.fsw, fsw.fne, fse.fnw, fnw.bse, fne.bsw, fsw.bne, fse.bnw);
			case 5: return world.getNode(fne.fsw, fne.fse, fse.fnw, fse.fne, fne.bsw, fne.bse, fse.bnw, fse.bne);

			case 6: return fsw;
			case 7: return world.getNode(fsw.fne, fse.fnw, fsw.fse, fse.fsw, fsw.bne, fse.bnw, fsw.bse, fse.bsw);
			case 8: return fse;

			// middle
			case 9: return world.getNode(fnw.bnw, fnw.bne, fnw.bsw, fnw.bse, bnw.fnw, bnw.fne, bnw.fsw, bnw.fse);
			case 10: return world.getNode(fnw.bne, fne.bnw, fnw.bse, fne.bsw, bnw.fne, bne.fnw, bnw.fse, bne.fsw);
			case 11: return world.getNode(fne.bnw, fne.bne, fne.bsw, fne.bse, bne.fnw, bne.fne, bne.fsw, bne.fse);

			case 12: return world.getNode(fnw.bsw, fnw.bse, fsw.bnw, fsw.bne, bnw.fsw, bnw.fse, bsw.fnw, bsw.fne);
			case 13: return center();
			case 14: return world.getNode(fne.bsw, fne.bse, fse.bnw, fse.bne, bne.fsw, bne.fse, bse.fnw, bse.fne);

			case 15: return world.getNode(fsw.bnw, fsw.bne, fsw.bsw, fsw.bse, bsw.fnw, bsw.fne, bsw.fsw, bsw.fse);
			case 16: return world.getNode(fsw.bne, fse.bnw, fsw.bse, fse.bsw, bsw.fne, bse.fnw, bsw.fse, bse.fsw);
			case 17: return world.getNode(fse.bnw, fse.bne, fse.bsw, fse.bse, bse.fnw, bse.fne, bse.fsw, bse.fse);

			// back
			case 18: return bnw;
			case 19: return world.getNode(bnw.fne, bne.fnw, bnw.fse, bne.fsw, bnw.bne, bne.bnw, bnw.bse, bne.bsw);
			case 20: return bne;

			case 21: return world.getNode(bnw.fsw, bnw.fse, bsw.fnw, bsw.fne, bnw.bsw, bnw.bse, bsw.bnw, bsw.bne);
			case 22: return world.getNode(bnw.fse, bne.fsw, bsw.fne, bse.fnw, bnw.bse, bne.bsw, bsw.bne, bse.bnw);
			case 23: return world.getNode(bne.fsw, bne.fse, bse.fnw, bse.fne, bne.bsw, bne.bse, bse.bnw, bse.bne);

			case 24: return bsw;
			case 25: return world.getNode(bsw.fne, bse.fnw, bsw.fse, bse.fsw, bsw.bne, bse.bnw, bsw.bse, bse.bsw);
			case 26: return bse;

			default: return null; // this should never happen, right?
		}
	}

	protected int width() {
		return 1 << level;
	}

	protected int genSteps() {
		return 1 << (level - 2);
	}

}
