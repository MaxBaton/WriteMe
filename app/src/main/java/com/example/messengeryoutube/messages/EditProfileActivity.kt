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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.messengeryoutube.CustomActionBar
import com.example.messengeryoutube.R
import com.example.messengeryoutube.databinding.ActivityEditProfileBinding
import com.example.messengeryoutube.registration.MainActivity
import com.example.messengeryoutube.registration.User
import com.example.messengeryoutube.toast
import com.example.messengeryoutube.toastLong
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var currentUser: User
    private var currentUserAuth = FirebaseAuth.getInstance().currentUser
    private var dialog: AlertDialog? = null
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
                dialog = createUpdateOrDeleteAlertDialog("Обновление данных")
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

    private fun createUpdateOrDeleteAlertDialog(title: String): AlertDialog? {
        return with(AlertDialog.Builder(this)) {
            setTitle(title)
            setView(layoutInflater.inflate(R.layout.registration_wait_alert_dialog,null))
            setCancelable(true)
            create()
        }
    }

    private fun updateData(
        userName: String,
        email: String,
        password: String,
        isClickDeleteAccount: Boolean
    ) {
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
        }else if (isClickDeleteAccount) {
            reference.child("imageUrl").setValue(MainActivity.ANONYMOUS_AVATAR_URL)
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
        var changeEmailOrAndPassword =  0
        if (email != currentUserAuth?.email) changeEmailOrAndPassword++
        if(password.isNotEmpty()) changeEmailOrAndPassword+=2
        if (changeEmailOrAndPassword > 0) {
            val dialog = createAlertDialogConfirmEdit(email,password,changeEmailOrAndPassword)
            dialog?.show()
        } else if (selectPhoto == null){
            dialog?.dismiss()
            finish()
        }
    }

    private fun createAlertDialogConfirmEdit(
        email: String,
        password: String,
        changeEmailOrAndPassword: Int
    ): AlertDialog? {
        return with(AlertDialog.Builder(this)) {
             setTitle("Подтверждение изменения")
            val view = layoutInflater.inflate(R.layout.confirm_edit_email_password_alert_dialog,null)
            setView(view)
            setCancelable(false)
            setPositiveButton("Подтвердить") { currentDialog, _ ->
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

    private fun createAlertDialogConfirmDelete(): AlertDialog? {
        return with(AlertDialog.Builder(this)) {
            setTitle("Подтверждение удаления")
            val positiveButton = setPositiveButton("Подтвердить") { currentDialog, _ ->
                currentDialog.cancel()

                //deleteAccount() //defer execution for now!!!

            }
            setNegativeButton("Отмена") {currentDialog, _ ->
                currentDialog.cancel()
            }
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

//    private fun deleteAccount() {
//        val deleteDialog = createUpdateOrDeleteAlertDialog("Удаление аккаунта")
//        deleteDialog!!.show()
//        val allCurrentUserReferences = mutableListOf<DatabaseReference>()
//        val refUser = FirebaseDatabase.getInstance().getReference("/users").child(currentUser.id)
//        val refTokens = FirebaseDatabase.getInstance().getReference("/tokens").child(currentUser.id)
//        val refUserInChat = FirebaseDatabase.getInstance().getReference("/user_in_chat").child(currentUser.id)
//        val refUserMessages = FirebaseDatabase.getInstance().getReference("/users_messages").child(currentUser.id)
//        val refCurrentUserLatestMessages = FirebaseDatabase.getInstance().getReference("/latest_messages").child(currentUser.id)
//
//        val listOfInterlocutors = mutableListOf<String>()
//        val listOfUserInChatWithCurrentUser = mutableListOf<String>()
//
//        refUserInChat.addListenerForSingleValueEvent(object: ValueEventListener{
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                listOfUserInChatWithCurrentUser.add(snapshot.key!!)
//            }
//        })
//
//        readData(refCurrentUserLatestMessages,object: OnGetDataListener{
//            override fun onSuccess(dataSnapshot: DataSnapshot) {
//                dataSnapshot.children.forEach { listOfInterlocutors.add(it.key!!) }
//                Log.d("onGetDataListener","Success!!! Data is retrieved")
//                listOfInterlocutors.forEach {
//                    val ref = FirebaseDatabase.getInstance().getReference("/latest_messages").child(it).child(currentUser.id)
//                    allCurrentUserReferences.add(ref)
//                }
//                allCurrentUserReferences.add(refUser)
//                allCurrentUserReferences.add(refTokens)
//                allCurrentUserReferences.add(refUserInChat)
//                allCurrentUserReferences.add(refUserMessages)
//                allCurrentUserReferences.add(refCurrentUserLatestMessages)
//
//                allCurrentUserReferences.forEach {
//                    it.removeValue()
//                }
//
//                if (currentUser.imageUrl != MainActivity.ANONYMOUS_AVATAR_URL) {
//                    FirebaseStorage.getInstance().getReference("/avatars/${FirebaseAuth.getInstance().currentUser!!.email}").delete()
//                }
//                FirebaseAuth.getInstance().currentUser!!.delete()
//                //FirebaseAuth.getInstance().signOut()
//                val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                finish()
//            }
//
//            override fun onStart() {
//                Log.d("onGetDataListener","onStart is running")
//            }
//
//            override fun onFailure() {
//                Log.d("onGetDataListener","onFailure is running")
//            }
//
//        })
//    }

    private fun readData(reference: DatabaseReference,listener: OnGetDataListener) {
        listener.onStart()
        reference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                listener.onFailure()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                listener.onSuccess(snapshot)
            }

        })
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
            R.id.menu_delete_account ->  createAlertDialogConfirmDelete()!!.show() //so far very bad
        }
        return super.onOptionsItemSelected(item)
    }
}
