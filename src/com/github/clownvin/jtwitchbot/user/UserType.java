package com.github.clownvin.jtwitchbot.user;

public enum UserType {
    MODERATOR(), STAFF(), ADMIN(), GLOBAL_MOD(), VIEWER();

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

    public boolean equivalentTo(UserType other) {
	switch (this) {
	case MODERATOR:
	    return true;
	case STAFF:
	    return other.equals(VIEWER);
	case ADMIN:
	    return true;
	case GLOBAL_MOD:
	    return other.equals(VIEWER);
	default:
	    return other.equals(VIEWER);
	}
    }
}
