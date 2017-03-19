package com.sam_chordas.android.stockhawk.service;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

/**
 * RemoteViewsService for collection widget
 */
public class WidgetIntentService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                // Retrieve data from content provider
                data = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.CHANGE},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null
                );

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                // Fill in the views
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                views.setTextViewText(R.id.stock_symbol, data.getString(0));
                views.setTextViewText(R.id.bid_price, data.getString(1));
                views.setTextViewText(R.id.change, data.getString(2));

                // Deep link intent to detail activity
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(StockDetailActivity.EXTRA_ID, data.getString(0));
                views.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
