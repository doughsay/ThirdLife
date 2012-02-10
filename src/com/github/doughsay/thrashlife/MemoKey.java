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
		return new int[] {this.nwId, this.neId, this.swId, this.seId};
	}

	@Override
	public String toString() {
		return this.nwId+","+this.neId+","+this.swId+","+this.seId;
	}
	
	@Override
	public boolean equals(Object obj) {
		MemoKey other = (MemoKey)obj;
		return other.nwId == this.nwId && other.neId == this.neId && other.swId == this.swId && other.seId == this.seId;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}