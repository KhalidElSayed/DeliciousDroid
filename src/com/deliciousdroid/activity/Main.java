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

import com.deliciousdroid.fragment.MainFragment;
import com.deliciousdroid.R;
import com.deliciousdroid.action.IntentHelper;

import android.os.Bundle;
import android.util.DisplayMetrics;

public class Main extends FragmentBaseActivity implements MainFragment.OnMainActionListener {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedState);
		setContentView(R.layout.main);
	}

	public void onMyBookmarksSelected() {
		startActivity(IntentHelper.ViewBookmarks("", mAccount.name, this));	
	}

	public void onMyUnreadSelected() {
		startActivity(IntentHelper.ViewUnread(mAccount.name, this));
	}

	public void onMyTagsSelected() {
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		
		if(findViewById(R.id.main_tablet_detect) != null){
			startActivity(IntentHelper.ViewTabletTags(mAccount.name, this));
		} else {
			startActivity(IntentHelper.ViewTags(mAccount.name, this));
		}
		
	}

	public void onMyNetworkSelected() {
		startActivity(IntentHelper.ViewBookmarks("", "network", this));	
	}

	public void onRecentSelected() {
		startActivity(IntentHelper.ViewBookmarks("", "recent", this));	
	}
}