package com.example.cj.Adapters

import android.content.Context
import com.example.cj.Models.UserStatus
import androidx.recyclerview.widget.RecyclerView
import com.example.cj.Adapters.TopStatusAdapter.TopStatusViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.example.cj.R
import com.bumptech.glide.Glide
import omari.hamza.storyview.model.MyStory
import omari.hamza.storyview.StoryView
import com.example.cj.Activities.MainActivity
import com.example.cj.databinding.ItemStatusBinding
import omari.hamza.storyview.callback.StoryClickListeners
import java.util.ArrayList

class TopStatusAdapter(var context: Context, var userStatuses: ArrayList<UserStatus>) :
    RecyclerView.Adapter<TopStatusViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopStatusViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false)
        return TopStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopStatusViewHolder, position: Int) {
        val userStatus = userStatuses[position]
        val lastStatus = userStatus.statuses?.get(userStatus.statuses!!.size - 1)
        Glide.with(context).load(lastStatus?.imageUrl).into(holder.binding.image)
        userStatus.statuses?.let { holder.binding.circularStatusView.setPortionsCount(it.size) }
        holder.binding.circularStatusView.setOnClickListener {
            val myStories = ArrayList<MyStory>()
            for (status in userStatus.statuses!!) {
                myStories.add(MyStory(status.imageUrl))
            }
            StoryView.Builder((context as MainActivity).supportFragmentManager)
                .setStoriesList(myStories) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleText(userStatus.name) // Default is Hidden
                .setSubtitleText("") // Default is Hidden
                .setTitleLogoUrl(userStatus.profileImage) // Default is Hidden
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {
                        //your action
                    }

                    override fun onTitleIconClickListener(position: Int) {
                        //your action
                    }
                }) // Optional Listeners
                .build() // Must be called before calling show method
                .show()
        }
    }

    override fun getItemCount(): Int {
        return userStatuses.size
    }

    inner class TopStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemStatusBinding

        init {
            binding = ItemStatusBinding.bind(itemView)
        }
    }
}