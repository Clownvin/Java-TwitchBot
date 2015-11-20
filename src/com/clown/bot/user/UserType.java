package com.clown.bot.user;

public enum UserType {
	MODERATOR, STAFF, ADMIN, GLOBAL_MOD, VIEWER;

	public static UserType getTypeForState(int state) {
		switch (state) {
		case 1:
			return MODERATOR;
		case 2:
			return STAFF;
		case 3:
			return ADMIN;
		case 4:
			return GLOBAL_MOD;
		case 5:
		default:
			return VIEWER;
		}
	}
}
