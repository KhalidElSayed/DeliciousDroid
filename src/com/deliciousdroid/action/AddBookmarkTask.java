package com.deliciousdroid.action;

import java.util.Random;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.activity.AddBookmark;
import com.deliciousdroid.client.DeliciousApi;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;

public class AddBookmarkTask extends AsyncTask<BookmarkTaskArgs, Integer, Boolean>{
	private Context context;
	private Bookmark bookmark;
	private Bookmark oldBookmark;
	private Account account;
	private Boolean update;
	
	@Override
	protected Boolean doInBackground(BookmarkTaskArgs... args) {
		context = args[0].getContext();
		bookmark = args[0].getBookmark();
		oldBookmark = args[0].getOldBookmark();
		account = args[0].getAccount();
		update = args[0].getUpdate();
		
		try {
			Boolean success = DeliciousApi.addBookmark(bookmark, account, context);
			if(success){
				return true;
			} else return false;
		} catch (Exception e) {
			Log.d("addBookmark error", e.toString());
			return false;
		}
	}

    protected void onPostExecute(Boolean result) {

		if(!result){
			throwError();
		}
    }
    
    private void throwError(){
    	 NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    	 Random r = new Random();
    	 int notification = r.nextInt();
    	 
    	 Notification n = new Notification(R.drawable.ic_main, "DeliciousDroid Error", System.currentTimeMillis());
    	 
    	 Intent ni = new Intent(context, AddBookmark.class);
    	 ni.setAction(Intent.ACTION_SEND);
    	 ni.setData(Uri.parse("content://com.deliciousdroid.bookmarks/" + Integer.toString(notification)));
    	 ni.putExtra(Intent.EXTRA_TEXT, bookmark.getUrl());
    	 ni.putExtra(Constants.EXTRA_DESCRIPTION, bookmark.getDescription());
    	 ni.putExtra(Constants.EXTRA_NOTES, bookmark.getNotes());
    	 ni.putExtra(Constants.EXTRA_TAGS, bookmark.getTagString());
    	 ni.putExtra(Constants.EXTRA_PRIVATE, bookmark.getPrivate());
    	 ni.putExtra(Constants.EXTRA_ERROR, true);
    	 
    	 if(update) {
			ni.putExtra(Intent.EXTRA_TEXT + ".old", oldBookmark.getUrl());
			ni.putExtra(Constants.EXTRA_DESCRIPTION + ".old", oldBookmark.getDescription());
			ni.putExtra(Constants.EXTRA_NOTES + ".old", oldBookmark.getNotes());
			ni.putExtra(Constants.EXTRA_TAGS + ".old", oldBookmark.getTagString());
			ni.putExtra(Constants.EXTRA_PRIVATE + ".old", oldBookmark.getPrivate());
			ni.putExtra(Constants.EXTRA_TIME + ".old", oldBookmark.getTime());
		} 
    	 
    	 ni.putExtra(Constants.EXTRA_UPDATE, update);
    	 ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	 
    	 n.flags |= Notification.FLAG_AUTO_CANCEL;
    	 n.flags |= Notification.FLAG_NO_CLEAR;
    	 
    	 PendingIntent ci = PendingIntent.getActivity(context, 0, ni, PendingIntent.FLAG_UPDATE_CURRENT);
    	 n.setLatestEventInfo(context, "DeliciousDroid Error", "Error Saving Bookmark", ci);

    	 nm.notify(notification, n);
    }
}