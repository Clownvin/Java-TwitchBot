package com.github.clownvin.jtwitchbot.modules;

import java.io.Serializable;
import java.util.ArrayList;

import com.github.clownvin.jtwitchbot.Main;
import com.github.clownvin.jtwitchbot.commands.Command;
import com.github.clownvin.jtwitchbot.messaging.Message;
import com.github.clownvin.jtwitchbot.user.User;
import com.github.clownvin.jtwitchbot.user.UserType;

public class TicTacToeModule extends Module {
    /**
     * 
     */
    private static final long serialVersionUID = 6944658151241665034L;
    private static final long INVITE_DELAY = 60000; // 1 minute.
    private static final long TURN_DELAY = 120000; // 2 minutes.

    private final class GameInvite implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2873129127685740473L;
	private final User sender;
	private final User reciever;
	private long delayTimer;
	private boolean senderTurn = false;
	private char[][] board = { { '0', '1', '2' }, { '3', '4', '5' }, { '6', '7', '8' } };
	private char senderPiece = 'X';
	private char recieverPiece = 'O';
	private int moveCount = 0; // at 9 game is over. Or earlier if a match is met.

	public GameInvite(User sender, User reciever) {
	    this.sender = sender;
	    this.reciever = reciever;
	    delayTimer = System.currentTimeMillis() + INVITE_DELAY;
	}

	public boolean expired() {
	    return System.currentTimeMillis() - delayTimer > 0;
	}

	public User getSender() {
	    return sender;
	}

	public User getReciever() {
	    return reciever;
	}

	private char getWinner() {
	    for (int y = 0; y < 3; y++) {
		if (board[y][0] == board[y][1] && board[y][1] == board[y][2]) {
		    return board[y][0];
		}
	    }
	    for (int x = 0; x < 3; x++) {
		if (board[0][x] == board[1][x] && board[1][x] == board[2][x]) {
		    return board[0][x];
		}
	    }
	    boolean diag = (board[0][0] == board[1][1] && board[1][1] == board[2][2])
		    || (board[0][2] == board[1][1] && board[1][1] == board[2][0]);
	    return diag ? board[1][1] : '-';
	}

	private void sendBoard(User user) {
	    user.sendWhisper("|" + board[0][0] + "|" + board[0][1] + "|" + board[0][2] + "|");
	    user.sendWhisper("|" + board[1][0] + "|" + board[1][1] + "|" + board[1][2] + "|");
	    user.sendWhisper("|" + board[2][0] + "|" + board[2][1] + "|" + board[2][2] + "|");
	}

	private User getWaitingPlayer() {
	    if (!senderTurn) {
		return sender;
	    } else {
		return reciever;
	    }
	}

	public void makeMove(User user, int move) {
	    if ((user.equals(sender) && !senderTurn) || (user.equals(reciever) && senderTurn)) {
		user.sendWhisper("It's not your turn yet!");
	    }
	    int x = move % 3;
	    int y = move / 3;
	    if (board[x][y] == senderPiece || board[x][y] == recieverPiece) {
		user.sendWhisper("That spot already has a piece in it.");
		return;
	    }
	    moveCount++;
	    board[x][y] = senderTurn ? senderPiece : recieverPiece;
	    char winner = getWinner();
	    if (winner != '-') { //Most recent move caused win, so most recent player won.
		user.sendWhisper("You've won!");
		User other = getWaitingPlayer();
		other.sendWhisper("You lost!");
		user.getBot().getChannelManager().getChannel(user.getChannel())
			.sendMessage(user.getUsername() + " beat " + other.getUsername() + " in a game of TicTacToe!");
		return;
	    }
	    if (moveCount == 9) {
		sender.sendWhisper("Game ended in a draw.");
		reciever.sendWhisper("Game ended in a draw.");
		return;
	    }
	    delayTimer = System.currentTimeMillis() + TURN_DELAY;
	    senderTurn = !senderTurn;
	    if (senderTurn) {
		sender.sendWhisper("It's now your turn.");
		sendBoard(sender);
	    } else {
		reciever.sendWhisper("It's now your turn.");
		sendBoard(reciever);
	    }
	}

	//Call on accepting an invitation.
	public void accept() {
	    delayTimer = System.currentTimeMillis() + TURN_DELAY;
	    senderTurn = Math.random() * 10 > 5.0 ? true : false;
	    User current = senderTurn ? sender : reciever;
	    User other = senderTurn ? reciever : sender;
	    current.sendWhisper("You get to make the first move. Use the command !move <num> to make your move.");
	    sendBoard(current);
	    other.sendWhisper(
		    "You are going second. When it is your turn, use the command !move <num> to make your move.");
	}
    }

    private transient Thread inviteCuller;

    private transient ArrayList<GameInvite> invites;

    private transient Command[] commandList;

    //Moving piece in game
    private void handleMove(User user, String[] args, String message) {
	for (GameInvite invite : invites) {
	    if (!invite.getReciever().equals(user) && !invite.getSender().equals(user)) {
		continue;
	    }
	    if (args.length == 0) {
		user.sendWhisper("You need to specify a number along with the move. !move <num>");
		return;
	    }
	    int moveNum = Integer.parseInt(args[0]);
	    invite.makeMove(user, moveNum);
	}
	user.sendWhisper("You aren't currently in any match.");
    }

    //Creating new invite
    // syntax: !invite user
    private void handleInvite(User user, String[] args, String message) {
	if (args.length == 0) {
	    user.sendWhisper("You must include a user to send the invite to. (!invite <user>)");
	    return;
	}
	for (GameInvite invite : invites) {
	    if (invite.getSender().equals(user) || invite.getReciever().equals(user)) {
		user.sendWhisper("You can't create an invitation when you already have one pending.");
		return;
	    }
	    if (invite.getSender().getUsername().equalsIgnoreCase(args[0])
		    || invite.getReciever().getUsername().equalsIgnoreCase(args[0])) {
		user.sendWhisper("That user already has a pending game invite.");
		return;
	    }
	}
	// Clear to create a new invitation.
	User other = user.getBot().getChannelManager().getChannel(user.getChannel()).getUser(args[0]);
	if (other == null) {
	    user.sendWhisper("Failed to find that user in my list. Try again in 30 seconds.");
	    return;
	}
	invites.add(new GameInvite(user, other));
	user.sendWhisper("Sending invitation...");
	other.sendWhisper(user.getUsername() + " would like to play TicTacToe with you. !accept or !decline");
    }

    //Accept invite
    private void handleAccept(User user, String[] args, String message) {
	for (GameInvite invite : invites) {
	    if (invite.getSender().equals(user)) {
		user.sendWhisper("You can't accept your own invitation.");
		return;
	    }
	    if (invite.getReciever().equals(user)) {
		invite.getSender().sendWhisper("Your invitation has been accepted.");
		invite.accept();
		return;
	    }
	}
	user.sendWhisper("You don't currently have any invitation to accept.");
    }

    //Decline invite
    public void handleDecline(User user, String[] args, String message) {
	for (GameInvite invite : invites) {
	    if (invite.getReciever().equals(user)) {
		invite.getSender().sendWhisper("Your invitation has been declined.");
		user.sendWhisper("Invitation declined.");
		invites.remove(invite);
		return;
	    }
	    if (invite.getSender().equals(user)) {
		invite.getReciever().sendWhisper("Other user revoked invitation.");
		user.sendWhisper("Invite cancelled.");
		invites.remove(invite);
		return;
	    }
	}
	user.sendWhisper("You don't currently have any pending invitations.");
    }

    @Override
    public boolean onCommand(User user, String command, String[] args) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean onJoin(User user) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean onLeave(User user) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void onLoad() {
	invites = new ArrayList<GameInvite>();
	inviteCuller = new Thread() {
	    @Override
	    public void run() {
		while (!Main.killIssued()) {
		    try {
			sleep(1000); // 1 second.
		    } catch (InterruptedException e) {
		    }
		    for (int i = 0; i < invites.size(); i++) {
			if (invites.get(i).expired()) {
			    invites.get(i).getSender().sendWhisper("Your invitation has expired.");
			    invites.get(i).getReciever().sendWhisper("Your current invitation has expired.");
			    invites.remove(i--); // Decrement back down, because there's a new invitation in i			
			}
		    }
		}
	    }
	};
	commandList = new Command[] {
		new Command("!move", "Use this to make a move during a game of TicTacToe. Usage: !move <num>") {

		    /**
		     * 
		     */
		    private static final long serialVersionUID = -5121543324640291604L;

		    @Override
		    public UserType getUserType() {
			return UserType.VIEWER;
		    }

		    @Override
		    public void handleCommand(User user, String[] args, String message) {
			handleMove(user, args, message);
		    }

		},

		new Command("!invite", "Use this to invite a player to a game of TicTacToe. Usage: !invite <user>") {

		    /**
		     * 
		     */
		    private static final long serialVersionUID = 346578323567779905L;

		    @Override
		    public UserType getUserType() {
			return UserType.VIEWER;
		    }

		    @Override
		    public void handleCommand(User user, String[] args, String message) {
			handleInvite(user, args, message);
		    }

		},

		new Command("!accept", "Use this to accept an invitation to a game. Usage: !accept") {

		    /**
		     * 
		     */
		    private static final long serialVersionUID = 1444863092377627391L;

		    @Override
		    public UserType getUserType() {
			return UserType.VIEWER;
		    }

		    @Override
		    public void handleCommand(User user, String[] args, String message) {
			handleAccept(user, args, message);
		    }

		},

		new Command("!decline", "Use this to decline and invitation to a game. Usage: !decline") {

		    /**
		     * 
		     */
		    private static final long serialVersionUID = 6195155222707004669L;

		    @Override
		    public UserType getUserType() {
			return UserType.VIEWER;
		    }

		    @Override
		    public void handleCommand(User user, String[] args, String message) {

		    }

		} };
	inviteCuller.start();
    }

    @Override
    public boolean onMessage(Message message) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean onWhisper(Message message) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public String getModuleName() {
	return "TicTacToeModule";
    }

    @Override
    public Command[] getModuleCommands() {
	return commandList;
    }

}
