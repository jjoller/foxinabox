package jjoller.foxinabox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Random;
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
		phase = Phase.PREFLOP;
		toPay = 0;
		toPayPreviousPhase = 0;
		minPlayerActions = this.players.size();
	}

	protected final Dealer dealer;
	protected final List<Player> players;
	protected final LinkedList<Player> activePlayers;
	private final List<Integer> actions;
	private Optional<EnumSet<Card>> flop;
	private Optional<Card> turn;
	private Optional<Card> river;

	// amount each player paid
	private final int[] paid;

	// preflop, flop, turn or river
	private Phase phase;

	// amount which a player has to pay to stay active
	private int toPay;

	// toPay at the end of the previous phase
	private int toPayPreviousPhase;

	// Number of minimal required player actions which are required to complete
	// the phase.
	private int minPlayerActions;

	// iterate over the active players
	private ListIterator<Player> onTurnIterator;

	// player currently on turn
	private Optional<Player> onTurn;

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

}

class Player {

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
	private final PlayerModel model;
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

class Dealer {

	public Dealer() {
		deck = new ArrayList<Card>(Card.values().length);
		deck.addAll(Arrays.asList(Card.values()));
		removed = new ArrayList<Card>(25);
		random = new Random();
	}

	private final List<Card> deck;
	private final List<Card> removed;
	private final Random random;

	public void reset() {
		deck.addAll(removed);
		removed.clear();
	}

	public void dealTableCards(TexasHand hand, Phase phase) {

		switch (phase) {
		case FLOP:
			EnumSet<Card> flop = EnumSet.of(removeRandomFromDeck(),
					removeRandomFromDeck(), removeRandomFromDeck());
			hand.setFlop(flop);
			break;
		case TURN:
			hand.setTurn(removeRandomFromDeck());
			break;
		case RIVER:
			hand.setRiver(removeRandomFromDeck());
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public void dealCards(Player player) {

		player.setCards(EnumSet.of(removeRandomFromDeck(),
				removeRandomFromDeck()));
	}

	private Card removeRandomFromDeck() {
		Card card = deck.remove(random.nextInt(deck.size()));
		removed.add(card);
		return card;
	}

	public int smallBlind() {
		return 1;
	}

	public int bigBlind() {
		return 2;
	}
}

interface PlayerModel {
	int action(TexasHand hand);
}

class Action {

	public static final int FOLD = -1;
	public static final int DEAL_PREFLOP = -2;
	public static final int DEAL_FLOP = -3;
	public static final int DEAL_TURN = -4;
	public static final int DEAL_RIVER = -5;

	public static String toString(int action) {

		switch (action) {
		case FOLD:
			return "f";
		case DEAL_PREFLOP:
			return "";
		case DEAL_FLOP:
			return "F";
		case DEAL_TURN:
			return "T";
		case DEAL_RIVER:
			return "R";
		default:
			Integer i = new Integer(action);
			return i.toString();
		}
	}
}

interface Party {
	public boolean isDealer();
}

enum Phase {
	PREFLOP(-2), FLOP(-3), TURN(-4), RIVER(-5);

	Phase(int action) {
		this.action = action;
	}

	private final int action;

	public int getAction() {
		return action;
	}
}
