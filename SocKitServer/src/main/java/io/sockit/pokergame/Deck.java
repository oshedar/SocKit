package io.sockit.pokergame;

import java.util.*;

public class Deck {
	
	private ArrayList<Card> cards;
	private int noOfMadeDecks;
        
	private static final List<Card> madeDeck = new ArrayList(52);
	static {
		for (Card.Suit suit : Card.Suit.values())
			for (Card.Rank rank : Card.Rank.values())
				madeDeck.add(new Card(rank, suit));
	}
        
        public static Deck freshDeck(){
            return new Deck();
        }
        
        public static Deck freshDeck(int noOfMadeDecks){
            return new Deck(noOfMadeDecks);
        }

	public static Deck shuffledDeck() {
		Deck result = new Deck();
		Collections.shuffle(result.cards);
		return result;
	}
        
	public static Deck shuffledDeck(int noOfMadeDecks) {
		Deck result = new Deck(noOfMadeDecks);
		Collections.shuffle(result.cards);
		return result;
	}
        
        public Deck(){
            this(1);
        }

        public Deck(int noOfMadeDecks){
            cards=new ArrayList(madeDeck.size()*noOfMadeDecks);
            this.noOfMadeDecks=noOfMadeDecks;
            for(int ctr=0;ctr<noOfMadeDecks;ctr++)
                cards.addAll(madeDeck);
        }
        
	/**
	 * Copy constructor
	 */
	private Deck(Deck source) {
		cards = new ArrayList<Card>(source.cards);
                noOfMadeDecks=source.noOfMadeDecks;
	}

        public void recreateFresh(){
            cards.clear();
            for(int ctr=0;ctr<noOfMadeDecks;ctr++)
                cards.addAll(madeDeck);
        }

        public void recreateShuffled(){
            cards.clear();
            for(int ctr=0;ctr<noOfMadeDecks;ctr++)
                cards.addAll(madeDeck);
            Collections.shuffle(cards);
        }
        
	public boolean contains(Object o) {
		return cards.contains(o);
	}

	public boolean containsAll(Collection<?> coll) {
		return cards.containsAll(coll);
	}

	@Override
        public boolean equals(Object that) {
		if (!(that instanceof Set) || ((Set)that).size() != cards.size())
			return false;
		for (Card c : cards)
			if (!((Set)that).contains(c))
				return false;
		return true;
	}

	public boolean isEmpty() {
		return cards.isEmpty();
	}

	public Iterator<Card> iterator() {
		return cards.iterator();
	}

	public void shuffle() {
		Collections.shuffle(cards);
	}

	public int size() {
		return cards.size();
	}

	public Object[] toArray() {
		return cards.toArray(new Card[cards.size()]);
	}
	
	public <T> T[] toArray(T[] a) {
		return cards.toArray(a);
	}
        
        public Card deal(){
            return cards.remove(0);
        }

	/**
	 * Returns a {@link String} containing a comma-space-separated list of cards.
	 * @return a {@link String} containing a comma-space-separated list of cards,
	 *			each the result of {@link Card#toString()}.
	 */
	@Override
	public String toString() {
		return cards.toString();
	}
}
