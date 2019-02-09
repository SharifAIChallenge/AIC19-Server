package ir.sharif.aichallenge.server.thefinalbattle.model.enums;

public enum AbilityType {
	DODGE(2), DEFENSIVE(1), OFFENSIVE(3), FORTIFY(0);
	private int priority;

	public int getPriority() {
		return priority;
	}

	AbilityType(int priority) {
		this.priority = priority;
	}
}
