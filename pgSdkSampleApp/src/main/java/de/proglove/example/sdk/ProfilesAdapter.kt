package de.proglove.example.sdk

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter that handles visual representation of the list of configuration profiles.
 */
class ProfilesAdapter(
        private val profilesList: MutableList<ProfileUiData> = mutableListOf(),
        private val onProfileClicked: (String) -> Unit
) : RecyclerView.Adapter<ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val profileView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(profileView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(
                profile = profilesList[position],
                onProfileClicked = onProfileClicked
        )
    }

    override fun getItemCount(): Int {
        return profilesList.size
    }

    /**
     * Updates list of configuration profiles and refreshes all the items.
     */
    fun updateProfiles(profiles: List<ProfileUiData>) {
        profilesList.clear()
        profilesList.addAll(profiles)
        notifyDataSetChanged()
    }
}

/**
 * ViewHolder for displaying configuration profile's overview.
 */
class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val profileActivateButton: Button = view.findViewById(R.id.profileActivateButton)
    private val profileActiveLabel: TextView = view.findViewById(R.id.profileActiveLabel)

    fun bind(profile: ProfileUiData, onProfileClicked: (String) -> Unit) {
        profileActivateButton.apply {
            text = profile.profileId
            setOnClickListener {
                onProfileClicked(profile.profileId)
            }
        }

        if (profile.active) {
            profileActiveLabel.visibility = VISIBLE
        } else {
            profileActiveLabel.visibility = INVISIBLE
        }
    }
}
