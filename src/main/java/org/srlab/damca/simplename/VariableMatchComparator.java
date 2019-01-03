package org.srlab.damca.simplename;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VariableMatchComparator implements Comparator<Object>{
	private String varName;

	VariableMatchComparator(String _varName) {
		this.varName = _varName;
	}

	public int compare(Object o1, Object o2) {
		VariableEntity v1 = (VariableEntity) o1;
		VariableEntity v2 = (VariableEntity) o2;
		return score(v2) - score(v1);
	}

	/**
	 * The four order criteria as described below - put already used into bit 10,
	 * all others into bits 0-9, 11-20, 21-30; 31 is sign - always 0
	 * 
	 * @param v
	 * @return the score for <code>v</code>
	 */
	private int score(VariableEntity v) {
		int variableScore = 100 - v.getEntityCategory().ordinal(); // since these are increasing with distance
		int subStringScore = getLongestCommonSubstring(v.getName(),this.varName).length();
		// substring scores under 60% are not considered
		// this prevents marginal matches like a - ba and false - isBool that will
		// destroy the sort order
		int shorter = Math.min(v.getName().length(), varName.length());
		if (subStringScore < 0.6 * shorter)
			subStringScore = 0;

		int positionScore = v.getPositionScore(); // since ???
		int matchedScore = v.isAlreadyMatched() ? 0 : 1;
		int autoboxingScore = v.isAutoboxingMatch() ? 0 : 1;

		int score = autoboxingScore << 30 | variableScore << 21 | subStringScore << 11 | matchedScore << 10
				| positionScore;
		return score;
	}

	/**
	 * Determine the best match of all possible type matches. The input into this
	 * method is all possible completions that match the type of the argument. The
	 * purpose of this method is to choose among them based on the following simple
	 * rules:
	 *
	 * 1) Local Variables > Instance/Class Variables > Inherited Instance/Class
	 * Variables
	 *
	 * 2) A longer case insensitive substring match will prevail
	 *
	 * 3) Variables that have not been used already during this completion will
	 * prevail over those that have already been used (this avoids the same
	 * String/int/char from being passed in for multiple arguments)
	 *
	 * 4) A better source position score will prevail (the declaration point of the
	 * variable, or "how close to the point of completion?"
	 *
	 * @param typeMatches
	 *            the list of type matches
	 * @param paramName
	 *            the parameter name
	 */
	private static void orderMatches(List typeMatches, String varName) {
		if (typeMatches != null)
			Collections.sort(typeMatches, new VariableMatchComparator(varName));
	}

	/**
	 * Returns the longest common substring of two strings.
	 *
	 * @param first
	 *            the first string
	 * @param second
	 *            the second string
	 * @return the longest common substring
	 */
	private static String getLongestCommonSubstring(String first, String second) {
		System.out.println("First "+first+" second"+second);
		String shorter = (first.length() <= second.length()) ? first : second;
		String longer = shorter == first ? second : first;

		int minLength = shorter.length();

		StringBuffer pattern = new StringBuffer(shorter.length() + 2);
		String longestCommonSubstring = ""; //$NON-NLS-1$

		for (int i = 0; i < minLength; i++) {
			for (int j = i + 1; j <= minLength; j++) {
				if (j - i < longestCommonSubstring.length())
					continue;

				String substring = shorter.substring(i, j);
				pattern.setLength(0);
				pattern.append('*');
				pattern.append(substring);
				pattern.append('*');

				StringMatcher matcher = new StringMatcher(pattern.toString(), true, false);
				if (matcher.match(longer))
					longestCommonSubstring = substring;
			}
		}

		return longestCommonSubstring;
	}
}
