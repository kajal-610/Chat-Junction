package com.example.cj.Activities

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.content.Intent
import com.example.cj.Activities.MainActivity
import com.example.cj.Activities.OTPActivity
import com.example.cj.databinding.ActivityPhoneNumberBinding

class PhoneNumberActivity : AppCompatActivity() {
    var binding: ActivityPhoneNumberBinding? = null
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser != null) {
            val intent = Intent(this@PhoneNumberActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        supportActionBar!!.hide()
        binding!!.phoneBox.requestFocus()
        binding!!.continueBtn.setOnClickListener {
            val intent = Intent(this@PhoneNumberActivity, OTPActivity::class.java)
            intent.putExtra("phoneNumber", binding!!.phoneBox.text.toString())
            startActivity(intent)
        }
    }
}