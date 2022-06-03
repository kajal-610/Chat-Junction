package com.example.cj.Activities

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.app.ProgressDialog
import android.os.Bundle
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider
import com.mukesh.OnOtpCompletionListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import android.widget.Toast
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import com.example.cj.Activities.SetupProfileActivity
import com.example.cj.databinding.ActivityOtpactivityBinding
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    var binding: ActivityOtpactivityBinding? = null
    var auth: FirebaseAuth? = null
    var verificationId: String? = null
    var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Sending OTP...")
        dialog!!.setCancelable(false)
        dialog!!.show()
        auth = FirebaseAuth.getInstance()
        supportActionBar!!.hide()
        val phoneNumber = intent.getStringExtra("phoneNumber")
        binding!!.phoneLbl.text = "Verify $phoneNumber"
        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this@OTPActivity)
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {}
                override fun onVerificationFailed(e: FirebaseException) {}
                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: ForceResendingToken
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog!!.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding!!.otpView.requestFocus()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        binding!!.otpView.setOtpCompletionListener { otp ->
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            auth!!.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@OTPActivity, "Logged in", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@OTPActivity, SetupProfileActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    Toast.makeText(this@OTPActivity, "Failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}