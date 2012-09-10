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
	
	public long getLastUpdate(){
		return lastUpdate;
	}
	
	public Update(long update){
		lastUpdate = update;
	}
	
	public static Update valueOf(String updateResponse){
        try {
        	int start = updateResponse.indexOf("<update");
        	int end = updateResponse.indexOf("/>", start);
        	String updateElement = updateResponse.substring(start, end);
        	int timestart = updateElement.indexOf("time=");
        	int timeend = updateElement.indexOf("\"", timestart + 7);
        	String time = updateElement.substring(timestart + 6, timeend);

			long updateTime = DateParser.parse(time).getTime();
			
			return new Update(updateTime);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
