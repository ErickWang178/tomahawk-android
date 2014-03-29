/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2012, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.tomahawk_android.fragments;

import org.tomahawk.libtomahawk.collection.Artist;
import org.tomahawk.libtomahawk.collection.UserCollection;
import org.tomahawk.libtomahawk.utils.TomahawkUtils;
import org.tomahawk.tomahawk_android.R;
import org.tomahawk.tomahawk_android.activities.TomahawkMainActivity;
import org.tomahawk.tomahawk_android.adapters.TomahawkListAdapter;
import org.tomahawk.tomahawk_android.utils.TomahawkListItem;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link TomahawkFragment} which shows a set of {@link Artist}s inside its {@link
 * se.emilsjolander.stickylistheaders.StickyListHeadersListView}
 */
public class ArtistsFragment extends TomahawkFragment implements OnItemClickListener {

    /**
     * Called every time an item inside the {@link se.emilsjolander.stickylistheaders.StickyListHeadersListView}
     * is clicked
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this will be a view
     *                 provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position -= getListView().getHeaderViewsCount();
        if (position >= 0) {
            Object item = getListAdapter().getItem(position);
            if (getListAdapter().getItem(position) instanceof Artist) {
                Bundle bundle = new Bundle();
                String key = TomahawkUtils.getCacheKey((Artist) item);
                bundle.putString(TOMAHAWK_ARTIST_KEY, key);
                ((TomahawkMainActivity) getActivity()).getContentViewer()
                        .replace(AlbumsFragment.class, key, TOMAHAWK_ARTIST_KEY, mIsLocal, false);
            }
        }
    }

    /**
     * Called whenever the {@link org.tomahawk.libtomahawk.collection.UserCollection} {@link Loader}
     * has finished
     */
    @Override
    public void onLoadFinished(Loader<UserCollection> loader, UserCollection coll) {
        super.onLoadFinished(loader, coll);

        TomahawkMainActivity activity = (TomahawkMainActivity) getActivity();
        Context context = getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View rootView = getActivity().findViewById(android.R.id.content);

        activity.setTitle(getString(R.string.artistsfragment_title_string));
        List<TomahawkListItem> artists = new ArrayList<TomahawkListItem>();
        if (mIsLocal) {
            artists.addAll(Artist.getLocalArtists());
        } else {
            artists.addAll(Artist.getArtists());
        }
        List<List<TomahawkListItem>> listArray = new ArrayList<List<TomahawkListItem>>();
        listArray.add(artists);
        if (getListAdapter() == null) {
            TomahawkListAdapter tomahawkListAdapter = new TomahawkListAdapter(context,
                    layoutInflater, rootView, listArray);
            tomahawkListAdapter.setShowArtistAsSingleLine(mIsLocal);
            setListAdapter(tomahawkListAdapter);
        } else {
            ((TomahawkListAdapter) getListAdapter()).setListArray(listArray);
        }

        getListView().setOnItemClickListener(this);
    }
}
