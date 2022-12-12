package com.example.yourtrip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.yourtrip.R
import com.example.yourtrip.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun registerUser() {
        if (isFormValid()) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            ).addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.registrationToastMsg),
                    Toast.LENGTH_LONG
                ).show()
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.toastErrorMsg, it.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loginUser() {
        if (isFormValid()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            ).addOnSuccessListener {

                Toast.makeText(
                    this,
                    getString(R.string.toastLoginSuccessMsg),
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()

            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.toastErrorMsg, it.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun isFormValid(): Boolean {
        return when {
            binding.etEmail.text.isEmpty() -> {
                binding.etEmail.error = getString(R.string.nonEmptyErrorMsg)
                false
            }
            binding.etPassword.text.isEmpty() -> {
                binding.etPassword.error = getString(R.string.nonEmptyErrorMsg)
                false
            }
            else -> true
        }
    }
}