package com.github.doughsay.thrashlife;

public class Life {
	public static int score(int center, int surround) {
		if(surround == 6 || (center == 1 && (surround == 5 || surround == 7))) {
			return 1;
		}
		return 0;
	}
}
