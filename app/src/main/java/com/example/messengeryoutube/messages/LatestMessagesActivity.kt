package com.example.messengeryoutube.messages

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.example.messengeryoutube.CustomActionBar
import com.example.messengeryoutube.R
import com.example.messengeryoutube.createPopupMenu
import com.example.messengeryoutube.databinding.ActivityLatestMessagesBinding
import com.example.messengeryoutube.notification.NotificationMessage
import com.example.messengeryoutube.notification.Token
import com.example.messengeryoutube.registration.MainActivity
import com.example.messengeryoutube.registration.User
import com.example.messengeryoutube.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message.view.*

class LatestMessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLatestMessagesBinding
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private val latestMessagesHashMap = HashMap<String,ChatMessage>()
    private var currentUser: User? = null

    companion object{
        val CURRENT_USER_KEY = "current user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verificationExistingUser()
        if (verificationExistingUser() != null) {
            finish()
            startActivity(verificationExistingUser())
            return
        }
        CustomActionBar.customActionBar(this,title = "Диалоги",isHomeButtonInlcude = false)
        val serviceIntent = Intent(this,CloseAppService::class.java)
        startService(serviceIntent)

        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)

            recyclerViewLatestMessages.adapter = groupAdapter
            recyclerViewLatestMessages.addItemDecoration(
                DividerItemDecoration(this@LatestMessagesActivity,DividerItemDecoration.VERTICAL)
            )
        }
        groupAdapter.setOnItemClickListener { item, _ ->
            val interlocutorUserInGroupAdapter = item as LatestMessageItem
            goOverInChat(interlocutorUserInGroupAdapter)
        }
        groupAdapter.setOnItemLongClickListener { item, view ->
            val interlocutorUserInGroupAdapter = item as LatestMessageItem

            val popupMenu = createPopupMenu(R.menu.popup_menu_latest_messages,view)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.go_over_in_chat -> {
                        goOverInChat(interlocutorUserInGroupAdapter)
                        true
                    }
                    R.id.delete_messages -> {
                        createAlertDialogConfirmDelete(interlocutorUserInGroupAdapter)!!.show()
                        true
                    }
                    else -> true
                }
            }
            popupMenu.show()
            true
        }
        updateToken(FirebaseInstanceId.getInstance().token)
    }

    private fun deleteCorrespondence(interlocutorUserInGroupAdapter: LatestMessagesActivity.LatestMessageItem) {
        val referenceLatestMessages = FirebaseDatabase.getInstance().getReference("/latest_messages/${currentUser!!.id}")
        val referenceUsersMessages = FirebaseDatabase.getInstance().getReference("/users_messages/${currentUser!!.id}")

        referenceLatestMessages.child(interlocutorUserInGroupAdapter.interlocutorUser!!.id).removeValue()
        referenceUsersMessages.child(interlocutorUserInGroupAdapter.interlocutorUser!!.id).removeValue()
    }

    private fun goOverInChat(interlocutorUserInGroupAdapter: LatestMessagesActivity.LatestMessageItem) {
        val intent = Intent(this,ChatLogActivity::class.java)
        intent.putExtra(NewMessageActivity.INTERLOCUTOR_USER,interlocutorUserInGroupAdapter.interlocutorUser)
        intent.putExtra(CURRENT_USER_KEY,currentUser)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        fetchCurrentUser()
        listenForLatestMessages()
        val refStatus = FirebaseDatabase.getInstance().getReference("/users")
        refStatus.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("LatesMessagesActivity","users canceled")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                fillAndRefreshLatestMessagesRecyclerView(null,isUserDataChange = true)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val a = 3 // without this is not working
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                fillAndRefreshLatestMessagesRecyclerView(null,isUserDataChange = true)
            }
        })
    }

    private fun createAlertDialogConfirmDelete(interlocutorUserInGroupAdapter: LatestMessageItem): AlertDialog? {
        return with(AlertDialog.Builder(this)) {
            setTitle("Подтверждение удаления")
            setPositiveButton("Подтвердить") { currentDialog, _ ->
                deleteCorrespondence(interlocutorUserInGroupAdapter)
                currentDialog.cancel()
            }
            setNegativeButton("Отмена") {currentDialog, _ ->
                currentDialog.cancel()
            }
            create()
        }
    }

    private fun listenForLatestMessages() {
        val fromUserId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromUserId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                fillAndRefreshLatestMessagesRecyclerView(p0)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("LatesMessagesActivity","users canceled")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                fillAndRefreshLatestMessagesRecyclerView(p0)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                fillAndRefreshLatestMessagesRecyclerView(p0,isCorrespondenceRemove = true)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NotificationMessage.NOTIFICATION_ID)
            }
        })
    }

    private fun fillAndRefreshLatestMessagesRecyclerView(p0: DataSnapshot?, isUserDataChange: Boolean = false,
                                                         isCorrespondenceRemove: Boolean = false) {
            if (!isUserDataChange && !isCorrespondenceRemove) {
                val chatMessage = p0!!.getValue(ChatMessage::class.java) ?: return
                latestMessagesHashMap[p0.key!!] = chatMessage

                groupAdapter.clear()
                latestMessagesHashMap.values.sortedByDescending { it.timestamp }.forEach { groupAdapter.add(LatestMessageItem(it)) }
            }else if (isUserDataChange && !isCorrespondenceRemove){
                latestMessagesHashMap.values.sortedByDescending { it.timestamp }.forEach { _ -> groupAdapter.notifyDataSetChanged() }
            }else if (isCorrespondenceRemove) {
                latestMessagesHashMap.remove(p0!!.key)

                groupAdapter.clear()
                latestMessagesHashMap.values.sortedByDescending { it.timestamp }.forEach { groupAdapter.add(LatestMessageItem(it)) }
            }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("LatesMessagesActivity","users canceled")
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
            }

        })
        ref.child("status").setValue("online")
    }

    private fun verificationExistingUser(): Intent? {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this@LatestMessagesActivity,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }else return null
    }


    private fun updateToken(refreshToken: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().getReference("/tokens")
        val token = Token(refreshToken!!)
        reference.child(currentUser!!.uid).setValue(token)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_latest_messages,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this@LatestMessagesActivity,NewMessageActivity::class.java)
                intent.putExtra(CURRENT_USER_KEY,currentUser)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                val reference = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().currentUser!!.uid}")
                reference.child("status").setValue("offline")
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@LatestMessagesActivity,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            R.id.menu_edit_profile -> {
                val intent = Intent(this,EditProfileActivity::class.java)
                intent.putExtra(CURRENT_USER_KEY,currentUser)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class LatestMessageItem(private val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
        var interlocutorUser: User? = null

        override fun getLayout() = R.layout.latest_message

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.text_view_latest_message_latest_messages_activity.text = chatMessage.text

            val currentId = if(chatMessage.fromUserId == FirebaseAuth.getInstance().uid) chatMessage.toUserId else chatMessage.fromUserId
            val ref = FirebaseDatabase.getInstance().getReference("/users/$currentId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("LatesMessagesActivity","users canceled")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    interlocutorUser = p0.getValue(User::class.java)
                    viewHolder.itemView.text_view_user_name_latest_messages_activity.text = interlocutorUser?.userName
                    viewHolder.itemView.circle_image_view_latest_messages_activity_status_user.visibility =
                        if (interlocutorUser!!.status == "online") View.VISIBLE else View.GONE
                    Glide
                        .with(this@LatestMessagesActivity)
                        .load(interlocutorUser?.imageUrl)
                        .into(viewHolder.itemView.circle_image_view_latest_messages_activity)
                }
            })
        }
    }
}
