package com.example.writeMe.messages.editProfile

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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.writeMe.*
import com.example.writeMe.databinding.ActivityEditProfileBinding
import com.example.writeMe.messages.latestMessages.LatestMessagesActivity
import com.example.writeMe.registration.MainActivity
import com.example.writeMe.registration.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var currentUser: User
    private var currentUserAuth = FirebaseAuth.getInstance().currentUser
    private var _dialog: AlertDialog? = null
    private var selectPhoto: Uri? = null
    private var isClickDeleteAccount: Boolean = false

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
                _dialog = createUpdateOrDeleteAccountAlertDialog(this@EditProfileActivity,"Обновление данных")
                updateData(userName,email,password,isClickDeleteAccount)
            }
            circleImageViewAvatarEditProfileFragment.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
            imageViewDeleteAvatar.setOnClickListener {
                Glide
                    .with(this@EditProfileActivity)
                    .load(MainActivity.ANONYMOUS_AVATAR_URL)
                    .into(circleImageViewAvatarEditProfileFragment)
                isClickDeleteAccount = true
            }
        }
    }

    private fun updateData(
        userName: String,
        email: String,
        password: String,
        isClickDeleteAccount: Boolean
    ) {
        runOnUiThread { _dialog?.show() }
        val uid = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        if(selectPhoto != null){
            val refAvatars = FirebaseStorage.getInstance().getReference("/avatars/${currentUserAuth!!.email}")
            refAvatars.putFile(selectPhoto!!)
                .addOnSuccessListener {
                    refAvatars.downloadUrl.addOnSuccessListener {
                        reference.child("imageUrl").setValue(it.toString())
                        runOnUiThread {
                            _dialog?.dismiss()
                            finish()
                        }
                    }
                }
        }else if (isClickDeleteAccount) {
            reference.child("imageUrl").setValue(MainActivity.ANONYMOUS_AVATAR_URL)
        }
        if (userName != currentUser.userName){
            reference.child("userName").setValue(userName)
                .addOnSuccessListener {
                    if (selectPhoto == null && email == currentUserAuth?.email
                        && password.isEmpty()){
                        runOnUiThread {
                            _dialog?.dismiss()
                            finish()
                        }
                    }
                }
                .addOnFailureListener { toastLong(it.message.toString()) }
        }
        var changeEmailOrAndPassword =  0
        if (email != currentUserAuth?.email) changeEmailOrAndPassword++
        if(password.isNotEmpty()) changeEmailOrAndPassword+=2
        if (changeEmailOrAndPassword > 0) {
            val dialog = createConfirmEditAccountAlertDialog(this,currentUserAuth!!,_dialog!!,
                                                        email,password,changeEmailOrAndPassword)
            dialog?.show()
        } else if (selectPhoto == null){
            _dialog?.dismiss()
            finish()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_profile,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
            R.id.menu_delete_account ->  createConfirmDeleteAccountAlertDialog(this,currentUser)!!.show()
        }
        return super.onOptionsItemSelected(item)
    }
}
