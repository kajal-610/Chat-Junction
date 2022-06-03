package com.example.cj.Activities

import androidx.appcompat.app.AppCompatActivity
import com.example.cj.Adapters.GroupMessagesAdapter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.app.ProgressDialog
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.content.Intent
import com.example.cj.Models.Message
import com.example.cj.databinding.ActivityGroupChatBinding
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import java.util.*

class GroupChatActivity : AppCompatActivity() {
    var binding: ActivityGroupChatBinding? = null
    var adapter: GroupMessagesAdapter? = null
    var messages: ArrayList<Message?>? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar!!.title = "Group Chat"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        senderUid = FirebaseAuth.getInstance().uid
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        adapter = GroupMessagesAdapter(this, messages)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this)
        binding!!.recyclerView.adapter = adapter
        database!!.reference.child("public")
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
            database!!.reference.child("public")
                .push()
                .setValue(message)
        }
        binding!!.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }
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
                                database!!.reference.child("public")
                                    .push()
                                    .setValue(message)
                                //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}