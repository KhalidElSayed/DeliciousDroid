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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.deliciousdroid.R;
import com.deliciousdroid.activity.FragmentBaseActivity;

public class MainFragment extends ListFragment {

	private FragmentBaseActivity base;
	
	private OnMainActionListener mainActionListener;
	
	public interface OnMainActionListener {
		public void onMyBookmarksSelected();
		public void onMyBundlesSelected();
		public void onMyTagsSelected();
		public void onMyNetworkSelected();
		public void onRecentSelected();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		setHasOptionsMenu(true);
		
		String[] MENU_ITEMS = new String[] {getString(R.string.main_menu_my_bookmarks),
				getString(R.string.main_menu_my_tags),
				getString(R.string.main_menu_my_bundles),
				getString(R.string.main_menu_recent_bookmarks),
				getString(R.string.main_menu_network_bookmarks)};
		
		setListAdapter(new ArrayAdapter<String>(base, R.layout.main_view, MENU_ITEMS));
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if(position == 0){
		    		mainActionListener.onMyBookmarksSelected();
		    	} else if(position == 1){
		    		mainActionListener.onMyTagsSelected();
		    	} else if(position == 2){
		    		mainActionListener.onMyBundlesSelected();
		    	} else if(position == 3){
		    		mainActionListener.onRecentSelected();
		    	} else if(position == 4){
		    		mainActionListener.onMyNetworkSelected();
		    	} 
		    }
		});
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main_menu, menu);
		
		base.setupSearch(menu);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.main_fragment, container, false);
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mainActionListener = (OnMainActionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnMainActionListener");
		}
	}
}