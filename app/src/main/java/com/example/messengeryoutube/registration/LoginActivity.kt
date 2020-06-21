package com.example.messengeryoutube.registration

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.messengeryoutube.R
import com.example.messengeryoutube.databinding.ActivityLoginBinding
import com.example.messengeryoutube.messages.LatestMessagesActivity
import com.example.messengeryoutube.toast
import com.example.messengeryoutube.toastLong
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tuneActionBar()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)

            btnLogIn.setOnClickListener {
                val email = editTextEmailRegistration.text.toString()
                val password = editTextPasswordRegistration.text.toString()

                if (email.isEmpty() || password.isEmpty()){
                    val failedMessage = if (email.isEmpty()){
                        editTextEmailRegistration.requestFocus()
                        "email"
                    }else {
                        editTextPasswordRegistration.requestFocus()
                        "пароль"
                    }
                    toast("Введите $failedMessage ")
                    return@setOnClickListener
                }
                createAlertDialog()
                dialog?.show()
                performLogIn(email = email,password = password)
            }
        }
    }

    private fun createAlertDialog() {
        with(AlertDialog.Builder(this@LoginActivity)) {
            setTitle("Аутентификация пользователя")
            setView(layoutInflater.inflate(R.layout.registration_wait_alert_dialog,null))
            setCancelable(true)
            dialog = create()
        }
    }

    private fun tuneActionBar() {
        supportActionBar?.title = "Вход"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(window.statusBarColor))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun performLogIn(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                dialog?.dismiss()
                toast("Успешный вход")
                val intent = Intent(this@LoginActivity, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { toastLong("Ошибка входа ${it.message}") }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
