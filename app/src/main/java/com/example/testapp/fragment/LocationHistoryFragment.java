package com.example.testapp.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.testapp.R;
import com.example.testapp.adapter.CustomTableDataAdapter;
import com.example.testapp.util.DatabaseHelper;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class LocationHistoryFragment extends Fragment {
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_history, container,false);

        // TODO: Get Data from SQLite
        db = new DatabaseHelper(this.getContext());
        SQLiteDatabase database = db.getWritableDatabase();

        //show db data
        Cursor cursor = database.rawQuery("SELECT*FROM LOCATION_HISTORY WHERE CREATED_DATE > datetime('now','-15 day')", null);
        while(cursor.moveToNext()){
            Log.d("ID", cursor.getString(0));
            Log.d("Longitude", ""+cursor.getDouble(1));
            Log.d("Latitude", ""+cursor.getDouble(2));
            Log.d("DISTANCE", cursor.getString(3));
            Log.d("CREATED_DATE", cursor.getString(4));
            Log.d("RESPONSE", cursor.getString(5));
        }

        // Hardcoded for now
        final String[] columnHeaders = { "Past 15 days", "Search Distance", "Number of Hotspots" };
        String[][] sampleData = {
            { "8/12/2020 - Carmona", "1000 m", "Comembo - 5, Dasmarinas - 6" },
            { "8/11/2020 - Carmona", "1000 m", "Comembo - 5, Dasmarinas - 6" }
        };

        TableView<String[]> tableView = (TableView<String[]>) rootView.findViewById(R.id.table_view);
        tableView.setHeaderBackgroundColor(Color.parseColor("#2ecc71"));
        SimpleTableHeaderAdapter tableHeaderAdapter = new SimpleTableHeaderAdapter(getContext(), columnHeaders);
        tableHeaderAdapter.setPaddingRight(0);
        tableView.setHeaderAdapter(tableHeaderAdapter);
        tableView.setColumnCount(3);

        CustomTableDataAdapter tableDataAdapter = new CustomTableDataAdapter(getContext(), sampleData);
        tableDataAdapter.setPaddingRight(0);
        tableView.setDataAdapter(tableDataAdapter);
        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("onHiddenChanged", "I WAS HERE!");
    }


}
