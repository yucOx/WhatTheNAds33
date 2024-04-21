package com.yucox.pillpulse.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.yucox.whatthenads.Model.UserInfo
import com.yucox.whatthenads.View.MainActivity
import com.yucox.whatthenads.View.SelectActivity
import com.yucox.whatthenads.ViewModel.LoginViewModel
import com.yucox.whatthenads.databinding.LoginActivityBinding
import kotlinx.coroutines.cancel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private lateinit var viewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        if (viewModel.isAnyoneIn() == 1) {
            val intent = Intent(this, SelectActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.status.observe(this) { isSuccessful ->
            if (isSuccessful == 1) {
                Toast.makeText(
                    this,
                    "Giriş başarılı, hoş geldin!",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this, SelectActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) {
            if (it != null) {
                Toast.makeText(
                    this,
                    "$it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.loginBtn.setOnClickListener {
            val mail: String = binding.mailEt.text.toString()
            val pass: String = binding.passwordEt.text.toString()

            if (mail.isBlank() ||
                pass.isBlank()
            ) {
                Toast.makeText(
                    this@LoginActivity,
                    "Boş alanları doldurun",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            viewModel.updateUser(UserInfo("", "", mail), pass)
            viewModel.logIn()
        }

        binding.letmetoRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.contuniueAsGuest.setOnClickListener {
            val intent = Intent(this, SelectActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.viewModelScope.cancel()
    }
}