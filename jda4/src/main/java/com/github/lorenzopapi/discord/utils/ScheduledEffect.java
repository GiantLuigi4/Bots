package com.github.lorenzopapi.discord.utils;

import java.util.Objects;

public class ScheduledEffect {
	public boolean isForTheWorstApplied = false;
	public float volume = -1;
	public int byteSwap = -1;
	public int pseudoRetro = -1;
	public int bassBoost = -1;
	public float chance = -1;
	public int delay = 0;
	
	public ScheduledEffect() {
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ScheduledEffect effect = (ScheduledEffect) object;
		return isForTheWorstApplied == effect.isForTheWorstApplied &&
				Float.compare(effect.volume, volume) == 0 &&
				byteSwap == effect.byteSwap &&
				pseudoRetro == effect.pseudoRetro &&
				bassBoost == effect.bassBoost &&
				Float.compare(effect.chance, chance) == 0 &&
				delay == effect.delay;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(isForTheWorstApplied, volume, byteSwap, pseudoRetro, bassBoost, chance, delay);
	}
}
