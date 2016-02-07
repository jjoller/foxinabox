package jjoller.foxinabox;

import java.util.Arrays;
import java.util.EnumSet;

import jjoller.foxinabox.Card.CardValue;

public class FiveCardValue implements Comparable<FiveCardValue> {

	private static final HandValueRanking RANKING = new HandValueRanking();

	/**
	 * Try all combinations of five cards and find out which has the highest
	 * value.
	 * 
	 * @param hand
	 *            A Hand of at least five cards.
	 * 
	 * @param flush
	 *            If set to false do not look at the suit of the cards.
	 * 
	 */
	public FiveCardValue(EnumSet<Card> hand, boolean flush) {

		// precondition
		if (hand.size() < 5)
			throw new IllegalArgumentException(
					"the Hand contains less than five Cards.");

		int best = 0;

		int numCards = hand.size();
		Card[] cards = new Card[numCards];
		cards = hand.toArray(cards);

		// array elements are sorted by index, starting with the lowest index.
		// Card[] cards = hand.toArray(new Card[numCards]);

		for (int p = 0; p < numCards - 4; p++) {
			for (int o = p + 1; o < numCards - 3; o++) {
				for (int k = o + 1; k < numCards - 2; k++) {
					for (int e = k + 1; e < numCards - 1; e++) {
						for (int r = e + 1; r < numCards; r++) {

							Card c0 = cards[p];
							Card c1 = cards[o];
							Card c2 = cards[k];
							Card c3 = cards[e];
							Card c4 = cards[r];

							int v0, v1, v2, v3, v4;
							v0 = c0.getValue().ordinal();
							v1 = c1.getValue().ordinal();
							v2 = c2.getValue().ordinal();
							v3 = c3.getValue().ordinal();
							v4 = c4.getValue().ordinal();

							int rank = RANKING.rank[v0][v1][v2][v3][v4];

							if (flush) {

								if (isFlush(c0, c1, c2, c3, c4)) {
									if (RANKING.isStraight(v0, v1, v2, v3, v4)) {
										// straight flush
										rank = (rank - RANKING.straightRank)
												+ RANKING.straightFlushRank;
									} else {

										// normal flush
										rank = RANKING.flushRank
												+ (rank - RANKING.minRank);
									}
								}
							}

							if (rank > best) {
								best = rank;
								fiveCard = EnumSet.of(c0, c1, c2, c3, c4);
							}
						}
					}
				}
			}
		}

		this.rank = best;
	}

	public FiveCardValue(EnumSet<Card> hand) {
		this(hand, true);
	}

	public int getRank(EnumSet<Card> cards) {
		return this.rank;
	}

	// private EnumSet<Card> hand;
	private EnumSet<Card> fiveCard;
	private int rank;

	public int getRank() {
		return rank;
	}

	public HandValueType getHandValue() {
		Card[] c = new Card[5];
		c = fiveCard.toArray(c);
		return getHandValueType(c[0], c[1], c[2], c[3], c[4]);
	}

	private static HandValueType getHandValueType(Card c0, Card c1, Card c2,
			Card c3, Card c4) {

		int p, o, k, e, r;
		p = c0.getValue().ordinal();
		o = c1.getValue().ordinal();
		k = c2.getValue().ordinal();
		e = c3.getValue().ordinal();
		r = c4.getValue().ordinal();

		boolean flush = isFlush(c0, c1, c2, c3, c4);
		boolean isStraight = RANKING.isStraight(p, o, k, e, r);

		if (flush && isStraight) {
			return HandValueType.STRAIGHT_FLUSH;
		}
		if (RANKING.isFourOfAKind(p, o, k, e, r)) {
			return HandValueType.FOUR_OF_A_KIND;
		}
		if (RANKING.isFullHouse(p, o, k, e, r)) {
			return HandValueType.FULL_HOUSE;
		}
		if (flush) {
			return HandValueType.FLUSH;
		}
		if (isStraight) {
			return HandValueType.STRAIGHT;
		}
		if (RANKING.isThreeOfAKind(p, o, k, e, r)) {
			return HandValueType.THREE_OF_A_KIND;
		}
		if (RANKING.isTwoPair(p, o, k, e, r)) {
			return HandValueType.TWO_PAIR;
		}
		if (RANKING.isPair(p, o, k, e, r)) {
			return HandValueType.PAIR;
		}
		return HandValueType.HIGH_CARD;
	}

	private static boolean isFlush(Card c1, Card c2, Card c3, Card c4, Card c5) {
		return c1.getSuit() == c2.getSuit() && c2.getSuit() == c3.getSuit()
				&& c3.getSuit() == c4.getSuit() && c4.getSuit() == c5.getSuit();
	}

	/**
	 * returns a positive number if this Value is better than the given
	 * FiveCardValue. Returns 0 if the given value is equal this value.
	 */
	@Override
	public int compareTo(FiveCardValue o) {
		return rank - o.rank;
	}

	public boolean isBetterThan(FiveCardValue o) {
		return rank > o.rank;
	}

	public boolean isEqual(FiveCardValue o) {
		return rank == o.rank;
	}

	public EnumSet<Card> getHand() {
		return fiveCard;
	}

	public String toString() {

		String s = "";

		HandValueType v = getHandValue();

		CardValue[] fiveCardValues = new CardValue[5];
		int i = 0;
		for (Card c : fiveCard) {
			fiveCardValues[i++] = c.getValue();
		}

		s += v;

		switch (v) {

		case HIGH_CARD:
			s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " "
					+ fiveCardValues[2] + " " + fiveCardValues[3] + " "
					+ fiveCardValues[4];
			break;

		case PAIR:
			s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " "
					+ fiveCardValues[2] + " " + fiveCardValues[3] + " "
					+ fiveCardValues[4];
			break;

		case TWO_PAIR:
			s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " "
					+ fiveCardValues[2] + " " + fiveCardValues[3] + " "
					+ fiveCardValues[4];
			break;

		case THREE_OF_A_KIND:
			s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " "
					+ fiveCardValues[2] + " " + fiveCardValues[3] + " "
					+ fiveCardValues[4];
			break;

		case STRAIGHT:
			if (fiveCardValues[0] == CardValue.ACE
					&& fiveCardValues[1] == CardValue.FIVE) {
				s += " " + fiveCardValues[1] + " high";
			} else {
				s += " " + fiveCardValues[0] + " high";
			}
			break;

		case FLUSH:
			s += " " + fiveCardValues[0] + " " + fiveCardValues[1] + " "
					+ fiveCardValues[2] + " " + fiveCardValues[3] + " "
					+ fiveCardValues[4];
			break;

		case FULL_HOUSE:

			if (fiveCardValues[0] == fiveCardValues[2]) {
				s += " " + fiveCardValues[0] + "s, full of "
						+ fiveCardValues[3] + "s";
			} else {
				s += " " + fiveCardValues[3] + "s, full of "
						+ fiveCardValues[0] + "s";
			}

			break;

		case FOUR_OF_A_KIND:

			if (fiveCardValues[0] == fiveCardValues[1]) {
				s += " " + fiveCardValues[0] + "s, " + fiveCardValues[4]
						+ " kicker";
			} else {
				s += " " + fiveCardValues[1] + "s, " + fiveCardValues[0]
						+ " kicker";
			}
			break;

		case STRAIGHT_FLUSH:

			if (fiveCardValues[0] == CardValue.ACE
					&& fiveCardValues[1] == CardValue.FIVE) {
				s += " " + fiveCardValues[1] + " high";
			} else {
				s += " " + fiveCardValues[0] + " high";
			}
			break;
		}

		return s;
	}

	static class HandValueRanking {

		protected HandValueRanking() {

			count = Integer.MAX_VALUE / 2;

			maxRank = count + 9; // the number of different straight flushs

			straightFlushRank = count;

			putFourOfAKind();

			fourOfAKindRank = count;

			putFullHouse();

			fullHouseRank = count;

			count -= 13 * 12 * 11 * 10 * 9;

			flushRank = count;
			count--;

			putStraight();

			straightRank = count;

			putThreeOfAKind();
			putTwoPair();
			putPair();
			putHighCard();
			minRank = count;

		}

		private int[][][][][] rank = new int[13][13][13][13][13];

		private int count;
		public int flushRank;
		public int straightRank;
		public int fullHouseRank;
		public int fourOfAKindRank;
		public int straightFlushRank;
		public int maxRank;
		public int minRank;

		// private void fill() {
		//
		//
		//
		// }

		private void putFourOfAKind() {
			for (int p = 0; p < 13; p++) {
				for (int o = 0; o < 13; o++) {
					if (areDifferent(p, o)) {
						put(p, p, p, p, o, count--);
					}
				}
			}
		}

		private void putFullHouse() {
			for (int p = 0; p < 13; p++) {
				for (int o = 0; o < 13; o++) {
					if (areDifferent(p, o)) {
						put(p, p, p, o, o, count--);
					}
				}
			}
		}

		private void putStraight() {
			// straight five high
			for (int p = 0; p < 9; p++) {
				rank[p][p + 1][p + 2][p + 3][p + 4] = count--;
			}
			rank[0][9][10][11][12] = count--;
		}

		private void putThreeOfAKind() {
			for (int p = 0; p < 13; p++) {
				for (int o = 0; o < 13; o++) {
					for (int k = o; k < 13; k++) {
						if (areDifferent(p, o, k)) {
							put(p, p, p, o, k, count--);
						}
					}
				}
			}
		}

		private void putTwoPair() {
			for (int p = 0; p < 13; p++) {
				for (int o = p; o < 13; o++) {
					for (int k = 0; k < 13; k++) {
						if (areDifferent(p, o, k)) {
							put(p, p, o, o, k, count--);
						}
					}
				}
			}
		}

		private void putPair() {
			for (int p = 0; p < 13; p++) {
				for (int o = 0; o < 13; o++) {
					for (int k = o; k < 13; k++) {
						for (int e = k; e < 13; e++) {
							if (areDifferent(p, o, k, e)) {
								put(p, p, o, k, e, count--);
							}
						}
					}
				}
			}
		}

		private void putHighCard() {
			for (int p = 0; p < 13; p++) {
				for (int o = p + 1; o < 13; o++) {
					for (int k = o + 1; k < 13; k++) {
						for (int e = k + 1; e < 13; e++) {
							for (int r = e + 1; r < 13; r++) {
								if (areDifferent(p, o, k, e, r)
										&& !isStraight(p, o, k, e, r)) {
									put(p, o, k, e, r, count--);
								}
							}
						}
					}
				}
			}
		}

		private void put(int p, int o, int k, int e, int r, int count) {
			int[] i = new int[5];
			i[0] = p;
			i[1] = o;
			i[2] = k;
			i[3] = e;
			i[4] = r;
			Arrays.sort(i);
			rank[i[0]][i[1]][i[2]][i[3]][i[4]] = count;
		}

		private boolean areDifferent(int... a) {
			int n = a.length;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						if (a[i] == a[j]) {
							return false;
						}
					}
				}
			}
			return true;
		}

		private boolean isPair(int p, int o, int k, int e, int r) {

			boolean[] pairs = new boolean[10];

			pairs[0] = p == o;
			pairs[1] = p == k;
			pairs[2] = p == e;
			pairs[3] = p == r;
			pairs[4] = o == k;
			pairs[5] = o == e;
			pairs[6] = o == r;
			pairs[7] = k == e;
			pairs[8] = k == r;
			pairs[9] = e == r;

			boolean result = false;
			for (boolean b : pairs) {
				if (b) {
					result = true;
					break;
				}
			}
			return result;
		}

		private boolean isTwoPair(int p, int o, int k, int e, int r) {
			boolean[] pairs = new boolean[10];

			pairs[0] = p == o;
			pairs[1] = p == k;
			pairs[2] = p == e;
			pairs[3] = p == r;
			pairs[4] = o == k;
			pairs[5] = o == e;
			pairs[6] = o == r;
			pairs[7] = k == e;
			pairs[8] = k == r;
			pairs[9] = e == r;

			int numPairs = 0;
			for (boolean b : pairs) {
				if (b) {
					numPairs++;
				}
			}
			boolean result = false;

			if (numPairs == 2) {
				result = true;
			}
			return result;
		}

		private boolean isThreeOfAKind(int p, int o, int k, int e, int r) {

			boolean result = false;
			for (int i = 0; i < 13; i++) {
				int count = 0;
				if (p == i) {
					count++;
				}
				if (o == i) {
					count++;
				}
				if (k == i) {
					count++;
				}
				if (e == i) {
					count++;
				}
				if (r == i) {
					count++;
				}
				if (count == 3) {
					result = true;
					break;
				}
			}
			return result;
		}

		private boolean isFourOfAKind(int p, int o, int k, int e, int r) {
			boolean result = false;
			for (int i = 0; i < 13; i++) {
				int count = 0;
				if (p == i) {
					count++;
				}
				if (o == i) {
					count++;
				}
				if (k == i) {
					count++;
				}
				if (e == i) {
					count++;
				}
				if (r == i) {
					count++;
				}
				if (count == 4) {
					result = true;
					break;
				}
			}
			return result;
		}

		private boolean isFullHouse(int p, int o, int k, int e, int r) {
			boolean result;
			result = p == o && o == k && k != e && e == r;
			result |= p == o && o != k && k == e && e == r;
			return result;
		}

		private boolean isStraight(int p, int o, int k, int e, int r) {
			boolean isStraight = false;
			if (o - p == 1 && k - o == 1 && e - k == 1 && r - e == 1) {
				isStraight = true;
			} else if (p == 0 && o == 9 && k == 10 && e == 11 && r == 12) {
				isStraight = true;
			}

			//
			// int[] a = new int[5];
			// a[0] = p;
			// a[1] = o;
			// a[2] = k;
			// a[3] = e;
			// a[4] = r;
			// Arrays.sort(a);
			// boolean isStraight = true;
			// for (int i = 1; i < 5; i++) {
			// if (a[i] - a[i - 1] != 1) {
			// isStraight = false;
			// break;
			// }
			// }

			// straight ace to 5
			// if (isStraight)
			// log.info("is straight: " + p + " " + o + " " + k + " " + e
			// + " " + r + " " + isStraight);

			return isStraight;
		}
	}

	public enum HandValueType {

		HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH

	}
}
