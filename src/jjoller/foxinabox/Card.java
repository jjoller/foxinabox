package jjoller.foxinabox;



/**
 * Representations of all 52 poker cards.
 */
public enum Card {

	ACE_S(CardValue.ACE, CardSuit.SPADES),

	ACE_H(CardValue.ACE, CardSuit.HEARTS),

	ACE_C(CardValue.ACE, CardSuit.CLUBS),

	ACE_D(CardValue.ACE, CardSuit.DIAMONDS),

	KING_S(CardValue.KING, CardSuit.SPADES),

	KING_H(CardValue.KING, CardSuit.HEARTS),

	KING_C(CardValue.KING, CardSuit.CLUBS),

	KING_D(CardValue.KING, CardSuit.DIAMONDS),

	QUEEN_S(CardValue.QUEEN, CardSuit.SPADES),

	QUEEN_H(CardValue.QUEEN, CardSuit.HEARTS),

	QUEEN_C(CardValue.QUEEN, CardSuit.CLUBS),

	QUEEN_D(CardValue.QUEEN, CardSuit.DIAMONDS),

	JACK_S(CardValue.JACK, CardSuit.SPADES),

	JACK_H(CardValue.JACK, CardSuit.HEARTS),

	JACK_C(CardValue.JACK, CardSuit.CLUBS),

	JACK_D(CardValue.JACK, CardSuit.DIAMONDS),

	TEN_S(CardValue.TEN, CardSuit.SPADES),

	TEN_H(CardValue.TEN, CardSuit.HEARTS),

	TEN_C(CardValue.TEN, CardSuit.CLUBS),

	TEN_D(CardValue.TEN, CardSuit.DIAMONDS),

	NINE_S(CardValue.NINE, CardSuit.SPADES),

	NINE_H(CardValue.NINE, CardSuit.HEARTS),

	NINE_C(CardValue.NINE, CardSuit.CLUBS),

	NINE_D(CardValue.NINE, CardSuit.DIAMONDS),

	EIGHT_S(CardValue.EIGHT, CardSuit.SPADES),

	EIGHT_H(CardValue.EIGHT, CardSuit.HEARTS),

	EIGHT_C(CardValue.EIGHT, CardSuit.CLUBS),

	EIGHT_D(CardValue.EIGHT, CardSuit.DIAMONDS),

	SEVEN_S(CardValue.SEVEN, CardSuit.SPADES),

	SEVEN_H(CardValue.SEVEN, CardSuit.HEARTS),

	SEVEN_C(CardValue.SEVEN, CardSuit.CLUBS),

	SEVEN_D(CardValue.SEVEN, CardSuit.DIAMONDS),

	SIX_S(CardValue.SIX, CardSuit.SPADES),

	SIX_H(CardValue.SIX, CardSuit.HEARTS),

	SIX_C(CardValue.SIX, CardSuit.CLUBS),

	SIX_D(CardValue.SIX, CardSuit.DIAMONDS),

	FIVE_S(CardValue.FIVE, CardSuit.SPADES),

	FIVE_H(CardValue.FIVE, CardSuit.HEARTS),

	FIVE_C(CardValue.FIVE, CardSuit.CLUBS),

	FIVE_D(CardValue.FIVE, CardSuit.DIAMONDS),

	FOUR_S(CardValue.FOUR, CardSuit.SPADES),

	FOUR_H(CardValue.FOUR, CardSuit.HEARTS),

	FOUR_C(CardValue.FOUR, CardSuit.CLUBS),

	FOUR_D(CardValue.FOUR, CardSuit.DIAMONDS),

	THREE_S(CardValue.THREE, CardSuit.SPADES),

	THREE_H(CardValue.THREE, CardSuit.HEARTS),

	THREE_C(CardValue.THREE, CardSuit.CLUBS),

	THREE_D(CardValue.THREE, CardSuit.DIAMONDS),

	TWO_S(CardValue.TWO, CardSuit.SPADES),

	TWO_H(CardValue.TWO, CardSuit.HEARTS),

	TWO_C(CardValue.TWO, CardSuit.CLUBS),

	TWO_D(CardValue.TWO, CardSuit.DIAMONDS);

	Card(CardValue value, CardSuit suit) {
		this.value = value;
		this.suit = suit;
	}

	private CardValue value;
	private CardSuit suit;

	/**
	 * get the ordinal of the card by the ordinal of the value and the ordinal
	 * of the suit.
	 * 
	 * @param valueOrdinal
	 * 		The ordinal of the card value
	 * @param suitOrdinal
	 * 		The ordinal of the card suit
	 * @return value ordinal
	 */
	public static int ordinalByOrdinals(int valueOrdinal, int suitOrdinal) {
		return valueOrdinal * 4 + suitOrdinal;
	}

	public static Card get(CardValue value, CardSuit suit) {
		return values()[ordinalByOrdinals(value.ordinal(), suit.ordinal())];
	}

	public CardValue getValue() {
		return value;
	}

	public CardSuit getSuit() {
		return suit;
	}

	public void setValue(CardValue value) {
		this.value = value;
	}

	public void setSuit(CardSuit suit) {
		this.suit = suit;
	}

	/**
	 * parses the string to find out what card it is. can handle codes like As,
	 * Qh, Jc, Td, 9c and so on.
	 * 
	 * @param s
	 * 		String to be parsed
	 * @return the card represented by the string
	 */
	public static Card getFromString(String s) {

		char c1 = s.charAt(0);
		char c2 = s.charAt(1);

		CardValue val = null;
		CardSuit suit = null;

		switch (c1) {
		case 'A':
			val = CardValue.ACE;
			break;
		case 'K':
			val = CardValue.KING;
			break;
		case 'Q':
			val = CardValue.QUEEN;
			break;
		case 'J':
			val = CardValue.JACK;
			break;
		case 'T':
			val = CardValue.TEN;
			break;
		case '9':
			val = CardValue.NINE;
			break;
		case '8':
			val = CardValue.EIGHT;
			break;
		case '7':
			val = CardValue.SEVEN;
			break;
		case '6':
			val = CardValue.SIX;
			break;
		case '5':
			val = CardValue.FIVE;
			break;
		case '4':
			val = CardValue.FOUR;
			break;
		case '3':
			val = CardValue.THREE;
			break;
		case '2':
			val = CardValue.TWO;
			break;
		}

		switch (c2) {
		case 's':
			suit = CardSuit.SPADES;
			break;
		case 'h':
			suit = CardSuit.HEARTS;
			break;
		case 'c':
			suit = CardSuit.CLUBS;
			break;
		case 'd':
			suit = CardSuit.DIAMONDS;
			break;
		}

		if (val == null || suit == null) {
			throw new IllegalArgumentException();
		}

		return Card.get(val, suit);
	}

	@Override
	public String toString() {

		String s = "";

		switch (value) {
		case ACE:
			s = "A";
			break;

		case KING:
			s = "K";
			break;

		case QUEEN:
			s = "Q";
			break;

		case JACK:
			s = "J";
			break;

		case TEN:
			s = "T";
			break;

		case NINE:
			s = "9";
			break;

		case EIGHT:
			s = "8";
			break;

		case SEVEN:
			s = "7";
			break;

		case SIX:
			s = "6";
			break;

		case FIVE:
			s = "5";
			break;

		case FOUR:
			s = "4";
			break;

		case THREE:
			s = "3";
			break;

		case TWO:
			s = "2";
			break;

		}

		switch (suit) {
		case SPADES:
			s += "s";
			break;

		case HEARTS:
			s += "h";
			break;

		case CLUBS:
			s += "c";
			break;

		case DIAMONDS:
			s += "d";
			break;
		}

		return s;
	}
		
	enum CardValue {
		ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO
	}

	enum CardSuit {
		SPADES, HEARTS, CLUBS, DIAMONDS
	}

}
