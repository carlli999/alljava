public class Card {
	private int card_nbr;

	private int card_index;

	private int suit_index;

	public Card(int nbr) {
		card_nbr = nbr;
		card_index = card_nbr % 13;
		suit_index = card_nbr / 13;
	}

	public int getCardIndex() {
		return card_index;
	}

	public int getSuitIndex() {
		return suit_index;
	}

	public int getValue() {
		return card_nbr;
	}
}