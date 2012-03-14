package com.github.doughsay.thrashlife;

public final class MemoKey {
	public final int fnwId, fneId, fswId, fseId, bnwId, bneId, bswId, bseId;

	public MemoKey(int fnwId, int fneId, int fswId, int fseId, int bnwId, int bneId, int bswId, int bseId) {
		this.fnwId = fnwId;
		this.fneId = fneId;
		this.fswId = fswId;
		this.fseId = fseId;
		this.bnwId = bnwId;
		this.bneId = bneId;
		this.bswId = bswId;
		this.bseId = bseId;
	}

	protected int[] toArray() {
		return new int[] {fnwId, fneId, fswId, fseId, bnwId, bneId, bswId, bseId};
	}

	@Override
	public boolean equals(Object obj) {
		MemoKey other = (MemoKey)obj;
		return other.fnwId == fnwId && other.fneId == fneId && other.fswId == fswId && other.fseId == fseId &&
				other.bnwId == bnwId && other.bneId == bneId && other.bswId == bswId && other.bseId == bseId;
	}

	@Override
	public int hashCode() {
		return
			fnwId +
			11 * fneId +
			53 * fswId +
			101 * fseId +
			503 * bnwId +
			1009 * bneId +
			5003 * bswId +
			10007 * bseId;
	}
}