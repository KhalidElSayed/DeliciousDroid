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

package com.deliciousdroid.activity;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;
import com.deliciousdroid.action.BookmarkTaskArgs;
import com.deliciousdroid.action.DeleteBookmarkTask;
import com.deliciousdroid.client.DeliciousFeed;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.providers.BookmarkContentProvider;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;

public class BrowseBookmarks extends AppBaseListActivity {
	
	private ListView lv;
	
	private final int sortDateAsc = 9999991;
	private final int sortDateDesc = 9999992;
	private final int sortDescAsc = 9999993;
	private final int sortDescDesc = 9999994;
	private final int sortUrlAsc = 9999995;
	private final int sortUrlDesc = 9999996;
	
	private String sortfield = Bookmark.Time + " DESC";
	
	private String tagname = null;
	private String bundlename = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		if(mAccount != null) {		
			Intent intent = getIntent();
	
			Uri data = intent.getData();
			String path = null;
			
			if(data != null) {
				if(data.getUserInfo() != "") {
					username = data.getUserInfo();
				} else username = mAccount.name;
				
				path = data.getPath();
				tagname = data.getQueryParameter("tagname");
				bundlename = data.getQueryParameter("bundlename");
			}
			
	    	if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    		Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
	
	    		if(searchData != null) {
	    			tagname = searchData.getString("tagname");
	    			username = searchData.getString("username");
	    		}
	    		
	    		String query = intent.getStringExtra(SearchManager.QUERY);
	    		
	    		setTitle("Bookmark Search Results For \"" + query + "\"");
	    		
	    		if(isMyself()) {
	    			Cursor c = BookmarkManager.SearchBookmarks(query, tagname, username, this);
	    			startManagingCursor(c);

	    			SimpleCursorAdapter a = new SimpleCursorAdapter(mContext, R.layout.bookmark_view, c, 
	    				new String[]{Bookmark.Description, Bookmark.Tags}, 
	    				new int[]{R.id.bookmark_description, R.id.bookmark_tags});
	    		
	    			setListAdapter(a);
	    		}
	    		
	    	} else if(!data.getScheme().equals("content")) {
	    		
	    		openBookmarkInBrowser(new Bookmark(data.toString()));
	    		finish();
	    		
	    	} else if(path.equals("/bookmarks") && isMyself()) {
	    		
				if(tagname != null && tagname != "") {
					if(bundlename != null && bundlename != "")
						setTitle("My Bookmarks In " + bundlename);
					else setTitle("My Bookmarks Tagged With " + tagname);
				} else {
					setTitle("My Bookmarks");
				}
				
				loadBookmarkList();
			} else if(username.equals("network")){
				try{
					setTitle("My Network's Recent Bookmarks");
					
					new LoadBookmarkFeedTask().execute("network");
				}
				catch(Exception e){}
			} else if(username.equals("hotlist")){
				try{
					setTitle("Hotlist Bookmarks");
					
					new LoadBookmarkFeedTask().execute("hotlist");
				}
				catch(Exception e){}
			} else if(username.equals("popular")){
				try{
					setTitle("Popular Bookmarks");
					
					new LoadBookmarkFeedTask().execute("popular");
				}
				catch(Exception e){}
			} else if(path.equals("/bookmarks")) {
				try{
					if(tagname != null && tagname != "") {
						setTitle("Bookmarks For " + username + " Tagged With " + tagname);
					} else {
						setTitle("Bookmarks For " + username);
					}
			    	
					new LoadBookmarkFeedTask().execute(username, tagname);
				}
				catch(Exception e){}
			} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
				viewBookmark(Integer.parseInt(data.getLastPathSegment()));
				finish();
			}
			
			lv = getListView();
			lv.setTextFilterEnabled(true);
			lv.setFastScrollEnabled(true);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Cursor c = (Cursor)lv.getItemAtPosition(position);
					Bookmark b = BookmarkManager.CursorToBookmark(c);
	
			    	if(defaultAction.equals("view")) {
			    		viewBookmark(b);
			    	} else {
			    		openBookmarkInBrowser(b);
			    	}
			    }
			});
			
			/* Add Context-Menu listener to the ListView. */
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Actions");
					if(isMyself()){
						menu.add(Menu.NONE, 0, Menu.NONE, "Open in browser");
						menu.add(Menu.NONE, 1, Menu.NONE, "View Details");
						menu.add(Menu.NONE, 2, Menu.NONE, "Edit");
						menu.add(Menu.NONE, 3, Menu.NONE, "Delete");
					} else {
						menu.add(Menu.NONE, 0, Menu.NONE, "Open in browser");
						menu.add(Menu.NONE, 1, Menu.NONE, "View Details");
						menu.add(Menu.NONE, 4, Menu.NONE, "Add");
					}
				}
			});
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Cursor c = (Cursor)lv.getItemAtPosition(menuInfo.position);
		Bookmark b = BookmarkManager.CursorToBookmark(c);
		
		switch (aItem.getItemId()) {
			case 0:
				openBookmarkInBrowser(b);
				return true;
			case 1:				
				viewBookmark(b);
				return true;
			case 2:
				Intent editBookmark = new Intent(this, AddBookmark.class);
				editBookmark.setAction(Intent.ACTION_EDIT);
				
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
				data.appendEncodedPath("bookmarks");
				data.appendEncodedPath(Integer.toString(b.getId()));
				editBookmark.setData(data.build());

				startActivity(editBookmark);
				return true;
			
			case 3:
				BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, mContext);	
				new DeleteBookmarkTask().execute(args);
				return true;
				
			case 4:				
				Intent addBookmark = new Intent(this, AddBookmark.class);
				addBookmark.setAction(Intent.ACTION_SEND);
				addBookmark.putExtra(Intent.EXTRA_TEXT, b.getUrl());
				startActivity(addBookmark);
				return true;
		}
		return false;
	}
	
	@Override
	public boolean onSearchRequested() {
		
		if(isMyself()) {
			Bundle contextData = new Bundle();
			contextData.putString("tagname", tagname);
			contextData.putString("username", username);
			startSearch(null, false, contextData, false);
			return true;
		} else return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		if(result && isMyself()) {
		    SubMenu sortmenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 1, R.string.menu_sort_title);
		    sortmenu.setIcon(R.drawable.ic_menu_sort_alphabetically);
		    sortmenu.add(Menu.NONE, sortDateAsc, 0, "Date (Oldest First)");
		    sortmenu.add(Menu.NONE, sortDateDesc, 1, "Date (Newest First)");
		    sortmenu.add(Menu.NONE, sortDescAsc, 2, "Description (A-Z)");
		    sortmenu.add(Menu.NONE, sortDescDesc, 3, "Description (Z-A)");
		    sortmenu.add(Menu.NONE, sortUrlAsc, 4, "Url (A-Z)");
		    sortmenu.add(Menu.NONE, sortUrlDesc, 5, "Url (Z-A)");
		}
		
	    return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean result = false;
		
	    switch (item.getItemId()) {
		    case sortDateAsc:
		    	sortfield = Bookmark.Time + " ASC";
				result = true;
				break;
		    case sortDateDesc:			
		    	sortfield = Bookmark.Time + " DESC";
		    	result = true;
		    	break;
		    case sortDescAsc:			
		    	sortfield = Bookmark.Description + " ASC";
		    	result = true;
		    	break;
		    case sortDescDesc:			
		    	sortfield = Bookmark.Description + " DESC";
		    	result = true;
		    	break;
		    case sortUrlAsc:			
		    	sortfield = Bookmark.Url + " ASC";
		    	result = true;
		    	break;
		    case sortUrlDesc:			
		    	sortfield = Bookmark.Url + " DESC";
		    	result = true;
		    	break;
	    }
	    
	    if(result) {
	    	loadBookmarkList();
	    } else result = super.onOptionsItemSelected(item);
	    
	    return result;
	}
	
	private void loadBookmarkList() {
		Cursor c = BookmarkManager.GetBookmarks(username, tagname, sortfield, this);
		startManagingCursor(c);
		
		SimpleCursorAdapter a = new SimpleCursorAdapter(mContext, R.layout.bookmark_view, c, 
				new String[]{Bookmark.Description, Bookmark.Tags}, 
				new int[]{R.id.bookmark_description, R.id.bookmark_tags});
		
		setListAdapter(a);
	}
	
	private void openBookmarkInBrowser(Bookmark b) {
    	String url = b.getUrl();
    	Uri link = Uri.parse(url);
		Intent i = new Intent(Intent.ACTION_VIEW, link);
		
		startActivity(i);
	}
	
	private void viewBookmark(int id) {
		Bookmark b = new Bookmark(id);
		viewBookmark(b);
	}
	
	private void viewBookmark(Bookmark b) {
		Intent viewBookmark = new Intent();
		viewBookmark.setAction(Intent.ACTION_VIEW);
		viewBookmark.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
		data.appendEncodedPath("bookmarks");
		
		if(isMyself()) {
			data.appendEncodedPath(Integer.toString(b.getId()));
		} else {
			data.appendEncodedPath(Integer.toString(0));
			data.appendQueryParameter("url", b.getUrl());
			data.appendQueryParameter("title", b.getDescription());
			data.appendQueryParameter("notes", b.getNotes());
			data.appendQueryParameter("tags", b.getTagString());
			data.appendQueryParameter("time", Long.toString(b.getTime()));
			data.appendQueryParameter("account", b.getAccount());
		}
		viewBookmark.setData(data.build());
		
		Log.d("View Bookmark Uri", data.build().toString());
		startActivity(viewBookmark);
	}
	
    public class LoadBookmarkFeedTask extends AsyncTask<String, Integer, Boolean>{
    	private String user;
    	private String tag = null;
    	private ProgressDialog progress;
    	private Cursor c;
    	
    	protected void onPreExecute() {
    		progress = new ProgressDialog(mContext);
    		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		progress.setMessage("Loading. Please wait...");
    		progress.setCancelable(true);
    		progress.show();
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... args) {
    		user = args[0];
    		
    		if(args.length > 1)
    			tag = args[1];
    		
    		boolean result = false;
    		
			try {
				if(user.equals("network")) {
					c = DeliciousFeed.fetchNetworkRecent(mAccount.name, Integer.parseInt(bookmarkLimit));
				} else if(user.equals("hotlist")) {
					c = DeliciousFeed.fetchHotlist(Integer.parseInt(bookmarkLimit));
				} else if(user.equals("popular")) {
					c = DeliciousFeed.fetchPopular(Integer.parseInt(bookmarkLimit));
				}  else {
					c = DeliciousFeed.fetchFriendBookmarks(user, tag, Integer.parseInt(bookmarkLimit));
				}
				result = true;
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
    		return result;
    	}
    	
        protected void onPostExecute(Boolean result) {
        	progress.dismiss();
        	
        	if(result) {
        		startManagingCursor(c);
        		
        		SimpleCursorAdapter a = new SimpleCursorAdapter(mContext, R.layout.bookmark_view, c, 
	        		new String[]{Bookmark.Description, Bookmark.Tags}, 
	        		new int[]{R.id.bookmark_description, R.id.bookmark_tags});
        		
        		setListAdapter(a);
        	}
        }
    }
}