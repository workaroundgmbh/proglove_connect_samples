package com.example.pgsdksamplejavaapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Adapter that handles visual representation of the list of configuration profiles.
 */
class ProfilesAdapter extends RecyclerView.Adapter<ProfileViewHolder> {
    private ArrayList<ProfileUiData> profilesList;
    private ProfileClickListener onProfileClicked;

    ProfilesAdapter(ArrayList<ProfileUiData> profiles, ProfileClickListener profileClickListener) {
        profilesList = profiles;
        onProfileClicked = profileClickListener;
    }

    @Override
    @NonNull
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View profileView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(profileView);
    }

    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {
        holder.bind(
                profilesList.get(position),
                onProfileClicked
        );
    }

    @Override
    public int getItemCount() {
        return profilesList.size();
    }

    /**
     * Updates list of configuration profiles and refreshes all the items.
     */
    void updateProfiles(ArrayList<ProfileUiData> profiles) {
        profilesList.clear();
        profilesList.addAll(profiles);
        notifyDataSetChanged();
    }
}

/**
 * ViewHolder for displaying configuration profile's overview.
 */
class ProfileViewHolder extends RecyclerView.ViewHolder {

    private Button profileActivateButton;
    private TextView profileActiveLabel;

    ProfileViewHolder(View view) {
        super(view);
        profileActivateButton = view.findViewById(R.id.profileActivateButton);
        profileActiveLabel = view.findViewById(R.id.profileActiveLabel);
    }

    void bind(final ProfileUiData profile, final ProfileClickListener profileClickListener) {
        profileActivateButton.setText(profile.profileId);
        profileActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileClickListener.onProfileClicked(profile.profileId);
            }
        });

        if (profile.active) {
            profileActiveLabel.setVisibility(View.VISIBLE);
        } else {
            profileActiveLabel.setVisibility(View.INVISIBLE);
        }
    }
}
