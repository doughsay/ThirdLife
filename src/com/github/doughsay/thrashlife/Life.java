package com.github.doughsay.thrashlife;

public class Life {
	public static int score(int center, int surround) {
		if(surround == 3 || (surround == 2 && center == 1)) {
			return 1;
		}
		return 0;
	}
}
