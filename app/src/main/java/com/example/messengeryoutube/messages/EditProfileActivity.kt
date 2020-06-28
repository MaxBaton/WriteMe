package com.example.messengeryoutube.messages

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.messengeryoutube.CustomActionBar
import com.example.messengeryoutube.R
import com.example.messengeryoutube.databinding.ActivityEditProfileBinding
import com.example.messengeryoutube.registration.User
import com.example.messengeryoutube.toast
import com.example.messengeryoutube.toastLong
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Job

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var currentUser: User
    private var currentUserAuth = FirebaseAuth.getInstance().currentUser
    private var dialog: AlertDialog? = null
    private var selectPhoto: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        CustomActionBar.customActionBar(this,title = "Профиль",isHomeButtonInlcude = true)
        currentUser = intent.getParcelableExtra(LatestMessagesActivity.CURRENT_USER_KEY)!!
        fillWithData()
        with(binding) {
            setContentView(binding.root)
            btnEditProfile.setOnClickListener {
                val email = editTextEmailEditProfile.text.toString()
                val password = editTextPasswordEditProfile.text.toString()
                val userName = editTextUsernameEditProfile.text.toString()
                createAlertDialog()
                updateData(userName,email,password)
            }
            circleImageViewAvatarEditProfileFragment.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
    }

    private fun createAlertDialog() {
        with(AlertDialog.Builder(this)) {
            setTitle("Обновление данных")
            setView(layoutInflater.inflate(R.layout.registration_wait_alert_dialog,null))
            setCancelable(true)
            dialog = create()
        }
    }

    private fun updateData(userName: String, email: String, password: String) {
        runOnUiThread { dialog?.show() }
        val uid = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        if(selectPhoto != null){
            val refAvatars = FirebaseStorage.getInstance().getReference("/avatars/${currentUserAuth!!.email}")
            refAvatars.putFile(selectPhoto!!)
                .addOnSuccessListener {
                    refAvatars.downloadUrl.addOnSuccessListener {
                        reference.child("imageUrl").setValue(it.toString())
                        runOnUiThread {
                            dialog?.dismiss()
                            finish()
                        }
                    }
                }
        }
        var changeEmailOrAndPassword =  0
        if (email != currentUserAuth?.email) changeEmailOrAndPassword++
        if(password.isNotEmpty()) changeEmailOrAndPassword+=2
        if (changeEmailOrAndPassword > 0) {
            val dialog = createAlertDialogConfirm(email,password,changeEmailOrAndPassword)
            dialog?.show()
        }
        if (userName != currentUser.userName){
            reference.child("userName").setValue(userName)
                .addOnSuccessListener {
                    if (selectPhoto == null && email == currentUserAuth?.email
                        && password.isEmpty()){
                        runOnUiThread {
                            dialog?.dismiss()
                            finish()
                        }
                    }
                }
                .addOnFailureListener { toastLong(it.message.toString()) }
        }
    }

    private fun createAlertDialogConfirm(
        email: String,
        password: String,
        changeEmailOrAndPassword: Int
    ): AlertDialog? {
        return with(AlertDialog.Builder(this)) {
             setTitle("Подтверждение изменения")
            val view = layoutInflater.inflate(R.layout.confirm_edit_email_password_alert_dialog,null)
            setView(view)
            setCancelable(false)
            setPositiveButton("Подтвердить") { currentDialog, which ->
                                       currentUserAuth?.let {
                                           val currentEmail = currentUserAuth!!.email!!
                                           val editTextPassword = view.findViewById<EditText>(R.id.edit_text_confirm_password)
                                           val currentPassword = editTextPassword.text.toString().trim()
                                           val credential = EmailAuthProvider.getCredential(currentEmail,currentPassword)

                                           it.reauthenticate(credential)
                                               .addOnCompleteListener {
                                                   currentDialog.cancel()
                                                   when(changeEmailOrAndPassword) {
                                                       1 -> {
                                                           currentUserAuth!!.updateEmail(email)
                                                               .addOnCompleteListener {
                                                                   toast("complete updating email")
                                                                   runOnUiThread {
                                                                       dialog?.cancel()
                                                                       finish()
                                                                   }
                                                               }
                                                       }
                                                       2 -> {
                                                           currentUserAuth!!.updatePassword(password)
                                                               .addOnCompleteListener {
                                                                   toast("complete updating password")
                                                                   runOnUiThread {
                                                                       dialog?.cancel()
                                                                       finish()
                                                                   }
                                                               }
                                                       }
                                                       3 -> {
                                                           currentUserAuth!!.updateEmail(email)
                                                               .addOnCompleteListener {
                                                                   toast("complete updating email")
                                                                   currentUserAuth!!.updatePassword(password)
                                                                       .addOnCompleteListener {
                                                                           toast("complete updating password")
                                                                           runOnUiThread {
                                                                               dialog?.cancel()
                                                                               finish()
                                                                           }
                                                                       }
                                                               }
                                                       }
                                                   }
                                               }
                                               .addOnFailureListener { toast(it.message!!) }
                                    }
            }
            setNegativeButton("Отмена",object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    toast("click")
                }

            })
            create()
         }
    }

    private fun fillWithData() {
        val imageUrl = if (currentUser.imageUrl != "") currentUser.imageUrl else R.drawable.edit_anonymous
        Glide
            .with(this)
            .load(imageUrl)
            .into(binding.circleImageViewAvatarEditProfileFragment)
        binding.editTextUsernameEditProfile.setText(currentUser.userName, TextView.BufferType.EDITABLE)
        binding.editTextEmailEditProfile.setText(currentUserAuth?.email, TextView.BufferType.EDITABLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectPhoto = data.data

            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, selectPhoto)
            } else {
                val source = ImageDecoder.createSource(contentResolver, selectPhoto!!)
                ImageDecoder.decodeBitmap(source)
            }
            binding.circleImageViewAvatarEditProfileFragment.setImageBitmap(bitmap)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
