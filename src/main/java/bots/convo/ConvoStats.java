package bots.convo;

public class ConvoStats {
	final long channel;
	int sentence = 0;

	public ConvoStats(int sentence, long channel) {
		this.sentence = sentence;
		this.channel = channel;
	}
}
