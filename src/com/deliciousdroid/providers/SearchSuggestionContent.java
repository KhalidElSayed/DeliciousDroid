/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.providers;

import java.util.Comparator;

public class SearchSuggestionContent {

	private String text1;
	private String text2;
	private String text2Url;
	private int icon1;
	private int icon2;
	private String intentData;
	
	public String getText1() {
		return text1;
	}
	
	public String getText2() {
		return text2;
	}
	
	public String getText2Url() {
		return text2Url;
	}
	
	public int getIcon1() {
		return icon1;
	}
	
	public int getIcon2() {
		return icon2;
	}
	
	public String getIntentData() {
		return intentData;
	}
	
	public SearchSuggestionContent(String t1, String t2, int i1, int i2, String data) {
		text1 = t1;
		text2 = t2;
		icon1 = i1;
		icon2 = i2;
		intentData = data;
	}
	
	public SearchSuggestionContent(String t1, String t2, String t2u, int i1, int i2, String data) {
		text1 = t1;
		text2 = t2;
		text2Url = t2u;
		icon1 = i1;
		icon2 = i2;
		intentData = data;
	}
	
	public static class Comparer implements Comparator<SearchSuggestionContent> {

		public int compare(SearchSuggestionContent a, SearchSuggestionContent b) {
			return a.text1.compareToIgnoreCase(b.text1);
		}
		
	}
}
