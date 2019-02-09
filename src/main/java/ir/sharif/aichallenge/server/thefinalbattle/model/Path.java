package ir.sharif.aichallenge.server.thefinalbattle.model;

import ir.sharif.aichallenge.server.thefinalbattle.model.enums.Direction;

public class Path {
	private Cell begin;
	private Cell end;
	private Direction direction;

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
