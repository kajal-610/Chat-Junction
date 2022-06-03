package com.example.cj.Activities

import androidx.appcompat.app.AppCompatActivity
import com.example.cj.Adapters.MessagesAdapter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.app.ProgressDialog
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.cj.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import android.content.Intent
import android.os.Handler
import android.text.TextWatcher
import android.text.Editable
import android.view.Menu
import android.view.View
import com.example.cj.Models.Message
import com.example.cj.databinding.ActivityChatBinding
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.OnCompleteListener
import java.util.*

class ChatActivity : AppCompatActivity() {
    var binding: ActivityChatBinding? = null
    var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message?>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null
    var token: String? = null
    var name: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        val token = intent.getStringExtra("token")

        //Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        binding!!.name.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.avatar)
            .into(binding!!.profile)
        binding!!.imageView2.setOnClickListener { finish() }
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (!status!!.isEmpty()) {
                            if (status == "Offline") {
                                binding!!.status.visibility = View.GONE
                            } else {
                                binding!!.status.text = status
                                binding!!.status.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this, messages, senderRoom, receiverRoom)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this)
        binding!!.recyclerView.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val message = snapshot1.getValue(
                            Message::class.java
                        )
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        binding!!.sendBtn.setOnClickListener {
            val messageTxt = binding!!.messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt, senderUid, date.time)
            binding!!.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message
            lastMsgObj["lastMsgTime"] = date.time
            database!!.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
            database!!.reference.child("chats")
                .child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener { }
                }
        }
        binding!!.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }
        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                database!!.reference.child("presence").child(senderUid!!).setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database!!.reference.child("presence").child(
                    senderUid!!
                ).setValue("Online")
            }
        })
        supportActionBar!!.setDisplayShowTitleEnabled(false)

//        getSupportActionBar().setTitle(name);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                if (data.data != null) {
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    val reference = storage!!.reference.child("chats")
                        .child(calendar.timeInMillis.toString() + "")
                    dialog!!.show()
                    reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                val filePath = uri.toString()
                                val messageTxt = binding!!.messageBox.text.toString()
                                val date = Date()
                                val message = Message(messageTxt, senderUid, date.time)
                                message.message = "photo"
                                message.imageUrl = filePath
                                binding!!.messageBox.setText("")
                                val randomKey = database!!.reference.push().key
                                val lastMsgObj = HashMap<String, Any>()
                                lastMsgObj["lastMsg"] = message.message
                                lastMsgObj["lastMsgTime"] = date.time
                                database!!.reference.child("chats").child(senderRoom!!)
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats").child(receiverRoom!!)
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats")
                                    .child(senderRoom!!)
                                    .child("messages")
                                    .child(randomKey!!)
                                    .setValue(message).addOnSuccessListener {
                                        database!!.reference.child("chats")
                                            .child(receiverRoom!!)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener { }
                                    }

                                //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(currentId!!).setValue("Offline")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}