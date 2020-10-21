package com.example.writeMe.messages.chatLog

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.writeMe.*
import com.example.writeMe.R
import com.example.writeMe.databinding.ActivityChatLogBinding
import com.example.writeMe.messages.newMessages.NewMessageActivity
import com.example.writeMe.messages.latestMessages.LatestMessagesActivity
import com.example.writeMe.notification.*
import com.example.writeMe.registration.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.interlocutor_message_in_chat.view.*
import kotlinx.android.synthetic.main.my_message_in_chat.view.*


class ChatLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatLogBinding
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var interlocutorUser: User? = null
    private var currentUser: User? = null
    private lateinit var apiService: APIService
    private var notify: Boolean = false
    private var isInterlocutorInChat: Boolean = false
    private val listOfMessages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interlocutorUser = intent.getParcelableExtra(NewMessageActivity.INTERLOCUTOR_USER)
        currentUser = intent.getParcelableExtra(LatestMessagesActivity.CURRENT_USER_KEY)
        apiService = Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)
        CustomActionBar.customActionBar(this,title = interlocutorUser!!.userName,isHomeButtonInlcude = true)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)

            btnSendMessage.setOnClickListener {
                notify = true
                val text = editTextPutMessage.text.toString()
                if (text.isEmpty()){
                    editTextPutMessage.requestFocus()
                    toast("Введите сообщение")
                }else{
                    performSendMessage(text)
                }
            }
            recyclerViewChat.adapter = groupAdapter
        }

        groupAdapter.setOnItemLongClickListener { item, view ->
            val popupMenu = createPopupMenu(R.menu.popup_menu_chat_log_activity,view)
            val isInterlocutorClass = if (item::class.java == InterlocutorChatItem::class.java) {
                popupMenu.menu.getItem(1).isVisible = false
                popupMenu.menu.getItem(2).isVisible = false
                true
            }else false
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.delete_from_me -> {
                        val dialog = createDeleteMessageFromMeAlertDialog(currentUser!!,interlocutorUser!!,listOfMessages,
                                                                            item,isInterlocutorClass)
                        dialog.show()

                        true
                    }
                    R.id.delete_from_both -> {
                        val dialog = createDeleteMessageFromBothAlertDialog(currentUser!!,interlocutorUser!!,listOfMessages,
                                                                                item)
                        dialog.show()

                        true
                    }
                    R.id.edit_message -> {
                        val view = layoutInflater.inflate(R.layout.confirm_edit_message_in_chat,null)
                        val editTextEditMessage = view.findViewById<EditText>(R.id.edit_text_confirm_edit_message)
                        val oldText = (item as MyChatItem).text
                        editTextEditMessage.setText(oldText, TextView.BufferType.EDITABLE)

                        val dialog = createEditMessageAlertDialog(view,currentUser!!,interlocutorUser!!,editTextEditMessage,item)
                        dialog.show()

                        true
                    }
                    else -> true
                }
            }
            true
        }

        listenForMessages()
    }

    override fun onStart() {
        super.onStart()
        val refUser = FirebaseDatabase.getInstance().getReference("/users")
        refUser.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatLogActivity","user is canceled")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ChatLogActivity","user change")
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ChatLogActivity","new user add")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user!!.id == interlocutorUser!!.id) {
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationMessage.NOTIFICATION_ID)
        setUserInChatFlag(true)

        val referenceInterlocutorInChat = FirebaseDatabase.getInstance().getReference("/user_in_chat").child(interlocutorUser!!.id)
        referenceInterlocutorInChat.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("ChatLogActivity","user in chat canceled")
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (p in p0.children) {
                    if (p.key == currentUser!!.id) {
                        isInterlocutorInChat = p.getValue(Boolean::class.java)!!
                    }
                }
            }
        })
    }

    private fun setUserInChatFlag(isUserInChat: Boolean) {
        val refUserInChat = FirebaseDatabase.getInstance().getReference("/user_in_chat")
        refUserInChat.child(currentUser!!.id).child(interlocutorUser!!.id).setValue(isUserInChat)
    }

    private fun listenForMessages() {
        val fromUserId = currentUser!!.id
        val interlocutorUserId = interlocutorUser!!.id
        val ref = FirebaseDatabase.getInstance().getReference("/users_messages/$fromUserId/$interlocutorUserId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                listOfMessages.add(chatMessage!!)
                if (chatMessage.fromUserId == FirebaseAuth.getInstance().uid){
                    groupAdapter.add(MyChatItem(chatMessage.text,chatMessage.id))
                }else {
                    groupAdapter.add(InterlocutorChatItem(chatMessage.text,chatMessage.id))
                }
                binding.recyclerViewChat.scrollToPosition(groupAdapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("ChatLogActivity","users canceled")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(ChatMessage::class.java)
                listOfMessages.forEachIndexed { index, chatMessage ->
                    if (chatMessage.id == message!!.id){
                        listOfMessages[index] = message
                        refreshRecyclerView()
                        return
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                val message = p0.getValue(ChatMessage::class.java)
                DeleteMessageFromList.delete(listOfMessages, message!!.id)

                refreshRecyclerView()
            }
        })
    }

    private fun refreshRecyclerView() {
        groupAdapter.clear()

        listOfMessages.forEach {
            if (it.fromUserId == currentUser!!.id) {
                groupAdapter.add(MyChatItem(it.text,it.id))
            }else {
                groupAdapter.add(InterlocutorChatItem(it.text,it.id))
            }
        }
    }

    private fun performSendMessage(text: String) {
        val fromUserId = currentUser!!.id
        val interlocutorUserId = interlocutorUser!!.id
        val currentUserReference = FirebaseDatabase.getInstance().getReference("/users_messages/$fromUserId/$interlocutorUserId").push()
        val interlocutorUserReference = FirebaseDatabase.getInstance().getReference("/users_messages/$interlocutorUserId/$fromUserId").push()
        if (currentUserReference.key == null) return

        val timestamp = System.currentTimeMillis()/1000
        val chatMessage = ChatMessage(currentUserReference.key!!,text, fromUserId,interlocutorUserId,timestamp)
        currentUserReference.setValue(chatMessage).addOnSuccessListener {
            binding.editTextPutMessage.text.clear()
            binding.recyclerViewChat.scrollToPosition(groupAdapter.itemCount - 1)
        }
        interlocutorUserReference.setValue(chatMessage)

        val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromUserId/$interlocutorUserId")
        val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest_messages/$interlocutorUserId/$fromUserId")
        latestMessageReference.setValue(chatMessage)
        latestMessageToReference.setValue(chatMessage)
        val message = chatMessage.text

        val reference = FirebaseDatabase.getInstance().getReference("/users").child(FirebaseAuth.getInstance().currentUser!!.uid)
        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("ChatLogActivity","users canceled")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                if (notify && !isInterlocutorInChat) {
                    SendNotification.send(
                        this@ChatLogActivity, apiService, currentUser!!, interlocutorUser!!,
                        receiver = interlocutorUserId, userName = user!!.userName, message = message
                    )
                }
                notify = false;
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        setUserInChatFlag(false)
    }

    inner class InterlocutorChatItem(val text: String,val id: String) : Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.interlocutor_message_in_chat

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.text_view_my_message_in_chat.text = text

            Glide
                .with(this@ChatLogActivity)
                .load(interlocutorUser!!.imageUrl)
                .into(viewHolder.itemView.circle_image_view_my_avatar_in_chat)
        }
    }

    inner class MyChatItem(val text: String,val id: String) : Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.my_message_in_chat

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.text_view_interlocutor_message_in_chat.text = text

            Glide
                .with(this@ChatLogActivity)
                .load(currentUser!!.imageUrl)
                .into(viewHolder.itemView.circle_image_view_interlocutor_avatar_in_chat)
        }
    }
}
