package jjoller.foxinabox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of a texas hold'em game.
 */
public abstract class TexasHand {

	protected static final Phase[] PHASES = { Phase.FLOP, Phase.TURN,
			Phase.RIVER };
	protected static final String EMPTY = "-";
	protected static final String SEPARATOR = "_";
	protected static final String _REPLACEMENT = "\\.\\*\\.";

	public TexasHand(Dealer dealer, List<Player> players) {
		assert dealer != null;
		assert players.size() > 1 : "A poker game consists of at least 2 players";

		dealer.reset();
		this.dealer = dealer;
		this.players = players;
		this.activePlayers = new LinkedList<>();
		activePlayers.addAll(players);
		this.actions = new ArrayList<>(this.players.size() * 8);
		flop = Optional.empty();
		turn = river = Optional.empty();
		paid = new int[players.size()];
		phase = Phase.PRE_FLOP;
		toPay = 0;
		toPayPreviousPhase = 0;
		minPlayerActions = this.players.size();
	}

	/**
	 * Copy constructor
	 * @param o
	 * 	to copy
	 * @return
	 *  a deep copy of this hand
     */
	protected TexasHand(TexasHand o){
		this.dealer = o.dealer;
		this.players = o.players;
		this.actions = new ArrayList<>(this.players.size()*8);
		this.actions.addAll(o.actions);
		this.activePlayers = new LinkedList<>();
		this.activePlayers.addAll(o.activePlayers);
		this.flop = o.flop;
		this.turn = o.turn;
		this.river = o.river;
		this.minPlayerActions = o.minPlayerActions;
		this.onTurn = o.onTurn;
		this.paid = new int[players.size()];
		System.arraycopy(o.paid,0,this.paid,0,this.paid.length);
		this.onTurnIterator = this.activePlayers.listIterator(o.onTurnIterator.nextIndex());
	}

	protected final Dealer dealer;
	protected final List<Player> players;
	protected final LinkedList<Player> activePlayers;
	protected final List<Integer> actions;
	protected Optional<EnumSet<Card>> flop;
	protected Optional<Card> turn;
	protected Optional<Card> river;

	// amount each player paid
	protected final int[] paid;

	// preflop, flop, turn or river
	protected Phase phase;

	// amount which a player has to pay to stay active
	protected int toPay;

	// toPay at the end of the previous phase
	protected int toPayPreviousPhase;

	// Number of minimal required player actions which are required to complete
	// the phase.
	protected int minPlayerActions;

	// iterate over the active players
	protected ListIterator<Player> onTurnIterator;

	// player currently on turn
	protected Optional<Player> onTurn;

	public abstract TexasHand clone();

	public TexasHand playHand() {

		// deal cards
		players.forEach(dealer::dealCards);

		// play blinds
		performPlayerAction(players.get(0), dealer.smallBlind());
		performPlayerAction(players.get(1), dealer.bigBlind());

		onTurnIterator = this.activePlayers
				.listIterator(activePlayers.size() > 2 ? 2 : 0);

		for (Phase phase : PHASES) {
			performPhase();
			if (this.activePlayers.size() < 2) {
				return payOutPot();
			} else {
				actions.add(phase.getAction());
				dealer.dealTableCards(this, phase);
				this.toPayPreviousPhase = this.toPay;
			}
			this.phase = phase;
		}
		performPhase();

		// next hand
		return payOutPot();
	}

	private void performPhase() {
		minPlayerActions = this.activePlayers.size();
		onTurnIterator = this.activePlayers.listIterator(0);
		moveToNextPlayer();
		do {
			Player player = onTurn.get();
			int action = player.getModel().action(this);
			performPlayerAction(player, action);
			moveToNextPlayer();
		} while (onTurn.isPresent());
	}

	private void performPlayerAction(Player player, int action) {

		minPlayerActions--;
		actions.add(action);
		int seat = seat(player);
		if (action == Action.FOLD) {
			onTurnIterator.remove();
		} else {
			paid[seat] += action;
			if (paid[seat] >= player.getStack())
				// is all-in
				onTurnIterator.remove();
		}

		toPay = Math.max(toPay, paid[seat]);
	}

	public int toPay() {
		return toPay;
	}

	public int toPayPreviousPhase() {
		return toPayPreviousPhase;
	}

	private TexasHand payOutPot() {
		assert this.isComplete() : "Hand must be complete when paying out the pot";

		Collection<Player> winners;
		FiveCardValue bestVal = null;

		if (this.activePlayers.size() > 1) {
			winners = new HashSet<>();
			for (Player player : this.activePlayers) {
				EnumSet<Card> cards = player.holdings().get();
				if (flop.isPresent()) {
					cards.addAll(this.flop.get());
					if (turn.isPresent()) {
						cards.add(turn.get());
						if (river.isPresent())
							cards.add(river.get());
					}
				}
				FiveCardValue val = new FiveCardValue(cards);
				if (bestVal == null || val.isBetterThan(bestVal)) {
					winners.clear();
					winners.add(player);
					bestVal = val;
				} else if (val.isEqual(bestVal)) {
					winners.add(player);
				}
			}
		} else {
			winners = this.activePlayers;
		}

		int pot = this.pot();
		for (Player player : players) {
			int newStack = player.getStack() - this.paid(player);
			if (winners.contains(player))
				newStack += pot / winners.size();
			player.setStack(newStack);
		}

		// move dealer button
		Player smallBlind = players.remove(0);
		players.add(smallBlind);
		return follower(dealer, players);
	}

	protected abstract TexasHand follower(Dealer dealer, List<Player> players);

	/**
	 * All the player except 1 folded or showdown
	 * 
	 * @return true if the hand is
	 */
	public boolean isComplete() {

		return this.activePlayers.size() < 2 || phase() == Phase.RIVER
				&& !playerOnTurn().isPresent();
	}

	public Optional<Player> playerOnTurn() {
		return this.onTurn;
	}

	/**
	 * Return the player which is on turn. Return empty if there is no player on
	 * turn, which means there is either a dealer action or the hand is
	 * complete.
	 *
	 */
	private void moveToNextPlayer() {

		if (this.activePlayers.size() > 1) {
			if (!this.onTurnIterator.hasNext())
				this.onTurnIterator = this.activePlayers.listIterator();
			Player player = this.onTurnIterator.next();
			if (this.minPlayerActions > 0 || paid(player) < toPay())
				// the player still has to pay something to stay in the
				// game or he did not perform an action in this phase
				onTurn = Optional.of(player);
			else
				onTurn = Optional.empty();
		} else {
			onTurn = Optional.empty();
		}
	}

	public Phase phase() {
		return phase;
	}

	public int seat(Player player) {
		assert players.contains(player) : "Player not on table";

		return players.indexOf(player);
	}

	public int paid(Player player) {

		return paid[seat(player)];

		// return actions.stream().filter(a -> a.getParty() == player)
		// .filter(a -> a.getAction() > 0)
		// .collect(Collectors.summingInt(a -> a.getAction()));
	}

	public int pot() {
		return actions.stream().filter(a -> a > 0)
				.collect(Collectors.summingInt(a -> a));
	}

	public void setRiver(Card river) {
		this.river = Optional.of(river);
	}

	public void setTurn(Card turn) {
		this.turn = Optional.of(turn);
	}

	public void setFlop(EnumSet<Card> flop) {
		this.flop = Optional.of(flop);
	}

	public Optional<EnumSet<Card>> getFlop() {
		return flop;
	}

	public Optional<Card> getTurn() {
		return turn;
	}

	public Optional<Card> getRiver() {
		return river;
	}

	@Override
	public String toString() {

		// limiter
		String seperator = TexasHand.SEPARATOR;

		// write blinds
		String s = dealer.smallBlind() + seperator + dealer.bigBlind()
				+ seperator;

		// write table cards
		if (flop.isPresent()) {
			Iterator<Card> iter = flop.get().iterator();
			while (iter.hasNext())
				s += iter.next();
			if (this.getTurn().isPresent()) {
				s += this.getTurn().get();
				if (this.getRiver().isPresent())
					s += this.getRiver().get();
			}
		}

		// write players
		for (Player p : this.players) {

			String cards = EMPTY;

			Optional<EnumSet<Card>> holeCards = p.holdings();

			if (holeCards.isPresent()) {
				Iterator<Card> iter = holeCards.get().iterator();
				cards = iter.next() + "" + iter.next();
			}

			s += seperator + p.getName().replaceAll("_", _REPLACEMENT)
					+ seperator + p.getStack() + seperator + cards;
		}

		// write actions
		for (int a : this.actions)
			s += seperator + Action.toString(a);

		return s;
	}

	public abstract int callAmount();

	public abstract int raiseAmount();

	public String actionIdentifier() {

		String s = "";
		for(int a:this.actions)
			s += a+"_";

		return s;
	}

	public static class Player {

		public Player(String name, PlayerModel model, int stack) {
			assert name.length() > 0;
			assert model != null;
			assert stack > 1 : "Player must have some stack";

			this.name = name;
			this.model = model;
			this.stack = stack;
			cards = Optional.empty();
		}

		private final String name;
		private PlayerModel model;
		private Optional<EnumSet<Card>> cards;
		private int stack;

		void setStack(int newStack) {
			this.stack = newStack;
		}

		protected void dealCards(Dealer dealer) {
			dealer.dealCards(this);
		}

		public String getName() {
			return name;
		}

		public PlayerModel getModel() {
			return this.model;
		}

		public void setModel(PlayerModel model){this.model = model; }

		public int getStack() {
			return stack;
		}

		public Optional<EnumSet<Card>> holdings() {
			if (this.cards.isPresent())
				return Optional.of(this.cards.get().clone());
			else
				return this.cards;
		}

		void setCards(EnumSet<Card> cards) {
			assert cards.size() == 2;
			this.cards = Optional.of(cards);
		}

	}


}

enum Phase {

	PRE_FLOP(-2), FLOP(-3), TURN(-4), RIVER(-5);

	Phase(int action) {
		this.action = action;
	}

	private final int action;

	public int getAction() {
		return action;
	}
}
