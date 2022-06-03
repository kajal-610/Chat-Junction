package com.example.cj.Activities

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.app.ProgressDialog
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.View
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.example.cj.Activities.MainActivity
import com.example.cj.Models.User
import com.example.cj.databinding.ActivitySetupProfileBinding
import java.util.*

class SetupProfileActivity : AppCompatActivity() {
    var binding: ActivitySetupProfileBinding? = null
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Updating profile...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar!!.hide()
        binding!!.imageView.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }
        binding!!.continueBtn.setOnClickListener(View.OnClickListener {
            val name = binding!!.nameBox.text.toString()
            if (name.isEmpty()) {
                binding!!.nameBox.error = "Please type a name"
                return@OnClickListener
            }
            dialog!!.show()
            if (selectedImage != null) {
                val reference = storage!!.reference.child("Profiles").child(
                    auth!!.uid!!
                )
                reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val uid = auth!!.uid
                            val phone = auth!!.currentUser!!.phoneNumber
                            val name = binding!!.nameBox.text.toString()
                            val user = User(uid, name, phone, imageUrl)
                            database!!.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnSuccessListener {
                                    dialog!!.dismiss()
                                    val intent =
                                        Intent(this@SetupProfileActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                }
            } else {
                val uid = auth!!.uid
                val phone = auth!!.currentUser!!.phoneNumber
                val user = User(uid, name, phone, "No Image")
                database!!.reference
                    .child("users")
                    .child(uid!!)
                    .setValue(user)
                    .addOnSuccessListener {
                        dialog!!.dismiss()
                        val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.data != null) {
                val uri = data.data // filepath
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference.child("Profiles").child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val filePath = uri.toString()
                            val obj = HashMap<String, Any>()
                            obj["image"] = filePath
                            database!!.reference.child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener { }
                        }
                    }
                }
                binding!!.imageView.setImageURI(data.data)
                selectedImage = data.data
            }
        }
    }
}