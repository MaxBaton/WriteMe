package com.example.messengeryoutube.registration

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.messengeryoutube.*
import com.example.messengeryoutube.databinding.ActivityMainBinding
import com.example.messengeryoutube.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private  var selectPhotoUri: Uri? = null
    private var dialog: AlertDialog? = null
    private var job: Job? = null

    companion object{
        const val ANONYMOUS_AVATAR_URL = "https://iptc.org/wp-content/uploads/2018/05/avatar-anonymous-300x300.png"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CustomActionBar.customActionBar(this,title = "Регистрация",isHomeButtonInlcude = false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)

            btnRegister.setOnClickListener {
                val email = editTextEmailRegistration.text.toString()
                val password = editTextPasswordRegistration.text.toString()
                val userName = editTextUsernameRegistration.text.toString()

                if (email.isEmpty() || password.isEmpty() || userName.isEmpty()){
                    val failedMessage = if (email.isEmpty()){
                        editTextEmailRegistration.requestFocus()
                        "email"
                    }else if (password.isEmpty()){
                        editTextPasswordRegistration.requestFocus()
                        "пароль"
                    }else {
                        editTextUsernameRegistration.requestFocus()
                        "имя пользователя"
                    }
                    toast("Введите $failedMessage ")
                    return@setOnClickListener
                }

                createAlertDialog()
                dialog?.show()
                job = GlobalScope.launch(Dispatchers.IO) { performRegister(email = email,password = password) }
            }

            textViewAlreadyRegistered.setOnClickListener {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            Glide
                .with(this@MainActivity)
                .load(ANONYMOUS_AVATAR_URL)
                .into(circleImageViewAvatarMainActivity)

            circleImageViewAvatarMainActivity.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent,0)
            }
        }
    }

    private fun createAlertDialog() {
        with(AlertDialog.Builder(this@MainActivity)) {
            setTitle("Регистрация пользователя")
            setView(layoutInflater.inflate(R.layout.registration_wait_alert_dialog,null))
            setCancelable(true)
            dialog = create()
        }
    }

    private fun performRegister(email:String,password:String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                uploadImageInFirebaseStorage(email)
            }
            .addOnFailureListener { toastLong("Ошибка регистрации ${it.message}") }
    }

    private fun registerUserOnDatabase(imageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, binding.editTextUsernameRegistration.text.toString(), imageUrl)
        ref.setValue(user)
        job!!.cancel()
        dialog?.dismiss()

        val intent = Intent(this@MainActivity,LatestMessagesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun uploadImageInFirebaseStorage(email: String) {
        if (selectPhotoUri == null){
            registerUserOnDatabase(ANONYMOUS_AVATAR_URL)
        }else {
            val ref = FirebaseStorage.getInstance().getReference("/avatars/$email")
            ref.putFile(selectPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        registerUserOnDatabase(it.toString())
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectPhotoUri = data.data

            val bitmap = if(Build.VERSION.SDK_INT < 28){
                 MediaStore.Images.Media.getBitmap(contentResolver,selectPhotoUri)
            }else {
                val source = ImageDecoder.createSource(contentResolver,selectPhotoUri!!)
                ImageDecoder.decodeBitmap(source)
            }

            binding.circleImageViewAvatarMainActivity.setImageBitmap(bitmap)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_reference) {
            val dialogReference = createReferenceAlertDialog()
            dialogReference.show()
        }
        return super.onOptionsItemSelected(item)
    }
}
