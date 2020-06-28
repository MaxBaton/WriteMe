package com.example.messengeryoutube.messages

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.example.messengeryoutube.CustomActionBar
import com.example.messengeryoutube.R
import com.example.messengeryoutube.databinding.ActivityLatestMessagesBinding
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
    private var editMenu: Menu? = null

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
            val interlocutorUser = item as LatestMessageItem
            val intent = Intent(this,ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.INTERLOCUTOR_USER,interlocutorUser.interlocutorUser)
            intent.putExtra(CURRENT_USER_KEY,currentUser)
            startActivity(intent)
        }
        updateToken(FirebaseInstanceId.getInstance().token)
    }

    override fun onStart() {
        super.onStart()
        fetchCurrentUser()
        listenForLatestMessages()
        val refStatus = FirebaseDatabase.getInstance().getReference("/users")
        refStatus.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                fillAndRefreshLatestMessagesRecyclerView(null,isStatusChange = true)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val a = 3 // without this is not working
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun listenForLatestMessages() {
        val fromUserId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromUserId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                fillAndRefreshLatestMessagesRecyclerView(p0)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                fillAndRefreshLatestMessagesRecyclerView(p0)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun fillAndRefreshLatestMessagesRecyclerView(p0: DataSnapshot?,isStatusChange: Boolean = false) {
            if (!isStatusChange) {
                val chatMessage = p0!!.getValue(ChatMessage::class.java) ?: return
                latestMessagesHashMap[p0.key!!] = chatMessage

                groupAdapter.clear()
                latestMessagesHashMap.values.sortedByDescending { it.timestamp }.forEach { groupAdapter.add(LatestMessageItem(it)) }
            }else {
                latestMessagesHashMap.values.sortedByDescending { it.timestamp }.forEach { _ -> groupAdapter.notifyDataSetChanged() }
            }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
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
        editMenu = menu
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
                    TODO("Not yet implemented")
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
