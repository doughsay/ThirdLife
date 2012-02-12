package com.github.doughsay.thrashlife;

public final class MemoKey {
	public final int nwId, neId, swId, seId;
	
	public MemoKey(int nwId, int neId, int swId, int seId) {
		this.nwId = nwId;
		this.neId = neId;
		this.swId = swId;
		this.seId = seId;
	}
	
	public int[] toArray() {
		return new int[] {nwId, neId, swId, seId};
	}

	@Override
	public String toString() {
		return nwId+","+neId+","+swId+","+seId;
	}
	
	@Override
	public boolean equals(Object obj) {
		MemoKey other = (MemoKey)obj;
		return other.nwId == nwId && other.neId == neId && other.swId == swId && other.seId == seId;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}