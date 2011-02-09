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

package com.deliciousdroid.platform;

import java.util.ArrayList;

import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.ContentNotFoundException;
import com.deliciousdroid.util.Md5Hash;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class BookmarkManager {
	
	public static ArrayList<Bookmark> GetBookmarks(String username, String tagname, Context context){
		return GetBookmarks(username, tagname, Bookmark.Time + " DESC", context);
	}
	
	public static ArrayList<Bookmark> GetBookmarks(String username, String tagname, String sortorder, Context context){
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, 
				Bookmark.Meta, Bookmark.Tags};
		String selection = null;
		String[] selectionargs = new String[]{username};
		
		if(tagname != null && tagname != "") {
			selection = "(" + Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
				Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
				Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
				Bookmark.Tags + " = '" + tagname + "') AND " +
				Bookmark.Account + "=?";
		} else {
			selection = Bookmark.Account + "=?";
		}
		
		Uri bookmarks = Bookmark.CONTENT_URI;
		
		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, sortorder);				
		
		if(c.moveToFirst()){
			int idColumn = c.getColumnIndex(Bookmark._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			
			do {
				
				Bookmark b = new Bookmark(c.getInt(idColumn), "", c.getString(urlColumn), 
						c.getString(descriptionColumn), "", c.getString(tagsColumn), "", 
						c.getString(metaColumn), 0);
				
				bookmarkList.add(b);
				
			} while(c.moveToNext());
				
		}
		c.close();
		return bookmarkList;
	}
	
	public static Bookmark GetById(int id, Context context) throws ContentNotFoundException {		
		String[] projection = new String[] {Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta};
		String selection = BaseColumns._ID + "=?";
		String[] selectionargs = new String[]{Integer.toString(id)};
		
		Uri bookmarks = Bookmark.CONTENT_URI;
		
		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, null);				
		
		if(c.moveToFirst()){
			int accountColumn = c.getColumnIndex(Bookmark.Account);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int notesColumn = c.getColumnIndex(Bookmark.Notes);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int hashColumn = c.getColumnIndex(Bookmark.Hash);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			int timeColumn = c.getColumnIndex(Bookmark.Time);
			
			String account = c.getString(accountColumn);
			String url = c.getString(urlColumn);
			String description = c.getString(descriptionColumn);
			String notes = c.getString(notesColumn);
			String tags = c.getString(tagsColumn);
			String hash = c.getString(hashColumn);
			String meta = c.getString(metaColumn);
			long time = c.getLong(timeColumn);
			
			c.close();
			
			return new Bookmark(id, account, url, description, notes, tags, hash, meta, time);
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static void AddBookmark(Bookmark bookmark, String account, Context context) {
		String url = bookmark.getUrl();

		if(url.lastIndexOf("/") <= 7){
			url = url + "/";
		}
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
			Log.d(url, hash);
		} else hash = bookmark.getHash();
		
		ContentValues values = new ContentValues();
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, url);
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTagString());
		values.put(Bookmark.Hash, hash);
		values.put(Bookmark.Meta, bookmark.getMeta());
		values.put(Bookmark.Time, bookmark.getTime());
		values.put(Bookmark.Account, account);
		
		context.getContentResolver().insert(Bookmark.CONTENT_URI, values);
	}
	
	public static void BulkInsert(ArrayList<Bookmark> list, String account, Context context){
		int bookmarksize = list.size();
		ContentValues[] bcv = new ContentValues[bookmarksize];
		
		for(int i = 0; i < bookmarksize; i++){
			Bookmark b = list.get(i);
			
			String url = b.getUrl();

			if(url.lastIndexOf("/") <= 7){
				url = url + "/";
			}
			
			String hash = "";
			if(b.getHash() == null || b.getHash() == ""){
				hash = Md5Hash.md5(url);
				Log.d(url, hash);
			} else hash = b.getHash();
			
			ContentValues values = new ContentValues();
			values.put(Bookmark.Description, b.getDescription());
			values.put(Bookmark.Url, url);
			values.put(Bookmark.Notes, b.getNotes());
			values.put(Bookmark.Tags, b.getTagString());
			values.put(Bookmark.Hash, hash);
			values.put(Bookmark.Meta, b.getMeta());
			values.put(Bookmark.Time, b.getTime());
			values.put(Bookmark.Account, account);
			
			bcv[i] = values;
		}
		
		context.getContentResolver().bulkInsert(Bookmark.CONTENT_URI, bcv);
	}
	
	public static void UpdateBookmark(Bookmark bookmark, String account, Context context){
		String url = bookmark.getUrl();

		if(url.lastIndexOf("/") <= 7){
			url = url + "/";
		}
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
			Log.d(url, hash);
		} else hash = bookmark.getHash();
		
		String selection = Bookmark.Hash + "=? AND " +
							Bookmark.Account + "=?";
		String[] selectionargs = new String[]{hash, account};
		
		ContentValues values = new ContentValues();
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, url);
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTagString());
		values.put(Bookmark.Meta, bookmark.getMeta());
		values.put(Bookmark.Time, bookmark.getTime());
		
		context.getContentResolver().update(Bookmark.CONTENT_URI, values, selection, selectionargs);
	}

	public static void DeleteBookmark(Bookmark bookmark, Context context){
		
		String selection = BaseColumns._ID + "=" + bookmark.getId();
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static void TruncateBookmarks(ArrayList<String> accounts, Context context, boolean inverse){
		
		ArrayList<String> selectionList = new ArrayList<String>();
		
		String operator = inverse ? "<>" : "=";
		String logicalOp = inverse ? " AND " : " OR ";
		
		for(String s : accounts) {
			selectionList.add(Bookmark.Account + " " + operator + " '" + s + "'");
		}
		
		String selection = TextUtils.join(logicalOp, selectionList);
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static void SetLastUpdate(Bookmark bookmark, Long lastUpdate, String account, Context context){
		
		String selection = Bookmark.Hash + "=? AND " +
							Bookmark.Account + "=?";
		String[] selectionargs = new String[]{bookmark.getHash(), account};
		
		ContentValues values = new ContentValues();	
		values.put(Bookmark.LastUpdate, lastUpdate);
		
		context.getContentResolver().update(Bookmark.CONTENT_URI, values, selection, selectionargs);
	}
	
	public static void DeleteOldBookmarks(Long lastUpdate, String account, Context context){
		String selection = "(" + Bookmark.LastUpdate + "<? OR " +
			Bookmark.LastUpdate + " is null) AND " +
			Bookmark.Account + "=?";
		String[] selectionargs = new String[]{Long.toString(lastUpdate), account};

		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, selectionargs);
	}
	
	public static ArrayList<Bookmark> SearchBookmarks(String query, String tagname, String username, Context context) {
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, 
				Bookmark.Meta, Bookmark.Tags};
		String selection = null;
		String[] selectionargs = new String[]{username};
		String sortorder = null;
		
		String[] queryBookmarks = query.split(" ");
		
		ArrayList<String> queryList = new ArrayList<String>();
		
		if(query != null && query != "" && (tagname == null || tagname == "")) {
			for(String s : queryBookmarks) {
				queryList.add("(" + Bookmark.Tags + " LIKE '%" + s + "%' OR " +
						Bookmark.Description + " LIKE '%" + s + "%' OR " +
						Bookmark.Notes + " LIKE '%" + s + "%')");
			}
			
			selection = TextUtils.join(" AND ", queryList) + " AND " +
				Bookmark.Account + "=?";
		} else if(query != null && query != ""){
			for(String s : queryBookmarks) {
				queryList.add("(" + Bookmark.Description + " LIKE '%" + s + "%' OR " +
						Bookmark.Notes + " LIKE '%" + s + "%')");
			}

			selection = TextUtils.join(" AND ", queryList) +
				" AND " + Bookmark.Account + "=? AND " +
				"(" + Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
				Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
				Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
				Bookmark.Tags + " = '" + tagname + "')";
		} else {
			selection = Bookmark.Account + "=?";
		}
		
		sortorder = Bookmark.Description + " ASC";
		
		Uri bookmarks = Bookmark.CONTENT_URI;
		
		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, sortorder);				
		
		if(c.moveToFirst()){
			int idColumn = c.getColumnIndex(Bookmark._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			
			do {
				
				Bookmark b = new Bookmark(c.getInt(idColumn), "", c.getString(urlColumn), 
						c.getString(descriptionColumn), "", c.getString(tagsColumn), "", 
						c.getString(metaColumn), 0);
				
				bookmarkList.add(b);
				
			} while(c.moveToNext());
				
		}
		c.close();
		return bookmarkList;
	}
}