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

package com.deliciousdroid.client;

import com.deliciousdroid.util.DateParser;

public class Update {
	private long lastUpdate;
	private int inboxNew;
	
	public long getLastUpdate(){
		return lastUpdate;
	}
	
	public int getInboxNew(){
		return inboxNew;
	}
	
	public Update(long update, int inbox){
		lastUpdate = update;
		inboxNew = inbox;
	}
	
	public static Update valueOf(String updateResponse){
        try {
        	int start = updateResponse.indexOf("<update");
        	int end = updateResponse.indexOf("/>", start);
        	String updateElement = updateResponse.substring(start, end);
        	int timestart = updateElement.indexOf("time=");
        	int timeend = updateElement.indexOf("\"", timestart + 7);
        	String time = updateElement.substring(timestart + 6, timeend);

			long updateTime = DateParser.parseTime(time);
			
			int inboxstart = updateElement.indexOf("inboxnew");
			int inboxend = updateElement.indexOf("\"", inboxstart + 10);
			//int inbox = Integer.parseInt(updateElement.substring(inboxstart + 10, inboxend));
			int inbox = 0;
			
			return new Update(updateTime, inbox);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
