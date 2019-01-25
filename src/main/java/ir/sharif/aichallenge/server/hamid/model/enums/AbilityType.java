package ir.sharif.aichallenge.server.hamid.model.enums;

public enum AbilityType {
	DODGE(2), HEAL(1), ATTACK(3), FORTIFY(0);
	private int priority;

	public int getPriority() {
		return priority;
	}

	AbilityType(int priority) {
		this.priority = priority;
	}
}
