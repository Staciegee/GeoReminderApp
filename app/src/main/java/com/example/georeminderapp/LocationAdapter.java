package com.example.georeminderapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.georeminderapp.R;
import com.example.georeminderapp.LocationEntity;

import java.util.List;

    public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<LocationEntity> locationList;

        private AppDatabase db;

        public LocationAdapter(List<LocationEntity> locationList, AppDatabase db) {
            this.locationList = locationList;
            this.db = db;
        }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView latText;
        TextView lngText;
        TextView nameText;
        Button deleteBtn;
        Button navigateBtn;


        public ViewHolder(View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.nameText);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            navigateBtn = itemView.findViewById(R.id.navigateBtn);

        }
    }

    @NonNull
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        LocationEntity location = locationList.get(position);

        holder.nameText.setText(location.name);

        // ✅ DELETE BUTTON
        holder.deleteBtn.setOnClickListener(v -> {
            db.locationDao().delete(location);

            // ✅ Remove from list
            locationList.remove(position);

            // ✅ Update UI
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, locationList.size());

            Toast.makeText(v.getContext(), "Deleted!", Toast.LENGTH_SHORT).show();
        });

        // ✅ NAVIGATE BUTTON
        holder.navigateBtn.setOnClickListener(v -> {

            String uri = "google.navigation:q=" +
                    location.latitude + "," + location.longitude;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            Uri.parse("google.navigation:q=" + location.latitude + "," + location.longitude);

            intent.setPackage("com.google.android.apps.maps");

            try {
                v.getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Google Maps not installed!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }
}