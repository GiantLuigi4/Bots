package bots.convo;

import java.util.List;

public class ConvoStats {
	final long channel;
	int sentence = 0;
<<<<<<< Updated upstream
	final List<String> users;
	int maxUsers = 1;
	
	public ConvoStats(int sentence, long channel, List<String> users) {
		this.sentence = sentence;
		this.channel = channel;
		this.users = users;
		maxUsers = Math.max(maxUsers, users.size() + 1);
	}
	
	public void addUser(String user) {
		users.add(user);
		maxUsers = Math.max(maxUsers, users.size() + 1);
	}
	
	public void removeUser(String user) {
		users.remove(user);
=======

	public ConvoStats(int sentence, long channel) {
		this.sentence = sentence;
		this.channel = channel;
>>>>>>> Stashed changes
	}
}
