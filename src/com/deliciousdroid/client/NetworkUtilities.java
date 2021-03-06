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

package com.deliciousdroid.client;

import android.util.Log;

import com.deliciousdroid.Constants;
import com.deliciousdroid.client.HttpClientFactory;
import com.deliciousdroid.providers.ArticleContent.Article;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.AuthScope;
import android.net.Uri;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Provides utility methods for communicating with the server.
 */
public class NetworkUtilities {
    private static final String TAG = "NetworkUtilities";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_UPDATED = "timestamp";
    public static final String USER_AGENT = "AuthenticationService/1.0";

    public static final String FETCH_FRIEND_UPDATES_URI = "http://feeds.delicious.com/v2/json/networkmembers/";
    public static final String FETCH_FRIEND_BOOKMARKS_URI = "http://feeds.delicious.com/v2/json/";
    public static final String FETCH_NETWORK_RECENT_BOOKMARKS_URI = "http://feeds.delicious.com/v2/json/network/";
    public static final String FETCH_STATUS_URI = "http://feeds.delicious.com/v2/json/network/";
    public static final String FETCH_TAGS_URI = "http://feeds.delicious.com/v2/json/tags/";

    private static final String SCHEME = "http";
    private static final String SCHEME_HTTP = "http";
    private static final String DELICIOUS_AUTHORITY = "api.del.icio.us";
    private static final int PORT = 80;
    private static final int PORT_HTTP = 80;
 
    private static final AuthScope SCOPE = new AuthScope(DELICIOUS_AUTHORITY, PORT);
    private static final AuthScope SCOPE_HTTP = new AuthScope(DELICIOUS_AUTHORITY, PORT_HTTP);

    /**
     * Attempts to authenticate to Pinboard using a legacy Pinboard account.
     * 
     * @param username The user's username.
     * @param password The user's password.
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static boolean pinboardAuthenticate(String username, String password) {
        final HttpResponse resp;
        
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME);
        builder.authority(DELICIOUS_AUTHORITY);
        builder.appendEncodedPath("v1/posts/update");
        Uri uri = builder.build();

        HttpGet request = new HttpGet(String.valueOf(uri));

        DefaultHttpClient client = (DefaultHttpClient)HttpClientFactory.getThreadSafeClient();
        
        CredentialsProvider provider = client.getCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(SCOPE, credentials);

        try {
            resp = client.execute(request);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Successful authentication");
                }
                return true;
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Error authenticating" + resp.getStatusLine());
                }
                return false;
            }
        } catch (final IOException e) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "IOException when getting authtoken", e);
            }
            return false;
        } finally {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "getAuthtoken completing");
            }
        }
    }

    
    /**
     * Gets the title of a web page.
     * 
     * @param url The URL of the web page.
     * @return A String containing the title of the web page.
     */
    public static String getWebpageTitle(String url) {
   	
    	if(url != null && !url.equals("")) {
    		
    		if(!url.startsWith("http")){
    			url = "http://" + url;
    		}
	
	    	HttpResponse resp = null;
	    	HttpGet post = null;
	    	
	    	try {
				post = new HttpGet(url.replace("|", "%7C"));
	
				post.setHeader("User-Agent", "Mozilla/5.0");
	
				resp = HttpClientFactory.getThreadSafeClient().execute(post);

		    	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		    		String response = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
		    		int start = response.indexOf("<title>") + 7;
		    		int end = response.indexOf("</title>", start + 1);
		    		String title = response.substring(start, end);
		    		return title;
		    	} else return "";
			} catch (Exception e) {
				return "";
			}
    	} else return "";
    }
    
    /**
     * Gets the title of a web page.
     * 
     * @param url The URL of the web page.
     * @return A String containing the title of the web page.
     */
    public static Article getArticleText(String url) {
   	
    	if(url != null && !url.equals("")) {
    		
    		if(!url.startsWith("http")){
    			url = "http://" + url;
    		}
	
	    	HttpResponse resp = null;
	    	HttpGet post = null;
	    	
	    	try {
				post = new HttpGet(Constants.TEXT_EXTRACTOR_URL + URLEncoder.encode(url, "UTF-8") + "&format=json");
	
				post.setHeader("User-Agent", "Mozilla/5.0");
	
				resp = HttpClientFactory.getThreadSafeClient().execute(post);
				
		        
		        final int statusCode = resp.getStatusLine().getStatusCode();
				
		    	if (statusCode == HttpStatus.SC_OK) {		    		
		    		final String response = EntityUtils.toString(resp.getEntity());
		    		
		    		final JSONObject article = new JSONObject(response);
		    		
		    		return Article.valueOf(article);
		    	}
			} catch (Exception e) {
				return null;
			}
    	}
		return null;
    }
}