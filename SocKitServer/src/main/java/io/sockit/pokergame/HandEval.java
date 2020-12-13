package io.sockit.pokergame;

import java.util.ArrayList;
import java.util.List;


public final class HandEval {

	private HandEval() {}	// no instances
	
	/**
	 * Returns a value which can be used as a parameter to one of the HandEval evaluation methods.
	 * @param cs a {@link Deck}
	 * @return a value which can be used as a parameter to one of the HandEval evaluation methods.
	 * The value may also be bitwise OR'ed or added to other such
	 * values to build an evaluation method parameter.
	 */
	public static long encode(final List<Card> cs) {
		long result = 0;
		for (Card c : cs)
			result |= c.code;
		return result;
	}

	public static enum HandCategory { NO_PAIR, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
							FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH; }

	private static final int   RANK_SHIFT_1		= 4;
	private static final int   RANK_SHIFT_2		= RANK_SHIFT_1 + 4;
	private static final int   RANK_SHIFT_3		= RANK_SHIFT_2 + 4;
	private static final int   RANK_SHIFT_4		= RANK_SHIFT_3 + 4;
	private static final int   VALUE_SHIFT		= RANK_SHIFT_4 + 8;

	private static final int   NO_PAIR			= 0;
	private static final int   PAIR				= NO_PAIR			+ (1 << VALUE_SHIFT);
	private static final int   TWO_PAIR			= PAIR				+ (1 << VALUE_SHIFT);
	private static final int   THREE_OF_A_KIND	= TWO_PAIR			+ (1 << VALUE_SHIFT);
	private static final int   STRAIGHT			= THREE_OF_A_KIND	+ (1 << VALUE_SHIFT);
	private static final int   FLUSH			= STRAIGHT			+ (1 << VALUE_SHIFT);
	private static final int   FULL_HOUSE		= FLUSH				+ (1 << VALUE_SHIFT);
	private static final int   FOUR_OF_A_KIND	= FULL_HOUSE		+ (1 << VALUE_SHIFT);
	private static final int   STRAIGHT_FLUSH	= FOUR_OF_A_KIND	+ (1 << VALUE_SHIFT);

	/**
	 *  Greater than any return value of the HandEval evaluation methods.
	 */
	public static final int NO_8_LOW = STRAIGHT_FLUSH + (1 << VALUE_SHIFT);

	private static final int   ARRAY_SIZE		= 0x1FC0 + 1;			// all combos of up to 7 of LS 13 bits on
	/* Arrays for which index is bit mask of card ranks in hand: */
	private static final int[] straightValue	= new int[ARRAY_SIZE];	// Value(STRAIGHT) | (straight's high card rank-2 (3..12) << RANK_SHIFT_4); 0 if no straight
	private static final int[] nbrOfRanks		= new int[ARRAY_SIZE];	// count of bits set
	private static final int[] hiRank			= new int[ARRAY_SIZE];	// 4-bit card rank of highest bit set, right justified
	private static final int[] hiUpTo5Ranks		= new int[ARRAY_SIZE];	// 4-bit card ranks of highest (up to) 5 bits set, right-justified
	private static final int[] loMaskOrNo8Low	= new int[ARRAY_SIZE];	// low-order 5 of the low-order 8 bits set, or NO_8_LOW; Ace is LS bit.
	private static final int[] lo3_8OBRanksMask	= new int[ARRAY_SIZE];	// bits other than lowest 3 8-or-better reset; Ace is LS bit.

	private static int flushAndOrStraight7(final int ranks, final int c, final int d, final int h, final int s) {

		int	i, j;
		
		if ((j = nbrOfRanks[c]) > 7 - 5) {
			// there's either a club flush or no flush
			if (j >= 5)
				if ((i = straightValue[c]) == 0)
					return FLUSH | hiUpTo5Ranks[c];
				else
					return (STRAIGHT_FLUSH - STRAIGHT) + i;
		} else if ((j += (i = nbrOfRanks[d])) > 7 - 5) {
			if (i >= 5)
				if ((i = straightValue[d]) == 0)
					return FLUSH | hiUpTo5Ranks[d];
				else
					return (STRAIGHT_FLUSH - STRAIGHT) + i;
		} else if ((j += (i = nbrOfRanks[h])) > 7 - 5) {
			if (i >= 5)
				if ((i = straightValue[h]) == 0)
					return FLUSH | hiUpTo5Ranks[h];
				else
					return (STRAIGHT_FLUSH - STRAIGHT) + i;
		} else
			/* total cards in other suits <= 7-5: spade flush: */
			if ((i = straightValue[s]) == 0)
				return FLUSH | hiUpTo5Ranks[s];
			else
				return (STRAIGHT_FLUSH - STRAIGHT) + i;
		return straightValue[ranks];
	}

	/**
	 * Returns the value of the best 5-card high poker hand from 7 cards.
	 * @param hand bit mask with one bit set for each of 7 cards.
	 * @return the value of the best 5-card high poker hand.
	 */
	public static int hand7Eval(long hand) {
		int i, j, ranks;

		/* 
		 * The parameter contains four 16-bit fields; in each, the low-order
		 * 13 bits are significant.  Get the respective fields into variables.
		 * We don't care which suit is which; we arbitrarily call them c,d,h,s.
		 */
		final int c = (int)hand & 0x1FFF;
		final int d = ((int)hand >>> 16) & 0x1FFF;
		final int h = (int)(hand >>> 32) & 0x1FFF;
		final int s = (int)(hand >>> 48) & 0x1FFF;

		switch (nbrOfRanks[ranks = c | d | h | s]) {

		case 2:
		/*
		 * quads with trips kicker
		 */
			i = c & d & h & s; /* bit for quads */
			return FOUR_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3);

		case 3:
		/*
		 * trips and pair (full house) with non-playing pair,
		 * or two trips (full house) with non-playing singleton,
		 * or quads with pair and singleton
		 */
			/* bits for singleton, if any, and trips, if any: */
			if (nbrOfRanks[i = c ^ d ^ h ^ s] == 3) {
				/* two trips (full house) with non-playing singleton */
				if (nbrOfRanks[i = c & d] != 2)
					if (nbrOfRanks[i = c & h] != 2)
						if (nbrOfRanks[i = c & s] != 2)
							if (nbrOfRanks[i = d & h] != 2)
								if (nbrOfRanks[i = d & s] != 2)
									i = h & s; /* bits for the trips */
				return FULL_HOUSE | (hiUpTo5Ranks[i] << RANK_SHIFT_3);
			}
			if ((j = c & d & h & s) != 0) /* bit for quads */
				/* quads with pair and singleton */
				return FOUR_OF_A_KIND | (hiRank[j] << RANK_SHIFT_4) | (hiRank[ranks ^ j] << RANK_SHIFT_3);
			/* trips and pair (full house) with non-playing pair */
			return FULL_HOUSE | (hiRank[i] << RANK_SHIFT_4) | (hiRank[ranks ^ i] << RANK_SHIFT_3);

		case 4:
		/*
		 * three pair and singleton,
		 * or trips and pair (full house) and two non-playing singletons,
		 * or quads with singleton kicker and two non-playing singletons
		 */
			i = c ^ d ^ h ^ s; // the bit(s) of the trips, if any, and singleton(s)
			if (nbrOfRanks[i] == 1) {
				/* three pair and singleton */
				j = hiUpTo5Ranks[ranks ^ i];	/* ranks of the three pairs */
				return TWO_PAIR | ((j & 0x0FF0) << RANK_SHIFT_2) | (hiRank[i | (1 << (j & 0x000F))] << RANK_SHIFT_2);
			}
			if ((j = c & d & h & s) == 0) {
				// trips and pair (full house) and two non-playing singletons
				i ^= ranks; /* bit for the pair */
				if ((j = (c & d) & (~i)) == 0)
					j = (h & s) & (~i); /* bit for the trips */
				return FULL_HOUSE | (hiRank[j] << RANK_SHIFT_4) | (hiRank[i] << RANK_SHIFT_3);
			}
			// quads with singleton kicker and two non-playing singletons
			return FOUR_OF_A_KIND | (hiRank[j] << RANK_SHIFT_4) | (hiRank[i] << RANK_SHIFT_3);

		case 5:
		/*
		 * flush and/or straight,
		 * or two pair and three singletons,
		 * or trips and four singletons
		 */
			if ((i = flushAndOrStraight7(ranks, c, d, h, s)) != 0)
				return i;
			i = c ^ d ^ h ^ s; // the bits of the trips, if any, and singletons
			if (nbrOfRanks[i] != 5)
				/* two pair and three singletons */
				return TWO_PAIR | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_3) | (hiRank[i] << RANK_SHIFT_2);
			/* trips and four singletons */
			if ((j = c & d) == 0)
				j = h & s;
			// j has trips bit
			return THREE_OF_A_KIND | (hiRank[j] << RANK_SHIFT_4) | (hiUpTo5Ranks[i ^ j] & 0x0FF00);

		case 6:
		/*
		 * flush and/or straight,
		 * or one pair and three kickers and two nonplaying singletons
		 */
			if ((i = flushAndOrStraight7(ranks, c, d, h, s)) != 0)
				return i;
			i = c ^ d ^ h ^ s; /* the bits of the five singletons */
			return PAIR | (hiRank[ranks ^ i] << RANK_SHIFT_4) | ((hiUpTo5Ranks[i] & 0x0FFF00) >> RANK_SHIFT_1);

		case 7:
		/*
		 * flush and/or straight or no pair
		 */
			if ((i = flushAndOrStraight7(ranks, c, d, h, s)) != 0)
				return i;
			return  NO_PAIR | hiUpTo5Ranks[ranks];

		} /* end switch */

		return 0; /* never reached, but avoids compiler warning */
	}


	/**
	 * Returns the value of the best 5-card Razz poker hand from 7 cards.
	 * @param hand bit mask with one bit set for each of 7 cards.
	 * @return the value of the best 5-card Razz poker hand.
	 */
	public static int handRazzEval(long hand) {

		// each of the following extracts a 13-bit field from hand and
		// rotates it left to position the ace in the least significant bit
		final int c = (((int)hand & 0x0FFF) << 1)  + (((int)hand & 0x1000) >> 12);
		final int d = (((int)hand >> 15) & 0x1FFE) + (((int)hand & (0x1000 << 16)) >> 28);
		final int h = ((int)(hand >> 31) & 0x1FFE) + (int)((hand & (0x1000L << 32)) >> 44);
		final int s = ((int)(hand >> 47) & 0x1FFE) + (int)((hand & (0x1000L << 48)) >> 60);

		final int ranks = c | d | h | s;
		int i, j;

		switch (nbrOfRanks[ranks]) {

		case 2:
			/* AAAABBB -- full house */
			i = c & d & h & s; /* bit for quads */
			j = i ^ ranks; /* bit for trips */
			// it can't matter in comparison of results from a 52-card deck,
			// but we return the correct value per relative ranks
			if (i < j)
				return FULL_HOUSE | (hiRank[i] << RANK_SHIFT_4) | (hiRank[j] << RANK_SHIFT_3);
			return FULL_HOUSE | (hiRank[j] << RANK_SHIFT_4) | (hiRank[i] << RANK_SHIFT_3);

		case 3:
			/*
			 * AAABBBC -- two pair,
			 * AAAABBC -- two pair,
			 * AAABBCC -- two pair w/ kicker = highest rank.
			 */
			/* bits for singleton, if any, and trips, if any: */
			if (nbrOfRanks[i = c ^ d ^ h ^ s] == 3) {
				/* odd number of each rank: AAABBBC -- two pair */
				if (nbrOfRanks[i = c & d] != 2)
					if (nbrOfRanks[i = c & h] != 2)
						if (nbrOfRanks[i = c & s] != 2)
							if (nbrOfRanks[i = d & h] != 2)
								if (nbrOfRanks[i = d & s] != 2)
									i = h & s; /* bits for the trips */
				return TWO_PAIR | (hiUpTo5Ranks[i] << RANK_SHIFT_3) | (hiRank[ranks ^ i] << RANK_SHIFT_2);
			}
			if ((j = c & d & h & s) != 0)  /* bit for quads */
				/* AAAABBC -- two pair */
				return TWO_PAIR | (hiUpTo5Ranks[ranks ^ i] << RANK_SHIFT_3) | (hiRank[i] << RANK_SHIFT_2);
			/* AAABBCC -- two pair w/ kicker = highest rank */
			i = hiUpTo5Ranks[ranks]; /* 00KPP */
			return TWO_PAIR | ((i | (i << RANK_SHIFT_3)) & 0x0FFF00);	// TWO_PAIR | (KPPKPP & 0x0FFF00)

		case 4:
			/*
			 * AABBCCD -- one pair (lowest of A, B, C),
			 * AAABBCD -- one pair (A or B),
			 * AAAABCD -- one pair (A)
			 */
			i = c ^ d ^ h ^ s; /* the bit(s) of the trips, if any,
			 and singleton(s) */
			if (nbrOfRanks[i] == 1) {
				/* AABBCCD -- one pair, C with ABD; D's bit is in i */
				j = ranks ^ i;	// ABC bits
				int k = hiUpTo5Ranks[j] & 0x0000F;	// C rank
				i |= j ^ (1 << (k));	// ABD bits
				return PAIR | (k << RANK_SHIFT_4) | (hiUpTo5Ranks[i] << RANK_SHIFT_1);
			}
			if ((j = c & d & h & s) == 0) {
				/* AAABBCD -- one pair (A or B) */
				i ^= ranks; /* bit for B */
				if ((j = (c & d) & (~i)) == 0)
					j = (h & s) & (~i); /* bit for A */
				if (i < j)
					return PAIR | (hiRank[i] << RANK_SHIFT_4) | (hiUpTo5Ranks[ranks ^ i] << RANK_SHIFT_1);
				return PAIR | (hiRank[j] << RANK_SHIFT_4) | (hiUpTo5Ranks[ranks ^ j] << RANK_SHIFT_1);
			}
			/* AAAABCD -- one pair (A); j has A's bit */
			return PAIR | (hiRank[j] << RANK_SHIFT_4) | (hiUpTo5Ranks[i] << RANK_SHIFT_1);

		case 5:
			return NO_PAIR |  hiUpTo5Ranks[ranks];

		case 6:
			i = ranks ^ (1 << hiRank[ranks]);
			return NO_PAIR |  hiUpTo5Ranks[i];

		case 7:
			i = ranks ^ (1 << hiRank[ranks]);
			i ^= (1 << hiRank[i]);
			return NO_PAIR |  hiUpTo5Ranks[i];

		} /* end switch */

	    return 0; /* never reached, but avoids compiler warning */
	}

	private static int flushAndOrStraight6(final int ranks, final int c, final int d, final int h, final int s) {

		int	i, j;
		
		if ((j = nbrOfRanks[c]) > 6 - 5) {
			// there's either a club flush or no flush
			if (j >= 5)
				if ((i = straightValue[c]) == 0)
					return FLUSH | hiUpTo5Ranks[c];
				else
					return (STRAIGHT_FLUSH - STRAIGHT) + i;
		} else if ((j += (i = nbrOfRanks[d])) > 6 - 5) {
			if (i >= 5)
				if ((i = straightValue[d]) == 0)
					return FLUSH | hiUpTo5Ranks[d];
				else
					return (STRAIGHT_FLUSH - STRAIGHT) + i;
		} else if ((j += (i = nbrOfRanks[h])) > 6 - 5) {
			if (i >= 5)
				if ((i = straightValue[h]) == 0)
					return FLUSH | hiUpTo5Ranks[h];
				else
					return (STRAIGHT_FLUSH - STRAIGHT) + i;
		} else
			/* total cards in other suits <= N-5: spade flush: */
			if ((i = straightValue[s]) == 0)
				return FLUSH | hiUpTo5Ranks[s];
			else
				return (STRAIGHT_FLUSH - STRAIGHT) + i;
		return straightValue[ranks];
	}

	/**
	 * Returns the value of the best 5-card high poker hand from 6 cards.
	 * @param hand bit mask with one bit set for each of 6 cards.
	 * @return the value of the best 5-card high poker hand.
	 */
	public static int hand6Eval(long hand) {

		final int c = (int)hand & 0x1FFF;
		final int d = ((int)hand >>> 16) & 0x1FFF;
		final int h = (int)(hand >>> 32) & 0x1FFF;
		final int s = (int)(hand >>> 48) & 0x1FFF;

		final int ranks = c | d | h | s;
		int i, j;

	    switch (nbrOfRanks[ranks]) {

	        case 2: /* quads with pair kicker,
					   or two trips (full house) */
					/* bits for trips, if any: */
	                if ((nbrOfRanks[i = c ^ d ^ h ^ s]) != 0)
	                    /* two trips (full house) */
	                	return FULL_HOUSE | (hiUpTo5Ranks[i] << RANK_SHIFT_3);
					/* quads with pair kicker */
	                i = c & d & h & s;  /* bit for quads */
	                return FOUR_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3);

			case 3:	/* quads with singleton kicker and non-playing singleton,
					   or full house with non-playing singleton,
					   or two pair with non-playing pair */
					if ((c ^ d ^ h ^ s) == 0)
						/* no trips or singletons:  three pair */
						return TWO_PAIR | (hiUpTo5Ranks[ranks] << RANK_SHIFT_2);
					if ((i = c & d & h & s) == 0) {
						/* full house with singleton */
						if ((i = c & d & h) == 0)
							if ((i = c & d & s) == 0)
								if ((i = c & h & s) == 0)
									i = d & h & s; /* bit of trips */
						j = c ^ d ^ h ^ s; /* the bits of the trips and singleton */
						return FULL_HOUSE | (hiRank[i] << RANK_SHIFT_4) | (hiRank[j ^ ranks] << RANK_SHIFT_3); }
					/* quads with kicker and singleton */
					return FOUR_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3);

			case 4:	/* trips and three singletons,
					   or two pair and two singletons */
					if ((i = c ^ d ^ h ^ s) != ranks)
						/* two pair and two singletons */
						return TWO_PAIR | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_3) | (hiRank[i] << RANK_SHIFT_2);
					/* trips and three singletons */
					if ((i = c & d) == 0)
						i = h & s; /* bit of trips */
					return THREE_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) | ((hiUpTo5Ranks[ranks ^ i] & 0x00FF0) << RANK_SHIFT_1);

			case 5:	/* flush and/or straight,
					   or one pair and three kickers and
					    one non-playing singleton */
					if ((i = flushAndOrStraight6(ranks, c, d, h, s)) != 0)
						return i;
	                i = c ^ d ^ h ^ s; /* the bits of the four singletons */
	                return PAIR | (hiRank[ i ^ ranks] << RANK_SHIFT_4) | (hiUpTo5Ranks[i] & 0x0FFF0);

			case 6:	/* flush and/or straight or no pair */
					if ((i = flushAndOrStraight6(ranks, c, d, h, s)) != 0)
						return i;
	                return NO_PAIR |  hiUpTo5Ranks[ranks];

	        } /* end switch */

	    return 0; /* never reached, but avoids compiler warning */
	}

	/**
	 * Returns the value of a 5-card poker hand.
	 * @param hand bit mask with one bit set for each of 5 cards.
	 * @return the value of the hand.
	 */
	public static int hand5Eval(long hand) {
	
		final int c = (int)hand & 0x1FFF;
		final int d = ((int)hand >>> 16) & 0x1FFF;
		final int h = (int)(hand >>> 32) & 0x1FFF;
		final int s = (int)(hand >>> 48) & 0x1FFF;

		final int ranks = c | d | h | s;
		int i;

		switch (nbrOfRanks[ranks]) {

	        case 2: /* quads or full house */
	                i = c & d;				/* any two suits */
	                if ((i & h & s) == 0) { /* no bit common to all suits */
	                    i = c ^ d ^ h ^ s;  /* trips bit */
	                    return FULL_HOUSE | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3); }
	                else
	                    /* the quads bit must be present in each suit mask,
	                       but the kicker bit in no more than one; so we need
	                       only AND any two suit masks to get the quad bit: */
	                    return FOUR_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3);

	        case 3: /* trips and two kickers,
	                   or two pair and kicker */
	                if ((i = c ^ d ^ h ^ s) == ranks) {
	                    /* trips and two kickers */
	                    if ((i = c & d) == 0)
	                    	if ((i = c & h) == 0)
	                    			i = d & h;
	                    return THREE_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) 
	                        | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_2); }
	                /* two pair and kicker; i has kicker bit */
	                return TWO_PAIR | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_3) | (hiRank[i] << RANK_SHIFT_2);

	        case 4: /* pair and three kickers */
	                i = c ^ d ^ h ^ s; /* kicker bits */
	                return PAIR | (hiRank[ranks ^ i] << RANK_SHIFT_4) | (hiUpTo5Ranks[i] << RANK_SHIFT_1);

	        case 5: /* flush and/or straight, or no pair */
					if ((i = straightValue[ranks]) == 0)
						i = hiUpTo5Ranks[ranks];
					if (c != 0) {			/* if any clubs... */
						if (c != ranks)		/*   if no club flush... */
							return i; }		/*      return straight or no pair value */
					else
						if (d != 0) {
							if (d != ranks)
								return i; }
						else
							if (h != 0) {
								if (h != ranks)
									return i; }
						/*	else s == ranks: spade flush */
					/* There is a flush */
					if (i < STRAIGHT)
						/* no straight */
						return FLUSH | i;
					else
						return (STRAIGHT_FLUSH - STRAIGHT) + i;
		}

	    return 0; /* never reached, but avoids compiler warning */
	}

	/**
	 * Returns the Deuce-to-Seven low (Kansas City lowball) value of a 5-card poker hand.
	 * @param hand bit mask with one bit set for each of 5 cards.
	 * @return the value of the hand.
	 */
	public static int hand2to7LoEval(long hand) {

		final int WHEEL_EVAL		= 0x04030000;
		final int WHEEL_FLUSH_EVAL	= 0x08030000;
		final int NO_PAIR_ACE_HIGH	= 0x000C3210;
		
		int	result = hand5Eval(hand);
		if (result == WHEEL_EVAL)
			return NO_PAIR_ACE_HIGH;
		if (result == WHEEL_FLUSH_EVAL)
			return FLUSH | NO_PAIR_ACE_HIGH;
		return result;
		
	}

	/**
	 * Returns the Ace-to-5 value of a 5-card low poker hand.
	 * @param hand bit mask with one bit set for each of 5 cards.
	 * @return the Ace-to-5 low value of the hand.
	 */
	public static int handAto5LoEval(long hand) {

		// each of the following extracts a 13-bit field from hand and
		// rotates it left to position the ace in the least significant bit
		final int c = (((int)hand & 0x0FFF) << 1)  + (((int)hand & 0x1000) >> 12);
		final int d = (((int)hand >> 15) & 0x1FFE) + (((int)hand & (0x1000 << 16)) >> 28);
		final int h = ((int)(hand >> 31) & 0x1FFE) + (int)((hand & (0x1000L << 32)) >> 44);
		final int s = ((int)(hand >> 47) & 0x1FFE) + (int)((hand & (0x1000L << 48)) >> 60);

		final int ranks = c | d | h | s;
		int i;

		switch (nbrOfRanks[ranks]) {

	        case 2: /* quads or full house */
	                i = c & d;				/* any two suits */
	                if ((i & h & s) == 0) { /* no bit common to all suits */
	                    i = c ^ d ^ h ^ s;  /* trips bit */
	                    return FULL_HOUSE | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3); }
	                else
	                    /* the quads bit must be present in each suit mask,
	                       but the kicker bit in no more than one; so we need
	                       only AND any two suit masks to get the quad bit: */
	                    return FOUR_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) | (hiRank[i ^ ranks] << RANK_SHIFT_3);

	        case 3: /* trips and two kickers,
	                   or two pair and kicker */
	                if ((i = c ^ d ^ h ^ s) == ranks) {
	                    /* trips and two kickers */
	                    if ((i = c & d) == 0)
	                    	if ((i = c & h) == 0)
	                    		i = d & h;
	                    return THREE_OF_A_KIND | (hiRank[i] << RANK_SHIFT_4) 
	                        | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_2); }
	                /* two pair and kicker; i has kicker bit */
	                return TWO_PAIR | (hiUpTo5Ranks[i ^ ranks] << RANK_SHIFT_3) | (hiRank[i] << RANK_SHIFT_2);

	        case 4: /* pair and three kickers */
	                i = c ^ d ^ h ^ s; /* kicker bits */
	                return PAIR | (hiRank[ranks ^ i] << RANK_SHIFT_4) | (hiUpTo5Ranks[i] << RANK_SHIFT_1);

	        case 5: /* no pair */
					return hiUpTo5Ranks[ranks];
		}

	    return 0; /* never reached, but avoids compiler warning */
	}

	/**
	 * Returns the bitwise OR of the suit masks comprising <code>hand</code>; Ace is high.
	 * @param hand bit mask with one bit set for each of 0 to 52 cards.
	 * @return the bitwise OR of the suit masks comprising <code>hand</code>.
	 */
	public static int ranksMask(long hand) {
		
		return (	((int)hand & 0x1FFF)
				|	(((int)hand >>> 16) & 0x1FFF)
				|	((int)(hand >>> 32) & 0x1FFF)
				|	((int)(hand >>> 48) & 0x1FFF)
			   );		
	}

	/**
	 * Returns the bitwise OR of the suit masks comprising <code>hand</code>; Ace is low.
	 * @param hand bit mask with one bit set for each of 0 to 52 cards.
	 * @return the bitwise OR of the suit masks comprising <code>hand</code>.
	 */
	public static int ranksMaskLo(long hand) {
		
		return (	((((int)hand & 0x0FFF) << 1)  + (((int)hand & 0x1000) >> 12))
				|	((((int)hand >> 15) & 0x1FFE) + (((int)hand & (0x1000  << 16)) >> 28))
				|	(((int)(hand >> 31) & 0x1FFE) + (int)((hand & (0x1000L << 32)) >> 44))
				|	(((int)(hand >> 47) & 0x1FFE) + (int)((hand & (0x1000L << 48)) >> 60))
			   );		
	}

	/**
	 * Returns the 8-or-better low value of a 5-card poker hand or {@link #NO_8_LOW}.
	 * @param hand bit mask with one bit set for each of up to 7 cards.
	 * @return the 8-or-better low value of <code>hand</code> or {@link #NO_8_LOW}.
	 */
	public static int hand8LowEval(long hand) {
		
		int result = loMaskOrNo8Low[ranksMaskLo(hand)];
		return result == NO_8_LOW ? NO_8_LOW : hiUpTo5Ranks[result];
	}

	private static int Omaha8LowMaskEval(int twoHolesMask, int boardMask) {
	    return loMaskOrNo8Low[lo3_8OBRanksMask[boardMask & ~twoHolesMask] | twoHolesMask];
	}

	/**
	 * Returns the 8-or-better low value of a 5-card poker hand comprised of three board
	 * cards and two hole cards or {@link #NO_8_LOW}.
	 * @param holeCards CardSet of four hole cards.
	 * @param boardCards CardSet of at least three board cards.
	 * @return the 8-or-better low value or {@link #NO_8_LOW}.
	 */
	public static int Omaha8LowEval(List<Card> holeCards, List<Card> boardCards) {
		
		int board = ranksMaskLo(encode(boardCards));
		if (lo3_8OBRanksMask[board] == 0)
			return NO_8_LOW;
		int hole8OB[] = new int[4];
		int i, hole8OBCount = 0;
		for (Card c : holeCards)
			if ((i = ranksMaskLo(c.code)) <= 0x0080)	// hole card rank <= 8?
				hole8OB[hole8OBCount++] = i;
		int result = NO_8_LOW;
		if (hole8OBCount >= 2) {
    		if ((i = Omaha8LowMaskEval(hole8OB[0] | hole8OB[1], board)) < result)
    				result = i;
    		if (hole8OBCount >= 3 ) {
        		if ((i = Omaha8LowMaskEval(hole8OB[0] | hole8OB[2], board)) < result)
        			result = i;
        		if ((i = Omaha8LowMaskEval(hole8OB[1] | hole8OB[2], board)) < result)
        			result = i;
        		if (hole8OBCount == 4) {
            		if ((i = Omaha8LowMaskEval(hole8OB[0] | hole8OB[3], board)) < result)
            			result = i;
            		if ((i = Omaha8LowMaskEval(hole8OB[1] | hole8OB[3], board)) < result)
            			result = i;
            		if ((i = Omaha8LowMaskEval(hole8OB[2] | hole8OB[3], board)) < result)
            			result = i;
        		}
    		}
		}
		return result == NO_8_LOW ? NO_8_LOW : hiUpTo5Ranks[result];
	}

// The following exports of accessors to arrays used by the
// evaluation routines may be uncommented if needed.
	
//	/**
//	 * Returns the number of bits set in mask.
//	 * @param mask an int in the range 0..0x1FC0 (8128).
//	 * @return the number of bits set in mask.
//	 * @throws IndexOutOfBoundsException if mask < 0 || mask > 0x1FC0.
//	 */
//	public static int numberOfRanks(int mask)
//	{
//	    return nbrOfRanks[mask];
//	}
//
//	/**
//	 * Returns the rank (2..14) corresponding to the high-order bit set in mask.
//	 * @param mask an int in the range 0..0x1FC0 (8128).
//	 * @return the rank (2..14) corresponding to the high-order bit set in mask.
//	 * @throws IndexOutOfBoundsException if mask < 0 || mask > 0x1FC0.
//	 */
//	public static int rankOfHiCard(int mask)
//	{
//	    return hiRank[mask] + 2;
//	}

	/** ********** Initialization ********************** */

	private static final int ACE_RANK	= 14;

	private static final int A5432		= 0x0000100F; // A5432

	// initializer block
	static {
		int mask, bitCount, ranks;
		int shiftReg, i;
		int value;

		for (mask = 1; mask < ARRAY_SIZE; ++mask) {
			bitCount = ranks = 0;
			shiftReg = mask;
			for (i = ACE_RANK - 2; i >= 0; --i, shiftReg <<= 1)
				if ((shiftReg & 0x1000) != 0)
					if (++bitCount <= 5) {
						ranks <<= RANK_SHIFT_1;
						ranks += i;
						if (bitCount == 1)
							hiRank[mask] = i;
					}
			hiUpTo5Ranks[mask] = ranks;
			nbrOfRanks[mask] = bitCount;

			loMaskOrNo8Low[mask] = NO_8_LOW;
			bitCount = value = 0;
			shiftReg = mask;
			/* For the purpose of this loop, Ace is low; it's in the LS bit */
			for (i = 0; i < 8; ++i, shiftReg >>= 1)
				if ((shiftReg & 1) != 0) {
					value |= (1 << i); /* undo previous shifts, copy bit */
					if (++bitCount == 3)
						lo3_8OBRanksMask[mask] = value;
					if (bitCount == 5) {
						loMaskOrNo8Low[mask] = value;
						break; }
				}
		}
		for (mask = 0x1F00/* A..T */; mask >= 0x001F/* 6..2 */; mask >>= 1)
			setStraight(mask);
		setStraight(A5432); /* A,5..2 */
	}

	private static void setStraight(int ts) {
		/* must call with ts from A..T to 5..A in that order */

			int es, i, j;

			for (i = 0x1000; i > 0; i >>= 1)
				for (j = 0x1000; j > 0; j >>= 1) {
					es = ts | i | j; /* 5 straight bits plus up to two other bits */
					if (straightValue[es] == 0)
						if (ts == A5432)
							straightValue[es] = STRAIGHT | ((5-2) << RANK_SHIFT_4);
						else
							straightValue[es] = STRAIGHT | (hiRank[ts] << RANK_SHIFT_4);
				}
		}
        
        private static Card.Rank[] ranks={Card.Rank.TWO, Card.Rank.THREE, Card.Rank.FOUR, Card.Rank.FIVE, Card.Rank.SIX, Card.Rank.SEVEN, Card.Rank.EIGHT, Card.Rank.NINE, Card.Rank.TEN, Card.Rank.JACK, Card.Rank.QUEEN, Card.Rank.KING, Card.Rank.ACE};
        private static HandCategory[] handCategories={HandCategory.NO_PAIR, HandCategory.PAIR, HandCategory.TWO_PAIR, HandCategory.THREE_OF_A_KIND, HandCategory.STRAIGHT, HandCategory.FLUSH, HandCategory.FULL_HOUSE, HandCategory.FOUR_OF_A_KIND, HandCategory.STRAIGHT_FLUSH};        
        public static HandCategory getHandCategory(int evalCode){
            return handCategories[evalCode>>24];
        }
        
        public static List<Card> getWinningCards(int evalCode,List<Card> hand,List<Card> tableCards){
            List<Card> cards=new ArrayList<>(7);
            cards.addAll(hand);
            cards.addAll(tableCards);
            return getWinniningCards(evalCode, cards);
        }
        
        public static List<Card> getWinniningCards(int evalCode,List<Card> cards){
            HandCategory handCategory=getHandCategory(evalCode);
            switch(handCategory){
                case NO_PAIR: return getNoPairWinningCards(evalCode, cards);
                case PAIR: return getPairWinningCards(evalCode, cards);
                case TWO_PAIR: return getTwoPairWinningCards(evalCode, cards);
                case THREE_OF_A_KIND: return getThreeOfAKindWinningCards(evalCode, cards);
                case STRAIGHT: return getStraightWinningCards(evalCode, cards);
                case FLUSH: return getFlushWinningCards(evalCode, cards);
                case FULL_HOUSE: return getFullHouseWinningCards(evalCode, cards);
                case FOUR_OF_A_KIND: return getFourOfAKindWinningCards(evalCode, cards);
                case STRAIGHT_FLUSH: return getStraightFlushWinningCards(evalCode, cards);
            }
            return null;
        }
        
        private static Card findCard(Card.Rank rank, List<Card> cards){
            for(Card card:cards){
                if(card.rank==rank)
                    return card;
            }
            return null;
        }
        
        private static Card findCard(Card.Rank rank, Card.Suit suit, List<Card> cards){
            for(Card card:cards){
                if(card.rank==rank && card.suit==suit)
                    return card;
            }
            return null;
        }
        
        private static Card.Suit findMaxSuit(List<Card> cards){
            int spadeCount=0;
            int diamondCount=0;
            int clubCount=0;
            int heartCount=0;
            int maxCount=0;
            for(Card card:cards){
                switch(card.suit){
                    case HEART: heartCount++;break;
                    case DIAMOND: diamondCount++;break;
                    case CLUB: clubCount++;break;
                    case SPADE: spadeCount++;break;
                }
            }
            maxCount=heartCount;
            if(maxCount<diamondCount)
                maxCount=diamondCount;
            if(maxCount<clubCount)
                maxCount=clubCount;
            if(maxCount<spadeCount)
                maxCount=spadeCount;
            
            if(heartCount==maxCount)
                return Card.Suit.HEART;
            if(diamondCount==maxCount)
                return Card.Suit.DIAMOND;
            if(clubCount==maxCount)
                return Card.Suit.CLUB;
            return Card.Suit.SPADE;
        }
        
        private static List<Card> findCards(Card.Rank rank, List<Card> cards, int size){
            int count=0;
            List<Card> cardsFound=new ArrayList(size);
            for(Card card:cards){
                if(card.rank==rank){
                    cardsFound.add(card);
                    count++;
                    if(count==size)
                        return cardsFound;
                }
            }
            return cardsFound;
        }
        
        private static Card.Rank getKickerRank(int kickerNo,int evalCode){
            return ranks[(evalCode<<(8+4*kickerNo))>>>28];
        }
        
        private static int getKickerRankIndex(int kickerNo,int evalCode){
            return (evalCode<<(8+4*kickerNo))>>>28;
        }
        
        private static List<Card> getNoPairWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            for(int ctr=1;ctr<=5;ctr++)
                winningCards.add(findCard(getKickerRank(ctr, evalCode),cards));
            return winningCards;            
        }
        
        private static List<Card> getPairWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            winningCards.addAll(findCards(getKickerRank(1, evalCode),cards,2));
            for(int ctr=2;ctr<=4;ctr++)
                winningCards.add(findCard(getKickerRank(ctr, evalCode),cards));
            return winningCards;            
        }
        
        private static List<Card> getTwoPairWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            winningCards.addAll(findCards(getKickerRank(1, evalCode),cards,2));
            winningCards.addAll(findCards(getKickerRank(2, evalCode),cards,2));
            winningCards.add(findCard(getKickerRank(3, evalCode),cards));
            return winningCards;
        }
        
        private static List<Card> getThreeOfAKindWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            winningCards.addAll(findCards(getKickerRank(1, evalCode),cards,3));
            for(int ctr=2;ctr<=3;ctr++)
                winningCards.add(findCard(getKickerRank(ctr, evalCode),cards));
            return winningCards;            
        }

        private static List<Card> getStraightWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            int rankIndex=getKickerRankIndex(1, evalCode);
            if(rankIndex>3){
                for(int ctr=0;ctr<5;ctr++)
                    winningCards.add(findCard(ranks[rankIndex-ctr],cards));
            }
            else{
                for(int ctr=0;ctr<4;ctr++)
                    winningCards.add(findCard(ranks[rankIndex-ctr],cards));
                winningCards.add(findCard(Card.Rank.ACE, cards));
            }
            return winningCards;            
        }
        
        private static List<Card> getFlushWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            Card.Suit suit=findMaxSuit(cards);
            for(int ctr=1;ctr<=5;ctr++)
                winningCards.add(findCard(getKickerRank(ctr, evalCode),suit,cards));
            return winningCards;
        }
        
        private static List<Card> getFullHouseWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            winningCards.addAll(findCards(getKickerRank(1, evalCode),cards,3));
            winningCards.addAll(findCards(getKickerRank(2, evalCode),cards,2));
            return winningCards;
        }
        
        private static List<Card> getFourOfAKindWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            winningCards.addAll(findCards(getKickerRank(1, evalCode),cards,4));
            winningCards.add(findCard(getKickerRank(2, evalCode),cards));
            return winningCards;
        }
        
        private static List<Card> getStraightFlushWinningCards(int evalCode,List<Card> cards){
            List<Card> winningCards=new ArrayList(5);
            Card.Suit suit=findMaxSuit(cards);
            int rankIndex=getKickerRankIndex(1, evalCode);
            if(rankIndex>3){
                for(int ctr=0;ctr<5;ctr++)
                    winningCards.add(findCard(ranks[rankIndex-ctr],suit,cards));
            }
            else{
                for(int ctr=0;ctr<4;ctr++)
                    winningCards.add(findCard(ranks[rankIndex-ctr],suit,cards));
                winningCards.add(findCard(Card.Rank.ACE, suit,cards));
            }
            return winningCards;            
        }
        
        public static void main(String[] args){            
            Deck deck=Deck.shuffledDeck();
            int count=0;
            List<Card> cards=new ArrayList<>(7);            
            int evalCode;
            HandCategory handCategory;
            while(count<20){
                if(deck.size()<4)
                    deck=Deck.shuffledDeck();
                cards.clear();
                for(int ctr=0;ctr<7;ctr++)
                    cards.add(deck.deal());
                evalCode=hand7Eval(encode(cards));
                handCategory=getHandCategory(evalCode);
                if(handCategory==HandCategory.STRAIGHT_FLUSH){
                    System.out.println(cards);
                    System.out.println(getWinniningCards(evalCode, cards));
                    System.out.println();
                    count++;
                }
            }
        }
        
}
