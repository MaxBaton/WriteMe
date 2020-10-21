package com.example.writeMe.registration

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.writeMe.CustomActionBar
import com.example.writeMe.R
import com.example.writeMe.databinding.ActivityLoginBinding
import com.example.writeMe.messages.latestMessages.LatestMessagesActivity
import com.example.writeMe.toast
import com.example.writeMe.toastLong
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CustomActionBar.customActionBar(this,title = "Вход",isHomeButtonInlcude = true)

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

    private fun performLogIn(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                dialog?.dismiss()
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
