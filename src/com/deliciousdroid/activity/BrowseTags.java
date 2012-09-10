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

import com.deliciousdroid.fragment.BrowseTagsFragment;
import com.deliciousdroid.R;
import com.deliciousdroid.action.IntentHelper;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class BrowseTags extends FragmentBaseActivity implements BrowseTagsFragment.OnTagSelectedListener {
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_tags);

        Intent intent = getIntent();
        
        Uri data = intent.getData();
        String action = intent.getAction();

		if(data != null)
			username = data.getUserInfo();
        
		BrowseTagsFragment frag = (BrowseTagsFragment) getSupportFragmentManager().findFragmentById(R.id.listcontent);
        frag.setAccount(username);
		
		if(Intent.ACTION_VIEW.equals(action)) {
			setTitle(getString(R.string.browse_my_tags_title));
		} else if(Intent.ACTION_SEARCH.equals(action)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			frag.setQuery(query);
			setTitle(getString(R.string.tag_search_results_title, query));
		}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    setupSearch(menu);
	    return true;
	}

	public void onTagSelected(String tag) {		
		startActivity(IntentHelper.ViewBookmarks(tag, username, this));
	}
}