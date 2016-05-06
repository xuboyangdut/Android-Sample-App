package com.sharethrough.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.sharethrough.sample.R;
import com.sharethrough.sdk.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PublisherListAdapterActivity extends Activity {
    // prelive multiple dynamic placement
    private static final String PLACEMENT_KEY = "c1a0a591";
    private Context context = this;
    private SwipeRefreshLayout swipeLayout;
    private PublisherListAdapter publisherListAdapter;
    private SharethroughListAdapter sharethroughListAdapter;
    private Sharethrough sharethrough = null;
    private String savedSharethrough = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mt_activity_sharethrough_list_adapter);
        setSwipeRefreshLayout();
    }

    private void setupListAdapter() {
        publisherListAdapter = new PublisherListAdapter(context.getApplicationContext(), R.layout.mt_list_view, new ArrayList<ContentItem>());

        if (savedSharethrough != null) {
            // Initialize Sharethrough with serializedSharethrough and reset serializedSharethrough
            STRSdkConfig config = new STRSdkConfig(this, PLACEMENT_KEY);
            config.setSerializedSharethrough(savedSharethrough);
            sharethrough = new Sharethrough(config);
            savedSharethrough = null;
        } else{
            sharethrough = new Sharethrough(new STRSdkConfig(this, PLACEMENT_KEY));
        }

        sharethroughListAdapter = new SharethroughListAdapter(context, publisherListAdapter, sharethrough, R.layout.mt_ad_view, R.id.title, R.id.description, R.id.advertiser, R.id.thumbnail, R.id.optout_icon, R.id.brand_logo);
        sharethrough.setOnStatusChangeListener(new Sharethrough.OnStatusChangeListener() {
            @Override
            public void newAdsToShow() {
                sharethroughListAdapter.notifyDataSetChanged();
            }

            @Override
            public void noAdsToShow() {
            }
        });

        // create listview
        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(sharethroughListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra("link", ((ContentItem)sharethroughListAdapter.getItem(position)).getLink());
                startActivity(intent);
            }
        });
    }


    /**
     * Makes request to retrieve sample app content items
     */
    private void retrievePublisherContentList() {
        try {
            // parses sample app content items response
            ContentItemParser contentItemParser = new ContentItemParser(ContentItemParser.feed);
            ArrayList<ContentItem> contentList = new ArrayList<ContentItem>();
            contentList.addAll(contentItemParser.getContentItemList());
            // creates list sampleAppListAdapter on initial app load, or refreshes content list in sampleAppListAdapter
            // when user drags to refresh
            if (publisherListAdapter == null) {
                setupListAdapter();
            } else {
                publisherListAdapter.setContentList(contentList);
                publisherListAdapter.notifyDataSetChanged();
                sharethroughListAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.w("Sample App", e.getMessage());
        }
    }

    /**
     * Sets the drag down to refresh layout
     */
    private void setSwipeRefreshLayout() {
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorSchemeResources(android.R.color.holo_green_light);
        swipeLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.transparent));
        swipeLayout.setOnRefreshListener(refreshListener);
    }

    // refreshListener will make an content request to refresh the Messy Truth content items
    private final SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrievePublisherContentList();
            swipeLayout.setRefreshing(false);
        }
    };

    @Override
    protected void onResume() {
        setupListAdapter();
        retrievePublisherContentList();

        Map<String, String> customKeyValues = new HashMap<String, String>();
        customKeyValues.put("key1", "val1");
        customKeyValues.put("key2", "val2");
        sharethrough.fetchAds(customKeyValues);

        super.onResume();
    }

    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        savedSharethrough = (String) savedInstanceState.get("sharethrough");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("sharethrough", sharethrough.serialize());
        super.onSaveInstanceState(outState);
    }
}


