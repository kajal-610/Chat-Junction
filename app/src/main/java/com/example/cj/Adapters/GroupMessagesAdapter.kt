package com.example.cj.Adapters

import android.app.AlertDialog
import android.content.Context

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.example.cj.R
import com.google.firebase.auth.FirebaseAuth

import com.github.pgreze.reactions.ReactionsConfigBuilder
import com.github.pgreze.reactions.ReactionPopup
import com.google.firebase.database.FirebaseDatabase
import com.bumptech.glide.Glide
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import com.example.cj.Models.Message
import com.example.cj.Models.User
import com.example.cj.databinding.DeleteDialogBinding
import com.example.cj.databinding.ItemReceiveGroupBinding
import com.example.cj.databinding.ItemSentGroupBinding
import java.util.ArrayList
import kotlin.Any as Any1

class GroupMessagesAdapter(var context: Context, var messages: ArrayList<Message>) :

    RecyclerView.Adapter<kotlin.Any1>() {
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_sent_group, parent, false)
            return SentViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_receive_group, parent, false)
            return ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if ((FirebaseAuth.getInstance().uid == message.senderId)) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: Any1, position: Int) {
        val message = messages[position]
        val reactions = intArrayOf(
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
        )
        val config = ReactionsConfigBuilder(context)
            .withReactions(reactions)
            .build()
        val popup = ReactionPopup(context, config) { pos: Int? ->
            if (holder.javaClass == SentViewHolder::class.java) {
                val viewHolder: SentViewHolder = holder as SentViewHolder
                viewHolder.binding.feeling.setImageResource(reactions.get((pos)!!))
                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
            } else {
                val viewHolder: ReceiverViewHolder = holder as ReceiverViewHolder
                viewHolder.binding.feeling.setImageResource(reactions.get((pos)!!))
                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
            }
            message.feeling = (pos)
            message.messageId?.let {
                FirebaseDatabase.getInstance().getReference()
                    .child("public")
                    .child(it).setValue(message)
            }
            true // true is closing popup, false is requesting a new selection
        }
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            if ((message.message == "photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            message.senderId?.let {
                FirebaseDatabase.getInstance()
                    .reference.child("users")
                    .child(it)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(
                                    User::class.java
                                )
                                viewHolder.binding.name.text = "@" + user!!.name
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            viewHolder.binding.message.text = message.message
            if (message.feeling >= 0) {
                viewHolder.binding.feeling.setImageResource(reactions[message.feeling])
                viewHolder.binding.feeling.visibility = View.VISIBLE
            } else {
                viewHolder.binding.feeling.visibility = View.GONE
            }
            viewHolder.binding.message.setOnTouchListener(OnTouchListener { v, event ->
                popup.onTouch(v, event)
                false
            })
            viewHolder.binding.image.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    popup.onTouch(v, event)
                    return false
                }
            })
            viewHolder.itemView.setOnLongClickListener(object : OnLongClickListener {
                override fun onLongClick(v: View): Boolean {
                    val view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
                    val binding = DeleteDialogBinding.bind(view)
                    val dialog = AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.root)
                        .create()
                    binding.everyone.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            message.message = "This message is removed."
                            message.feeling = -1
                            message.messageId?.let {
                                FirebaseDatabase.getInstance().reference
                                    .child("public")
                                    .child(it).setValue(message)
                            }
                            dialog.dismiss()
                        }
                    })
                    binding.delete.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            message.messageId?.let {
                                FirebaseDatabase.getInstance().reference
                                    .child("public")
                                    .child(it).setValue(null)
                            }
                            dialog.dismiss()
                        }
                    })
                    binding.cancel.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            dialog.dismiss()
                        }
                    })
                    dialog.show()
                    return false
                }
            })
        } else {
            val viewHolder = holder as ReceiverViewHolder
            if ((message.message == "photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            message.senderId?.let {
                FirebaseDatabase.getInstance()
                    .reference.child("users")
                    .child(it)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(
                                    User::class.java
                                )
                                viewHolder.binding.name.text = "@" + user!!.name
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            viewHolder.binding.message.text = message.message
            if (message.feeling >= 0) {
                //message.setFeeling(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[message.feeling])
                viewHolder.binding.feeling.visibility = View.VISIBLE
            } else {
                viewHolder.binding.feeling.visibility = View.GONE
            }
            viewHolder.binding.message.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    popup.onTouch(v, event)
                    return false
                }
            })
            viewHolder.binding.image.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    popup.onTouch(v, event)
                    return false
                }
            })
            viewHolder.itemView.setOnLongClickListener(object : OnLongClickListener {
                override fun onLongClick(v: View): Boolean {
                    val view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
                    val binding = DeleteDialogBinding.bind(view)
                    val dialog = AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.root)
                        .create()
                    binding.everyone.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            message.message = "This message is removed."
                            message.feeling = -1
                            message.messageId?.let {
                                FirebaseDatabase.getInstance().reference
                                    .child("public")
                                    .child(it).setValue(message)
                            }
                            dialog.dismiss()
                        }
                    })
                    binding.delete.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            message.messageId?.let {
                                FirebaseDatabase.getInstance().reference
                                    .child("public")
                                    .child(it).setValue(null)
                            }
                            dialog.dismiss()
                        }
                    })
                    binding.cancel.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            dialog.dismiss()
                        }
                    })
                    dialog.show()
                    return false
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemSentGroupBinding

        init {
            binding = ItemSentGroupBinding.bind(itemView)
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemReceiveGroupBinding

        init {
            binding = ItemReceiveGroupBinding.bind(itemView)
        }
    }
}
