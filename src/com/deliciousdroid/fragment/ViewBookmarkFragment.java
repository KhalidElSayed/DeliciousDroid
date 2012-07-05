/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.deliciousdroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.deliciousdroid.Constants.BookmarkViewType;
import com.deliciousdroid.R;
import com.deliciousdroid.action.IntentHelper;
import com.deliciousdroid.activity.FragmentBaseActivity;
import com.deliciousdroid.client.NetworkUtilities;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.providers.ArticleContent.Article;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.ContentNotFoundException;
import com.deliciousdroid.providers.TagContent.Tag;
import com.deliciousdroid.ui.AccountSpan;
import com.deliciousdroid.ui.TagSpan;

public class ViewBookmarkFragment extends Fragment {

	private FragmentBaseActivity base;
	
	private View container;
	private ScrollView mBookmarkView;
	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotes;
	private TextView mTags;
	private TextView mTime;
	private TextView mUsername;
	private ImageView mIcon;
	private WebView mWebContent;
	private Bookmark bookmark;
	private BookmarkViewType viewType;
	private View readSection;
	private TextView readTitle;
	private TextView readView;
	
	private OnBookmarkActionListener bookmarkActionListener;
	private OnBookmarkSelectedListener bookmarkSelectedListener;
	
	private static final String STATE_VIEWTYPE = "viewType";
	
	public interface OnBookmarkActionListener {
		public void onViewTagSelected(String tag);
		public void onUserTagSelected(String tag, String user);
		public void onAccountSelected(String account);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
	    if (savedInstanceState != null) {
	        viewType = (BookmarkViewType)savedInstanceState.getSerializable(STATE_VIEWTYPE);
	    } 
		
		base = (FragmentBaseActivity)getActivity();
		
		container = (View) getView().findViewById(R.id.view_bookmark_container);
		mBookmarkView = (ScrollView) getView().findViewById(R.id.bookmark_scroll_view);
		mTitle = (TextView) getView().findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) getView().findViewById(R.id.view_bookmark_url);
		mNotes = (TextView) getView().findViewById(R.id.view_bookmark_notes);
		mTags = (TextView) getView().findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) getView().findViewById(R.id.view_bookmark_time);
		mUsername = (TextView) getView().findViewById(R.id.view_bookmark_account);
		mIcon = (ImageView) getView().findViewById(R.id.view_bookmark_icon);
		mWebContent = (WebView) getView().findViewById(R.id.web_view);
		readSection = getView().findViewById(R.id.read_bookmark_section);
		readTitle = (TextView) getView().findViewById(R.id.read_bookmark_title);
		readView = (TextView) getView().findViewById(R.id.read_view);
		
		mWebContent.getSettings().setJavaScriptEnabled(true);
		readView.setMovementMethod(LinkMovementMethod.getInstance());
		
		setHasOptionsMenu(true);
		//setRetainInstance(true);
	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
    		bookmarkActionListener.onViewTagSelected(tag);
        }
    };
    
    TagSpan.OnTagClickListener userTagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
        	bookmarkActionListener.onUserTagSelected(tag, bookmark.getAccount());
        }
    };
    
    AccountSpan.OnAccountClickListener accountOnClickListener = new AccountSpan.OnAccountClickListener() {
        public void onAccountClick(String account) {
        	bookmarkActionListener.onAccountSelected(account);
        }
    };
    
	public void setBookmark(Bookmark b, BookmarkViewType viewType) {
		this.viewType = viewType;
		bookmark = b;
		
		ActivityCompat.invalidateOptionsMenu(this.getActivity());
	}
	
	private void addTag(SpannableStringBuilder builder, Tag t, TagSpan.OnTagClickListener listener) {
		int flags = 0;
		
		if (builder.length() != 0) {
			builder.append("  ");
		}
		
		int start = builder.length();
		builder.append(t.getTagName());
		int end = builder.length();
		
		TagSpan span = new TagSpan(t.getTagName());
		span.setOnTagClickListener(listener);

		builder.setSpan(span, start, end, flags);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putSerializable(STATE_VIEWTYPE, viewType);
	    
	    super.onSaveInstanceState(savedInstanceState);
	}
    
	@Override
	@TargetApi(14)
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.view_menu, menu);
	    
	    if(android.os.Build.VERSION.SDK_INT >= 14) {
	    	Log.d("bookmark", Boolean.toString(bookmark == null));
	    	if(bookmark != null){
	    		ShareActionProvider shareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_view_sendbookmark).getActionProvider();
	    		shareActionProvider.setShareIntent(IntentHelper.SendBookmark(bookmark.getUrl(), bookmark.getDescription()));
	    	}
	    }
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if(bookmark != null){
			if(!isMyself()){
				menu.removeItem(R.id.menu_view_editbookmark);
				menu.removeItem(R.id.menu_view_deletebookmark);
			} else {
				menu.removeItem(R.id.menu_addbookmark);
			}
		} else {
			menu.removeItem(R.id.menu_view);
			menu.removeItem(R.id.menu_view_sendbookmark);
			menu.removeItem(R.id.menu_view_editbookmark);
			menu.removeItem(R.id.menu_view_deletebookmark);
		}
	}
	
	@Override
	@TargetApi(14)
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_view_details:
		    	bookmarkSelectedListener.onBookmarkView(bookmark);
				return true;
		    case R.id.menu_view_read:
				bookmarkSelectedListener.onBookmarkRead(bookmark);
				return true;
		    case R.id.menu_view_openbookmark:
		    	bookmarkSelectedListener.onBookmarkOpen(bookmark);
				return true;
		    case R.id.menu_view_editbookmark:
		    	bookmarkSelectedListener.onBookmarkEdit(bookmark);
		    	return true;
		    case R.id.menu_view_deletebookmark:
		    	bookmarkSelectedListener.onBookmarkDelete(bookmark);
				return true;
		    case R.id.menu_view_sendbookmark:
		    	if(android.os.Build.VERSION.SDK_INT < 14 || item.getActionProvider() == null || !(item.getActionProvider() instanceof ShareActionProvider)) {
		    		bookmarkSelectedListener.onBookmarkShare(bookmark);
		    	}
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.view_bookmark_fragment, container, false);
    }
    
    private boolean isMyself() {
    	return bookmark != null && bookmark.getId() != 0;
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	
    	loadBookmark();
    }

    public void loadBookmark(){
    	if(bookmark != null){
    		
    		if(isMyself() && bookmark.getId() != 0){
				try{		
					int id = bookmark.getId();
					bookmark = BookmarkManager.GetById(id, base);
				}
				catch(ContentNotFoundException e){}
    		}
    		
    		if(viewType == BookmarkViewType.VIEW){
				mBookmarkView.setVisibility(View.VISIBLE);
				readSection.setVisibility(View.GONE);
				mWebContent.setVisibility(View.GONE);
				if(isMyself()){
					Date d = new Date(bookmark.getTime());
					
					mTitle.setText(bookmark.getDescription());
					mUrl.setText(bookmark.getUrl());
					mNotes.setText(bookmark.getNotes());
					mTime.setText(d.toString());
					mUsername.setText(bookmark.getAccount());
					
					if(mIcon != null){
						if(!bookmark.getShared()) {
							mIcon.setImageResource(R.drawable.padlock);
						}
					}
					
	        		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
	
	        		for(Tag t : bookmark.getTags()) {
	        			addTag(tagBuilder, t, tagOnClickListener);
	        		}
	        		
	        		mTags.setText(tagBuilder);
	        		mTags.setMovementMethod(LinkMovementMethod.getInstance());
				} else {
					
					Date d = new Date(bookmark.getTime());
					
					if(bookmark.getDescription() != null && !bookmark.getDescription().equals("null"))
						mTitle.setText(bookmark.getDescription());
					
					mUrl.setText(bookmark.getUrl());
					
					if(bookmark.getNotes() != null && !bookmark.getNotes().equals("null"))
						mNotes.setText(bookmark.getNotes());
					
					mTime.setText(d.toString());
					
		    		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
		
		    		for(Tag t : bookmark.getTags()) {
		    			addTag(tagBuilder, t, userTagOnClickListener);
		    		}
		    		
		    		mTags.setText(tagBuilder);
		    		mTags.setMovementMethod(LinkMovementMethod.getInstance());
		
		    		if(bookmark.getAccount() != null){
						SpannableStringBuilder builder = new SpannableStringBuilder();
						int start = builder.length();
						builder.append(bookmark.getAccount());
						int end = builder.length();
						
						AccountSpan span = new AccountSpan(bookmark.getAccount());
						span.setOnAccountClickListener(accountOnClickListener);
			
						builder.setSpan(span, start, end, 0);
						
						mUsername.setText(builder);
		    		}
					
					
					mUsername.setMovementMethod(LinkMovementMethod.getInstance());
				}
			} else if(viewType == BookmarkViewType.READ){
				new GetArticleTask().execute(bookmark.getUrl());
			} else if(viewType == BookmarkViewType.WEB){
				showInWebView();
			}
    	}
    }
    
    private void showInWebView(){
		mWebContent.clearView();
		mWebContent.clearCache(true);
		mBookmarkView.setVisibility(View.GONE);
		readSection.setVisibility(View.GONE);
		mWebContent.setVisibility(View.VISIBLE);				
		mWebContent.loadUrl(bookmark.getUrl());
    }
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			bookmarkActionListener = (OnBookmarkActionListener) activity;
			bookmarkSelectedListener = (OnBookmarkSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBookmarkActionListener and OnBookmarkSelectedListener");
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		readView.setBackgroundColor(Integer.parseInt(base.readingBackground));
		readTitle.setBackgroundColor(Integer.parseInt(base.readingBackground));
		
		if(Integer.parseInt(base.readingBackground) == Color.BLACK){
			readView.setTextColor(Color.parseColor("#999999"));
			readTitle.setTextColor(Color.parseColor("#999999"));
		}
		else { 
			readView.setTextColor(Color.parseColor("#222222"));
			readTitle.setTextColor(Color.parseColor("#222222"));
		}
		
		readView.setPadding(Integer.parseInt(base.readingMargins), 15, Integer.parseInt(base.readingMargins), 15);

		Typeface tf = Typeface.createFromAsset(base.getAssets(), "fonts/" + base.readingFont + ".ttf");
		readView.setTypeface(tf);
		
		readView.setTextSize(Float.parseFloat(base.readingFontSize));
		readView.setLineSpacing(Float.parseFloat(base.readingLineSpace), 1);
		
	}
	
    public class GetArticleTask extends AsyncTask<String, Integer, Article>{
    	private String url;

    	@Override
    	protected Article doInBackground(String... args) {
    		
    		if(args.length > 0 && args[0] != null && args[0] != "") {
    			url = args[0];
    	
        		Article a = NetworkUtilities.getArticleText(url);

        		if(a != null && a.getContent() != null){
	        		Spanned s = Html.fromHtml(a.getContent(), new Html.ImageGetter() {
	
	        			public Drawable getDrawable(String source) {                  
	        				Drawable d = null;
	        				try {
	        					InputStream src = imageFetch(source);
	        					d = Drawable.createFromStream(src, "src");
	        					if(d != null){
	        						int containerWidth = container.getWidth() - (Integer.parseInt(base.readingMargins) * 2);
	        						int width = Math.min(containerWidth, d.getIntrinsicWidth());
	        						
	        						int height = d.getIntrinsicHeight();
	        						
	        						if(containerWidth < d.getIntrinsicWidth()){
	        							double scale = ((double)containerWidth / (double)d.getIntrinsicWidth());	        							
	        							double newWidth = d.getIntrinsicHeight() * scale;
	        							height = (int)Math.floor(newWidth);
	        							
	        						}
	        						d.setBounds(0, 0, width, height);
	        					}
	        				} catch (MalformedURLException e) {
	        					e.printStackTrace(); 
	        				} catch (IOException e) {
	        					e.printStackTrace();  
	        				}
	        				return d;
	        			}
	        		}, null);
	
	        		a.setSpan(s);
        		}
        		return a;

    		} else return null;
    	}
    	
        protected void onPostExecute(Article result) {
        	if(result != null && result.getSpan() != null && !result.getContent().equals("") && !result.getContent().equals("null")){
	        	readSection.scrollTo(0, 0);
	        	mBookmarkView.setVisibility(View.GONE);
	        	mWebContent.setVisibility(View.GONE);
				readSection.setVisibility(View.VISIBLE);
				readTitle.setText(Html.fromHtml(result.getTitle()));
				readView.setText(result.getSpan());
        	} else {
        		showInWebView();
        	}
        }
        
        private InputStream imageFetch(String source) throws MalformedURLException,IOException {
	    	URL url = new URL(source);
	    	Object o = url.getContent();
	    	InputStream content = (InputStream)o;    
	    	return content;
	    }
    }
}