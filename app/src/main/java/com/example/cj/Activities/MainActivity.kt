package com.example.cj.Activities

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.example.cj.Adapters.UsersAdapter
import com.example.cj.Adapters.TopStatusAdapter
import com.example.cj.Models.UserStatus
import android.app.ProgressDialog
import android.os.Bundle
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import android.graphics.drawable.Drawable
import android.graphics.drawable.ColorDrawable
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cj.R
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.storage.FirebaseStorage
import android.widget.Toast
import com.bumptech.glide.request.transition.Transition
import com.example.cj.Models.Status
import com.example.cj.Models.User
import com.example.cj.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null
    var database: FirebaseDatabase? = null
    var users: ArrayList<User?>? = null
    var usersAdapter: UsersAdapter? = null
    var statusAdapter: TopStatusAdapter? = null
    var userStatuses: ArrayList<UserStatus>? = null
    var dialog: ProgressDialog? = null
    var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener {
            val backgroundImage = mFirebaseRemoteConfig.getString("backgroundImage")
            Glide.with(this@MainActivity)
                .load(backgroundImage)
                .into(binding!!.backgroundImage)

            /* Toolbar Color */
            val toolbarColor = mFirebaseRemoteConfig.getString("toolbarColor")
            val toolBarImage = mFirebaseRemoteConfig.getString("toolbarImage")
            val isToolBarImageEnabled = mFirebaseRemoteConfig.getBoolean("toolBarImageEnabled")
            if (isToolBarImageEnabled) {
                Glide.with(this@MainActivity)
                    .load(toolBarImage)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            supportActionBar
                                ?.setBackgroundDrawable(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}

                    })
            } else {
                supportActionBar
                    ?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#009688")))
            }
        }
        database = FirebaseDatabase.getInstance()
        FirebaseMessaging.getInstance()
            .token
            .addOnSuccessListener { token ->
                val map = HashMap<String, Any>()
                map["token"] = token
                database!!.reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().uid!!)
                    .updateChildren(map)
                //Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
            }
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        users = ArrayList()
        userStatuses = ArrayList()
        database!!.reference.child("users").child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        usersAdapter = UsersAdapter(this, users)
        statusAdapter = TopStatusAdapter(this, userStatuses!!)
        //        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding!!.statusList.layoutManager = layoutManager
        binding!!.statusList.adapter = statusAdapter
        binding!!.recyclerView.adapter = usersAdapter
        binding!!.recyclerView.showShimmerAdapter()
        binding!!.statusList.showShimmerAdapter()
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(
                        User::class.java
                    )
                    if (user!!.uid != FirebaseAuth.getInstance().uid) users!!.add(user)
                }
                binding!!.recyclerView.hideShimmerAdapter()
                usersAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        database!!.reference.child("stories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userStatuses!!.clear()
                    for (storySnapshot in snapshot.children) {
                        val status = UserStatus()
                        status.name = storySnapshot.child("name").getValue(String::class.java)
                        status.profileImage = storySnapshot.child("profileImage").getValue(
                            String::class.java
                        )
                        status.lastUpdated = storySnapshot.child("lastUpdated").getValue(
                            Long::class.java
                        )!!
                        val statuses = ArrayList<Status?>()
                        for (statusSnapshot in storySnapshot.child("statuses").children) {
                            val sampleStatus = statusSnapshot.getValue(
                                Status::class.java
                            )
                            statuses.add(sampleStatus)
                        }
                        status.statuses = statuses
                        userStatuses!!.add(status)
                    }
                    binding!!.statusList.hideShimmerAdapter()
                    statusAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        binding!!.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.status -> {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(intent, 75)
                }
            }
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.data != null) {
                dialog!!.show()
                val storage = FirebaseStorage.getInstance()
                val date = Date()
                val reference = storage.reference.child("status").child(date.time.toString() + "")
                reference.putFile(data.data!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val userStatus = UserStatus()
                            userStatus.name = user!!.name
                            userStatus.profileImage = user!!.profileImage
                            userStatus.lastUpdated = date.time
                            val obj = HashMap<String, Any>()
                            obj["name"] = userStatus.name
                            obj["profileImage"] = userStatus.profileImage
                            obj["lastUpdated"] = userStatus.lastUpdated
                            val imageUrl = uri.toString()
                            val status = Status(imageUrl, userStatus.lastUpdated)
                            database!!.reference
                                .child("stories")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj)
                            database!!.reference.child("stories")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .child("statuses")
                                .push()
                                .setValue(status)
                            dialog!!.dismiss()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.group -> startActivity(Intent(this@MainActivity, GroupChatActivity::class.java))
            R.id.search -> Toast.makeText(this, "Search clicked.", Toast.LENGTH_SHORT).show()
            R.id.settings -> Toast.makeText(this, "Settings Clicked.", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.topmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}