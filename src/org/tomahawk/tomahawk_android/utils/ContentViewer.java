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
package org.tomahawk.tomahawk_android.utils;

import org.tomahawk.libtomahawk.database.DatabaseHelper;
import org.tomahawk.tomahawk_android.fragments.FakePreferenceFragment;
import org.tomahawk.tomahawk_android.fragments.PlaybackFragment;
import org.tomahawk.tomahawk_android.fragments.SearchableFragment;
import org.tomahawk.tomahawk_android.fragments.SocialActionsFragment;
import org.tomahawk.tomahawk_android.fragments.TomahawkFragment;
import org.tomahawk.tomahawk_android.fragments.TracksFragment;
import org.tomahawk.tomahawk_android.fragments.UserCollectionFragment;
import org.tomahawk.tomahawk_android.fragments.UserPlaylistsFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class wraps all functionality that handles the switching of {@link Fragment}s, whenever the
 * user navigates to a new {@link Fragment}. It also implements a custom back stack for every hub,
 * so the user can always return to the previous {@link Fragment}s. There is one hub for every menu
 * entry in the navigation drawer.
 */
public class ContentViewer {

    public static final int HUB_ID_HOME = -1;

    public static final int HUB_ID_DASHBOARD = 0;

    public static final int HUB_ID_COLLECTION = 1;

    public static final int HUB_ID_LOVEDTRACKS = 2;

    public static final int HUB_ID_PLAYLISTS = 3;

    public static final int HUB_ID_STATIONS = -2;

    public static final int HUB_ID_FRIENDS = -3;

    public static final int HUB_ID_SETTINGS = 4;

    public static final int HUB_ID_PLAYBACK = 100;

    public static final String FRAGMENT_TAG = "the_ultimate_tag";

    private Context mContext;

    private FragmentManager mFragmentManager;

    private ContentViewerListener mContentViewerListener;

    private int mContentFrameId;

    private ArrayList<FragmentStateHolder> mBackstack = new ArrayList<FragmentStateHolder>();

    /**
     * A {@link FragmentStateHolder} represents and stores all information needed to construct a
     * {@link Fragment}.
     */
    public static final class FragmentStateHolder implements Serializable {

        //The Class variable stores the class of the fragment.
        public final Class clss;

        //tomahawkListItemKey is the id of the corresponding TomahawkListItem which is being passed to the actual
        //fragment instance.
        public String tomahawkListItemKey = "";

        //the type of the corresponding TomahawkListItem
        public String tomahawkListItemType = "";

        //whether or not the corresponding TomahawkListItem is local
        public boolean tomahawkListItemIsLocal = false;

        public boolean showDashboard = false;

        public String queryString = "";

        //the listScrollPosition which is being stored and restored when the fragment is popped or stashed.
        public int listScrollPosition = 0;

        public ArrayList<String> correspondingQueryIds;

        /**
         * Construct a {@link FragmentStateHolder} without providing a reference to a {@link
         * TomahawkListItem}
         */
        public FragmentStateHolder(Class clss, ArrayList<String> correspondingQueryIds) {
            this.clss = clss;
            this.correspondingQueryIds = correspondingQueryIds;
        }

        /**
         * Construct a {@link FragmentStateHolder} while also providing a reference to a {@link
         * TomahawkListItem}
         */
        public FragmentStateHolder(Class clss, ArrayList<String> correspondingQueryIds,
                String tomahawkListItemKey, String tomahawkListItemType,
                boolean tomahawkListItemIsLocal) {
            this.clss = clss;
            this.correspondingQueryIds = correspondingQueryIds;
            this.tomahawkListItemKey = tomahawkListItemKey;
            this.tomahawkListItemType = tomahawkListItemType;
            this.tomahawkListItemIsLocal = tomahawkListItemIsLocal;
        }

        /**
         * Construct a {@link FragmentStateHolder} while also providing a reference to a {@link
         * TomahawkListItem}
         */
        public FragmentStateHolder(Class clss, ArrayList<String> correspondingQueryIds,
                String tomahawkListItemKey, String tomahawkListItemType,
                boolean tomahawkListItemIsLocal, boolean showDashboard) {
            this.clss = clss;
            this.correspondingQueryIds = correspondingQueryIds;
            this.tomahawkListItemKey = tomahawkListItemKey;
            this.tomahawkListItemType = tomahawkListItemType;
            this.tomahawkListItemIsLocal = tomahawkListItemIsLocal;
            this.showDashboard = showDashboard;
        }

        public boolean equals(FragmentStateHolder fragmentStateHolder) {
            return fragmentStateHolder != null
                    && fragmentStateHolder.clss == this.clss
                    && fragmentStateHolder.showDashboard == this.showDashboard
                    && fragmentStateHolder.tomahawkListItemIsLocal == this.tomahawkListItemIsLocal
                    && fragmentStateHolder.tomahawkListItemType.equals(this.tomahawkListItemType)
                    && fragmentStateHolder.tomahawkListItemKey.equals(this.tomahawkListItemKey)
                    && fragmentStateHolder.queryString.equals(this.queryString);
        }
    }

    /**
     * Constructs a new {@link ContentViewer}
     */
    public ContentViewer(Context context, FragmentManager fragmentManager,
            ContentViewerListener listener, int contentFrameId) {
        initialize(context, fragmentManager, listener, contentFrameId);
    }

    public void initialize(Context context, FragmentManager fragmentManager,
            ContentViewerListener listener, int contentFrameId) {
        mContext = context;
        mFragmentManager = fragmentManager;
        mContentFrameId = contentFrameId;
        mContentViewerListener = listener;
    }

    /**
     * Replaces the {@link Fragment} in the hub with the given hub id and adds it to the backstack,
     * if isBackAction is false.
     *
     * @param fragmentStateHolder {@link FragmentStateHolder} contains all information of the to be
     *                            replaced {@link Fragment}
     * @param isBackAction        whether or not the replacement is part of an action going back in
     *                            the backstack
     */
    public void replace(FragmentStateHolder fragmentStateHolder, boolean isBackAction) {
        // Get fragmentsStack for the given (tabs)position
        FragmentStateHolder currentFragmentStateHolder = getCurrentFragmentStateHolder();
        // Replace the fragment using a transaction.
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (isBackAction) {
            mBackstack.remove(currentFragmentStateHolder);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        } else {
            Fragment currentFragment = null;
            if (mFragmentManager != null && mFragmentManager.getFragments() != null) {
                currentFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
            }
            if (currentFragmentStateHolder != null && currentFragment != null
                    && currentFragment instanceof TomahawkFragment) {
                currentFragmentStateHolder.listScrollPosition
                        = ((TomahawkFragment) currentFragment).getListScrollPosition();
            }
            mBackstack.add(fragmentStateHolder);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
        Bundle bundle = new Bundle();
        bundle.putString(fragmentStateHolder.tomahawkListItemType,
                fragmentStateHolder.tomahawkListItemKey);
        bundle.putBoolean(TomahawkFragment.TOMAHAWK_LIST_ITEM_IS_LOCAL,
                fragmentStateHolder.tomahawkListItemIsLocal);
        bundle.putInt(TomahawkFragment.TOMAHAWK_LIST_SCROLL_POSITION,
                fragmentStateHolder.listScrollPosition);
        bundle.putString(SearchableFragment.SEARCHABLEFRAGMENT_QUERY_STRING,
                fragmentStateHolder.queryString);
        bundle.putBoolean(SocialActionsFragment.SHOW_DASHBOARD,
                fragmentStateHolder.showDashboard);
        ft.replace(mContentFrameId,
                Fragment.instantiate(mContext, fragmentStateHolder.clss.getName(), bundle),
                FRAGMENT_TAG);
        ft.commit();
        mContentViewerListener.updateViewVisibility();
    }

    /**
     * Replaces the {@link Fragment} in the hub with the given hub id and adds it to the backstack,
     * if isBackAction is false.
     *
     * @param clss                 The {@link Fragment}'s class to be used to construct a new {@link
     *                             FragmentStateHolder}
     * @param tomahawkListItemKey  the key of the {@link TomahawkListItem} corresponding to the
     *                             {@link Fragment}
     * @param tomahawkListItemType {@link String} containing the {@link TomahawkListItem}'s type
     * @param isBackAction         whether or not the replacement is part of an action going back in
     *                             the backstack
     */
    public void replace(Class clss, String tomahawkListItemKey,
            String tomahawkListItemType, boolean tomahawkListItemIsLocal,
            boolean isBackAction) {
        FragmentStateHolder fragmentStateHolder = new FragmentStateHolder(clss, null,
                tomahawkListItemKey, tomahawkListItemType,
                tomahawkListItemIsLocal);
        replace(fragmentStateHolder, isBackAction);
    }

    /**
     * Replaces the current {@link Fragment} with the previous {@link Fragment} stored in the
     * backStack. Does nothing and returns false if no previous {@link Fragment} exists.
     */
    public boolean back() {
        if (mBackstack.size() > 1) {
            FragmentStateHolder previousFragmentStateHolder = mBackstack.get(mBackstack.size() - 2);
            // Restore the remembered fragment and remove it from back fragments.
            this.replace(previousFragmentStateHolder, true);
            return true;
        }
        // Nothing to go back to.
        return false;
    }

    /**
     * Get the complete backstack
     *
     * @return the complete backstack
     */
    public ArrayList<FragmentStateHolder> getBackStack() {
        FragmentStateHolder currentFragmentStateHolder = getCurrentFragmentStateHolder();
        Fragment currentFragment = null;
        if (mFragmentManager != null && mFragmentManager.getFragments() != null) {
            currentFragment = mFragmentManager.getFragments().get(0);
        }
        if (currentFragment != null && currentFragment instanceof TomahawkFragment) {
            currentFragmentStateHolder.listScrollPosition = ((TomahawkFragment) currentFragment)
                    .getListScrollPosition();
            mBackstack.set(mBackstack.size() - 1, currentFragmentStateHolder);
        }
        return mBackstack;
    }

    public FragmentStateHolder getCurrentFragmentStateHolder() {
        if (mBackstack.size() > 0) {
            return mBackstack.get(mBackstack.size() - 1);
        }
        return null;
    }

    /**
     * Set the currently shown hub, by providing its id
     *
     * @param hubToShow the id of the hub which should be shown
     */
    public void showHub(int hubToShow) {
        FragmentStateHolder newFragmentStateHolder = null;
        switch (hubToShow) {
            case HUB_ID_HOME:
            case HUB_ID_DASHBOARD:
                if (mContentViewerListener.getLoggedInUser() != null) {
                    String key = mContentViewerListener.getLoggedInUser().getId();
                    newFragmentStateHolder = new FragmentStateHolder(SocialActionsFragment.class,
                            null, key, TomahawkFragment.TOMAHAWK_USER_ID, false, true);
                }
                break;
            case HUB_ID_COLLECTION:
                newFragmentStateHolder = new FragmentStateHolder(UserCollectionFragment.class,
                        null);
                break;
            case HUB_ID_LOVEDTRACKS:
                newFragmentStateHolder = new FragmentStateHolder(TracksFragment.class, null,
                        DatabaseHelper.LOVEDITEMS_PLAYLIST_ID,
                        UserPlaylistsFragment.TOMAHAWK_USERPLAYLIST_KEY, false);
                break;
            case HUB_ID_PLAYLISTS:
                newFragmentStateHolder = new FragmentStateHolder(UserPlaylistsFragment.class,
                        null);
                break;
            case HUB_ID_STATIONS:
            case HUB_ID_FRIENDS:
            case HUB_ID_SETTINGS:
                newFragmentStateHolder = new FragmentStateHolder(FakePreferenceFragment.class,
                        null);
                break;
            case HUB_ID_PLAYBACK:
                newFragmentStateHolder = new FragmentStateHolder(PlaybackFragment.class,
                        null);
                break;
        }
        FragmentStateHolder currentFragmentStateHolder = getCurrentFragmentStateHolder();
        if (newFragmentStateHolder != null
                && !newFragmentStateHolder.equals(currentFragmentStateHolder)) {
            replace(newFragmentStateHolder, false);
        }
    }
}
