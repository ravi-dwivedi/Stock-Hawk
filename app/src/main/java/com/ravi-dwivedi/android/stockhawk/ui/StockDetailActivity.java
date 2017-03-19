package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.Map;
import java.util.TreeMap;

/**
 * Activity which shows detail information on stocks.
 */
public class StockDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String EXTRA_ID = "id";

    private View progressCircle;
    private String mId;
    private LineChartView mLineChartView;
    private TextView mDetailStockSymbolTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mId = getIntent().getExtras().getString(EXTRA_ID);
        if (mId == null) mId = savedInstanceState.getString(EXTRA_ID);

        // Leave the activity if there is no mId given
        if (mId == null) finish();

        // Fetch ui
        progressCircle = findViewById(R.id.progress_circle);
        mLineChartView = (LineChartView) findViewById(R.id.linechart);
        mDetailStockSymbolTextView = (TextView) findViewById(R.id.detail_stock_symbol);

        // Initialize ui
        mLineChartView.setLabelsColor(Color.WHITE);
        mLineChartView.setAxisColor(Color.WHITE);
        mDetailStockSymbolTextView.setText(mId);

        // Start loader
        getSupportLoaderManager().restartLoader(0, null, this).forceLoad();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the mId
        outState.putString(EXTRA_ID, mId);

        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        progressCircle.setVisibility(View.VISIBLE);
        return new CursorLoader(this, QuoteProvider.Quotes.withSymbol(mId),
                new String[]{QuoteColumns._ID, QuoteColumns.CREATED, QuoteColumns.BIDPRICE}, null,
                null, QuoteColumns.CREATED + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Clear any previous data
        mLineChartView.getData().clear();

        // Initialize the cursors position
        data.moveToFirst();

        // Add each entry to the hashmap, this is to avoid duplicates and preserve order
        TreeMap<String, Float> dataMap = new TreeMap<>();
        do {
            try {
                dataMap.put(data.getString(1), Float.parseFloat(data.getString(2)));
            } finally {
                data.moveToNext();
            }
        } while (!data.isLast());

        // Convert map to lineset
        LineSet lineSet = new LineSet();
        lineSet.setColor(Color.WHITE);
        int max = 0;
        for (Map.Entry<String, Float> entry : dataMap.entrySet()) {
            lineSet.addPoint(entry.getKey(), entry.getValue());
            if ((int) entry.getValue().floatValue() > max)
                max = (int) entry.getValue().floatValue();
        }

        // Show the lineset
        mLineChartView.addData(lineSet);
        int maxAxisValue = (int) (max + 0.2f * max);
        maxAxisValue += 5 - maxAxisValue % 5;
        mLineChartView.setAxisBorderValues(0, maxAxisValue, maxAxisValue / 5);
        progressCircle.setVisibility(View.GONE);
        mLineChartView.show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
