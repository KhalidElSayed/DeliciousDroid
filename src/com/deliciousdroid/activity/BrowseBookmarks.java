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

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.deliciousdroid.fragment.AddBookmarkFragment;
import com.deliciousdroid.fragment.BrowseBookmarkFeedFragment;
import com.deliciousdroid.fragment.BrowseBookmarksFragment;
import com.deliciousdroid.fragment.BrowseTagsFragment;
import com.deliciousdroid.fragment.ViewBookmarkFragment;
import com.deliciousdroid.fragment.AddBookmarkFragment.OnBookmarkSaveListener;
import com.deliciousdroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.deliciousdroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.deliciousdroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.deliciousdroid.Constants;
import com.deliciousdroid.Constants.BookmarkViewType;
import com.deliciousdroid.R;
import com.deliciousdroid.action.IntentHelper;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;

public class BrowseBookmarks extends FragmentBaseActivity implements OnBookmarkSelectedListener, 
	OnBookmarkActionListener, OnBookmarkSaveListener, OnTagSelectedListener {

	private String query = "";
	private String tagname = "";
	private Boolean unread = false;
	private String path = "";
	private Bookmark lastSelected = null;
	private BookmarkViewType lastViewType = null;
	
	static final String STATE_LASTBOOKMARK = "lastBookmark";
	static final String STATE_LASTVIEWTYPE = "lastViewType";
	static final String STATE_USERNAME = "username";
	static final String STATE_TAGNAME = "tagname";
	static final String STATE_UNREAD = "unread";
	static final String STATE_QUERY = "query";
	static final String STATE_PATH = "path";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		Intent intent = getIntent();

		Uri data = intent.getData();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction t = fm.beginTransaction();
		
		Fragment bookmarkFrag;
		
		if(fm.findFragmentById(R.id.listcontent) == null){
			if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    		Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
	    		
	    		if(searchData != null) {
	    			tagname = searchData.getString("tagname");
	    			username = searchData.getString("username");
	    			unread = searchData.getBoolean("unread");
	    		}
	    		
	    		query = intent.getStringExtra(SearchManager.QUERY);
	    		
	    		if(intent.hasExtra("username")) {
	    			username = intent.getStringExtra("username");
	    		}
	    		
	    		if(data != null && data.getUserInfo() != null){
	    			username = data.getUserInfo();
	    		}
			} else {
				if(data != null) {
					if(data.getUserInfo() != "") {
						username = data.getUserInfo();
					} else username = mAccount.name;
					tagname = data.getQueryParameter("tagname");
					unread = data.getQueryParameter("unread") != null;
					path = data.getPath();
				}
			}
			
			if(isMyself()) {
				bookmarkFrag = new BrowseBookmarksFragment();
			} else {
				bookmarkFrag = new BrowseBookmarkFeedFragment();
			}

			t.add(R.id.listcontent, bookmarkFrag);
		} else {
			if(savedInstanceState != null){
			    username = savedInstanceState.getString(STATE_USERNAME);
			    tagname = savedInstanceState.getString(STATE_TAGNAME);
			    unread = savedInstanceState.getBoolean(STATE_UNREAD);
			    query = savedInstanceState.getString(STATE_QUERY);
			    path = savedInstanceState.getString(STATE_PATH);
			}
			
			bookmarkFrag = fm.findFragmentById(R.id.listcontent);
		}
		
		if(isMyself()){
			if(query != null && !query.equals("")){
				((BrowseBookmarksFragment) bookmarkFrag).setSearchQuery(query, username, tagname, unread);
			} else {
				((BrowseBookmarksFragment) bookmarkFrag).setQuery(username, tagname, unread);
			} 
		} else {
			if(query != null && !query.equals("")){
				((BrowseBookmarkFeedFragment) bookmarkFrag).setQuery(username, tagname);
			} else {
				((BrowseBookmarkFeedFragment) bookmarkFrag).setQuery(username, query);
			}
		}
		
		BrowseTagsFragment tagFrag = (BrowseTagsFragment) fm.findFragmentById(R.id.tagcontent);
		if(tagFrag != null){
			tagFrag.setAccount(username);
		}
		
		if(path != null && path.contains("tags")){
			t.hide(fm.findFragmentById(R.id.maincontent));
			findViewById(R.id.panel_collapse_button).setVisibility(View.GONE);
		} else{
			if(tagFrag != null){
				t.hide(tagFrag);
			}
		}
		
		Fragment addFrag = fm.findFragmentById(R.id.addcontent);
		if(addFrag != null){
			t.hide(addFrag);
		}

		t.commit();
    }
	
	@Override
	public boolean onSearchRequested() {
		if(isMyself()) {
			Bundle contextData = new Bundle();
			contextData.putString("tagname", tagname);
			contextData.putString("username", username);
			contextData.putBoolean("unread", unread);
			startSearch(null, false, contextData, false);
		} else {
			startSearch(null, false, Bundle.EMPTY, false);
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    setupSearch(menu);
	    return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if(lastSelected != null && lastViewType != null){
			savedInstanceState.putSerializable(STATE_LASTBOOKMARK, lastSelected);
	    	savedInstanceState.putSerializable(STATE_LASTVIEWTYPE, lastViewType);
		}
		
		savedInstanceState.putString(STATE_USERNAME, username);
		savedInstanceState.putString(STATE_TAGNAME, tagname);
		savedInstanceState.putBoolean(STATE_UNREAD, unread);
		savedInstanceState.putString(STATE_QUERY, query);

	    super.onSaveInstanceState(savedInstanceState);
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    if(findViewById(R.id.maincontent) != null) {
	    	lastSelected = (Bookmark)savedInstanceState.getSerializable(STATE_LASTBOOKMARK);
	    	lastViewType = (BookmarkViewType)savedInstanceState.getSerializable(STATE_LASTVIEWTYPE);
	    	setBookmarkView(lastSelected, lastViewType);
	    }
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		
		Fragment tagFrag = getSupportFragmentManager().findFragmentById(R.id.tagcontent);
		View panelBtn = findViewById(R.id.panel_collapse_button);
		
		if(tagFrag != null && panelBtn != null){
			if(tagFrag.isVisible())
				findViewById(R.id.panel_collapse_button).setVisibility(View.GONE);
			else findViewById(R.id.panel_collapse_button).setVisibility(View.VISIBLE);
		}
	}

	public void onBookmarkView(Bookmark b) {
		if(b != null){
			if(findViewById(R.id.maincontent) != null || findViewById(R.id.tagcontent) != null) {
				lastSelected = b;
				lastViewType = BookmarkViewType.VIEW;
				setBookmarkView(b, BookmarkViewType.VIEW);
			} else {
				startActivity(IntentHelper.ViewBookmark(b, BookmarkViewType.VIEW, username, this));
			}
		}
	}

	public void onBookmarkRead(Bookmark b) {
		if(b != null){
			if(findViewById(R.id.maincontent) != null) {
				lastSelected = b;
				lastViewType = BookmarkViewType.READ;
				setBookmarkView(b, BookmarkViewType.READ);
			} else {
				startActivity(IntentHelper.ViewBookmark(b, BookmarkViewType.READ, username, this));
			}
		}
	}

	public void onBookmarkOpen(Bookmark b) {
		if(b != null){
			if(findViewById(R.id.maincontent) != null) {
				lastSelected = b;
				lastViewType = BookmarkViewType.WEB;
				setBookmarkView(b, BookmarkViewType.WEB);
			} else {
				startActivity(IntentHelper.OpenInBrowser(b.getUrl()));
			}
		}
	}

	public void onBookmarkAdd(Bookmark b) {
		if(b != null){
			startActivity(IntentHelper.AddBookmark(b.getUrl(), mAccount.name, this));
		}
	}

	public void onBookmarkShare(Bookmark b) {
		if(b != null){
			Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
			startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
		}
	}

	public void onBookmarkEdit(Bookmark b) {		
		if(b != null){
			if(findViewById(R.id.maincontent) != null) {
				AddBookmarkFragment addFrag = (AddBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.addcontent);
				addFrag.loadBookmark(b, null);
				addFrag.refreshView();
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				if(getSupportFragmentManager().findFragmentById(R.id.tagcontent).isVisible()){
					transaction.hide(getSupportFragmentManager().findFragmentById(R.id.tagcontent));
					transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
					transaction.addToBackStack(null);
				}
				transaction.show(getSupportFragmentManager().findFragmentById(R.id.addcontent));
				transaction.commit();
				transaction = getSupportFragmentManager().beginTransaction();
				transaction.hide(getSupportFragmentManager().findFragmentById(R.id.maincontent));
				transaction.commit();
			} else {
				startActivity(IntentHelper.EditBookmark(b, mAccount.name, this));
			}
		}
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, mAccount.name, this);
	}

	public void onViewTagSelected(String tag) {
		if(findViewById(R.id.maincontent) != null) {
			BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
			frag.setQuery(username, tag, false);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(tag, username, this));
		}
	}

	public void onUserTagSelected(String tag, String user) {
		if(findViewById(R.id.maincontent) != null) {
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
			frag.setQuery(user, tag);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(tag, user, this));
		}
	}

	public void onAccountSelected(String account) {
		if(findViewById(R.id.maincontent) != null) {
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
			frag.setQuery(account, null);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(null, account, this));
		}
	}

	public void onBookmarkSave(Bookmark b) {
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(getSupportFragmentManager().findFragmentById(R.id.addcontent));
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.commit();
		}
		
		onBookmarkView(b);
	}

	public void onBookmarkCancel(Bookmark b) {
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(getSupportFragmentManager().findFragmentById(R.id.addcontent));
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.commit();
		}
		
		onBookmarkView(b);
	}

	public void onTagSelected(String tag) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(username, tag, false);
		transaction.replace(R.id.listcontent, frag);
		transaction.commit();
	}
	
	private void setBookmarkView(Bookmark b, BookmarkViewType viewType){
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden() && getSupportFragmentManager().findFragmentById(R.id.addcontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			if(getSupportFragmentManager().findFragmentById(R.id.tagcontent).isVisible()){
				transaction.hide(getSupportFragmentManager().findFragmentById(R.id.tagcontent));
				findViewById(R.id.panel_collapse_button).setVisibility(View.VISIBLE);
			}
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.addToBackStack(null);
			transaction.commit();
		} else if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden() && getSupportFragmentManager().findFragmentById(R.id.addcontent).isVisible()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(getSupportFragmentManager().findFragmentById(R.id.addcontent));
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.commit();
		}
		
		ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.maincontent);
		viewFrag.setBookmark(b, viewType);
		viewFrag.loadBookmark();
	}
	
	public void collapsePanel(View v) {
		
		if(findViewById(R.id.listcontent) != null){
			View bookmarkList = findViewById(R.id.listcontent);
			
			if(bookmarkList.getVisibility() == View.VISIBLE)
				bookmarkList.setVisibility(View.GONE);
			else bookmarkList.setVisibility(View.VISIBLE);
		}
	}
	
	public void saveHandler(View v) {
		FragmentManager fm = getSupportFragmentManager();
		AddBookmarkFragment addFrag = (AddBookmarkFragment)fm.findFragmentById(R.id.addcontent);
		
		if(addFrag != null){
			addFrag.saveHandler(v);
		}
	}
	
	public void cancelHandler(View v) {
		FragmentManager fm = getSupportFragmentManager();
		AddBookmarkFragment addFrag = (AddBookmarkFragment)fm.findFragmentById(R.id.addcontent);
		
		if(addFrag != null) {
			addFrag.cancelHandler(v);
		}
	}
}