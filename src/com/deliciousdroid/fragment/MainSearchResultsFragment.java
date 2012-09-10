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

package com.deliciousdroid.fragment;

import com.deliciousdroid.R;
import com.deliciousdroid.activity.BrowseBookmarks;
import com.deliciousdroid.activity.FragmentBaseActivity;
import com.deliciousdroid.activity.ViewBookmark;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainSearchResultsFragment extends ListFragment {
	
	private FragmentBaseActivity base;
	
	private OnSearchActionListener searchActionListener;
	
	public interface OnSearchActionListener {
		public void onBookmarkSearch();
		public void onTagSearch();
		public void onGlobalTagSearch();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		base.setTitle(R.string.main_search_results_title);
		
		String[] MENU_ITEMS = new String[] {getString(R.string.search_results_bookmark),
				getString(R.string.search_results_tag), getString(R.string.search_results_global_tag)};
		
		setListAdapter(new ArrayAdapter<String>(base, R.layout.main_view, MENU_ITEMS));

		final Intent intent = base.getIntent();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			if(intent.hasExtra(SearchManager.QUERY)){
				Intent i = new Intent(base, MainSearchResultsFragment.class);
				i.putExtras(intent.getExtras());
				startActivity(i);
				base.finish();
			} else {
				base.onSearchRequested();
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
				base.finish();				
			} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
				Intent viewBookmark = new Intent(base, ViewBookmark.class);
				viewBookmark.setData(data);
				
				Log.d("View Bookmark Uri", data.toString());
				startActivity(viewBookmark);
				base.finish();
			} else if(tagname != null) {
				Intent viewTags = new Intent(base, BrowseBookmarks.class);
				viewTags.setData(data);
				
				Log.d("View Tags Uri", data.toString());
				startActivity(viewTags);
				base.finish();
			}
		} 
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if(position == 0){
		    		searchActionListener.onBookmarkSearch();
		    	} else if(position == 1){
		    		searchActionListener.onTagSearch();
		    	} else if(position == 2){
		    		searchActionListener.onGlobalTagSearch();
		    	}
		    }
		});
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.main_search_fragment, container, false);
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			searchActionListener = (OnSearchActionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSearchActionListener");
		}
	}
}