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

package com.deliciousdroid.widget;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.activity.AddBookmark;
import com.deliciousdroid.activity.BrowseBookmarks;
import com.deliciousdroid.activity.BrowseTags;
import com.deliciousdroid.activity.Main;
import com.deliciousdroid.providers.BookmarkContentProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class SearchWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int n = appWidgetIds.length;
        
		AccountManager mAccountManager = AccountManager.get(context);
		Account mAccount = null;
		String username = "";
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
			mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
			username = mAccount.name;
		}

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < n; i++) {
            int appWidgetId = appWidgetIds[i];


            Intent bookmarkIntent = new Intent(context, BrowseBookmarks.class);
            bookmarkIntent.setAction(Intent.ACTION_VIEW);
            bookmarkIntent.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder bookmarkData = new Uri.Builder();
    		bookmarkData.scheme(Constants.CONTENT_SCHEME);
    		bookmarkData.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
    		bookmarkData.appendEncodedPath("bookmarks");
    		bookmarkIntent.setData(bookmarkData.build());
    		
    		Intent tagIntent = new Intent(context, BrowseTags.class);
    		tagIntent.setAction(Intent.ACTION_VIEW);
    		tagIntent.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder tagData = new Uri.Builder();
    		tagData.scheme(Constants.CONTENT_SCHEME);
    		tagData.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
    		tagData.appendEncodedPath("tags");
    		tagIntent.setData(tagData.build());
    		
    		Intent searchIntent = new Intent(context, Main.class);
    		searchIntent.setAction(Intent.ACTION_SEARCH);
    		
    		Intent addIntent = new Intent(context, AddBookmark.class);
    		
    		Intent networkIntent = new Intent(context, BrowseBookmarks.class);
    		networkIntent.setAction(Intent.ACTION_VIEW);
    		networkIntent.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder data = new Uri.Builder();
    		data.scheme(Constants.CONTENT_SCHEME);
    		data.encodedAuthority("network@" + BookmarkContentProvider.AUTHORITY);
    		data.appendEncodedPath("bookmarks");
    		networkIntent.setData(data.build());
    		
            PendingIntent bookmarkPendingIntent = PendingIntent.getActivity(context, 0, bookmarkIntent, 0);
            PendingIntent tagPendingIntent = PendingIntent.getActivity(context, 0, tagIntent, 0);
            PendingIntent searchPendingIntent = PendingIntent.getActivity(context, 0, searchIntent, 0);
            PendingIntent addPendingIntent = PendingIntent.getActivity(context, 0, addIntent, 0);
            PendingIntent networkPendingIntent = PendingIntent.getActivity(context, 0, networkIntent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_appwidget);
            views.setOnClickPendingIntent(R.id.search_widget_bookmarks_button, bookmarkPendingIntent);
            views.setOnClickPendingIntent(R.id.search_widget_tags_button, tagPendingIntent);
            views.setOnClickPendingIntent(R.id.search_widget_search_button, searchPendingIntent);
            views.setOnClickPendingIntent(R.id.search_widget_add_button, addPendingIntent);
            views.setOnClickPendingIntent(R.id.search_widget_network_button, networkPendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}