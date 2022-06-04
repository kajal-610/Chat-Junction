package com.example.cj.Adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.cj.Adapters.UsersAdapter.UsersViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import com.example.cj.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.bumptech.glide.Glide
import android.content.Intent
import android.view.View
import com.example.cj.Activities.ChatActivity
import com.example.cj.Models.User
import com.example.cj.databinding.RowConversationBinding
import java.text.SimpleDateFormat
import java.util.*

class UsersAdapter(var context: Context, var users: ArrayList<User?>?) :
    RecyclerView.Adapter<UsersViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = users?.get(position)
        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom = senderId + user?.uid
        FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(senderRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.child("lastMsg").getValue(
                            String::class.java
                        )
                        val time = snapshot.child("lastMsgTime").getValue(
                            Long::class.java
                        )!!
                        val dateFormat = SimpleDateFormat("hh:mm a")
                        holder.binding.msgTime.text = dateFormat.format(Date(time))
                        holder.binding.lastMsg.text = lastMsg
                    } else {
                        holder.binding.lastMsg.text = "Tap to chat"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        if (user != null) {
            holder.binding.username.text = user.name
        }
        if (user != null) {
            Glide.with(context).load(user.profileImage)
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            if (user != null) {
                intent.putExtra("name", user.name)
            }
            if (user != null) {
                intent.putExtra("image", user.profileImage)
            }
            if (user != null) {
                intent.putExtra("uid", user.uid)
            }
            if (user != null) {
                intent.putExtra("token", user.token)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return users!!.size  
    }

    inner class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowConversationBinding

        init {
            binding = RowConversationBinding.bind(itemView)
        }
    }
}
