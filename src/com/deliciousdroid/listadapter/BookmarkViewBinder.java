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

package com.deliciousdroid.listadapter;

import com.deliciousdroid.R;

import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookmarkViewBinder implements SimpleCursorAdapter.ViewBinder {

	public boolean setViewValue(View v, Cursor c, int columnIndex) {
        switch(v.getId()) {
            case R.id.bookmark_description:
            	((TextView)v).setText(c.getString(columnIndex));
            	break;
            case R.id.bookmark_tags:
            	((TextView)v).setText(c.getString(columnIndex));
            	break;
            case R.id.bookmark_unsynced:
            	if(c.getInt(columnIndex) == 0)
            		v.setVisibility(View.VISIBLE);
            	else v.setVisibility(View.GONE);
            	break;
            case R.id.bookmark_private:
                if(c.getInt(columnIndex) == 0){
                	v.setVisibility(View.VISIBLE);
                	((ImageView)v).setImageResource(R.drawable.padlock);
                } else v.setVisibility(View.GONE);
            	break;
        }

		return true;
	}
}
