package com.example.testapp.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.testapp.R;
import com.example.testapp.adapter.CustomTableDataAdapter;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class LocationHistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_history, container,false);

        // TODO: Get Data from SQLite

        // Hardcoded for now
        final String[] columnHeaders = { "Past 15 days", "Number of Hotspots" };
        String[][] sampleData = {
            { "8/12/2020 - Carmona", "Comembo - 5, Dasmarinas - 6" },
            { "8/11/2020 - Carmona", "Comembo - 5, Dasmarinas - 6" }
        };

        TableView<String[]> tableView = (TableView<String[]>) rootView.findViewById(R.id.table_view);
        tableView.setHeaderBackgroundColor(Color.parseColor("#2ecc71"));
        SimpleTableHeaderAdapter tableHeaderAdapter = new SimpleTableHeaderAdapter(getContext(), columnHeaders);
        tableHeaderAdapter.setPaddingRight(0);
        tableView.setHeaderAdapter(tableHeaderAdapter);
        tableView.setColumnCount(2);

        CustomTableDataAdapter tableDataAdapter = new CustomTableDataAdapter(getContext(), sampleData);
        tableDataAdapter.setPaddingRight(0);
        tableView.setDataAdapter(tableDataAdapter);

        return rootView;
    }
}
