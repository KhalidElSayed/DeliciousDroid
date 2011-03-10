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

import com.deliciousdroid.R;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

public class MainSearchResults extends AppBaseListActivity {

	private Context mContext;
	
	static final String[] MENU_ITEMS = new String[] {"Bookmark Results", "Tag Results"};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_view, MENU_ITEMS));
		mContext = this;

		final Intent intent = getIntent();
		setContentView(R.layout.main_search_results);
		setTitle("Search Results");
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			if(intent.hasExtra(SearchManager.QUERY)){
				Intent i = new Intent(mContext, MainSearchResults.class);
				i.putExtras(intent.getExtras());
				startActivity(i);
				finish();
			} else {
				onSearchRequested();
			}
		} else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri data = intent.getData();
			String path = null;
			String tagname = null;

			if(data != null) {
				path = data.getPath();
				tagname = data.getQueryParameter("tagname");
			}

			if(data.getScheme() == null || !data.getScheme().equals("content")){
				Intent i = new Intent(Intent.ACTION_VIEW, data);

				startActivity(i);
				finish();        
			} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
				Intent viewBookmark = new Intent(this, ViewBookmark.class);
				viewBookmark.setData(data);

				Log.d("View Bookmark Uri", data.toString());
				startActivity(viewBookmark);
				finish();
			} else if(tagname != null) {
				Intent viewTags = new Intent(this, BrowseBookmarks.class);
				viewTags.setData(data);

				Log.d("View Tags Uri", data.toString());
				startActivity(viewTags);
				finish();
			}
		} 
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if(position == 0){
		    		
		    		Intent i = new Intent(mContext, BrowseBookmarks.class);
		    		i.setAction(Intent.ACTION_SEARCH);
		    		i.putExtras(intent.getExtras());
		    		
		    		startActivity(i);
		    	} else if(position == 1){
		    		
		    		Intent i = new Intent(mContext, BrowseTags.class);
		    		i.setAction(Intent.ACTION_SEARCH);
		    		i.putExtras(intent.getExtras());
		    		
		    		startActivity(i);
		    	}
		    }
		});
	}
}