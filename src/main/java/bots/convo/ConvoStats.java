package bots.convo;

import java.util.List;

public class ConvoStats {
	final long channel;
	int sentence = 0;
	final List<String> users;
	
	public ConvoStats(int sentence, long channel, List<String> users) {
		this.sentence = sentence;
		this.channel = channel;
		this.users = users;
	}
}
