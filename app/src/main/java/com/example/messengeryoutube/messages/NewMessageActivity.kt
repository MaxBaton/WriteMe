package com.example.messengeryoutube.messages

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.example.messengeryoutube.R
import com.example.messengeryoutube.databinding.ActivityNewMessageBinding
import com.example.messengeryoutube.registration.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.select_user.view.*

class NewMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewMessageBinding
    private var currentUser: User? = null
    private val groupAdapter =  GroupAdapter<GroupieViewHolder>()
    private val listOfUsers = mutableListOf<User>()

    companion object{
        const val INTERLOCUTOR_USER = "interlocutor user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tuneActionBar()

        currentUser = intent.getParcelableExtra(LatestMessagesActivity.CURRENT_USER_KEY)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
        }

        fetchUser()
    }

    override fun onStart() {
        super.onStart()
        val refStatus = FirebaseDatabase.getInstance().getReference("/users")
        refStatus.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                if (p0.key != currentUser!!.id) {
                    val user = p0.getValue(User::class.java)
                    fetchUser(isStatusChange = true,userFromChangeStatus = user)
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val a = 3
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun fetchUser(isStatusChange: Boolean = false, userFromChangeStatus: User? = null) {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val id = FirebaseAuth.getInstance().uid

                p0.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user?.id != id) {
                        if (!isStatusChange) {
                            groupAdapter.add(UserItem(user!!))
                            listOfUsers.add(user)
                        } else {
                            var position = 0
                            listOfUsers.forEachIndexed { index, _user ->
                                if (_user.id == userFromChangeStatus!!.id) {
                                    position = index
                                    return@forEachIndexed
                                }
                            }
                            groupAdapter.notifyItemChanged(position)
                        }
                    }
                }
                groupAdapter.setOnItemClickListener { item, _ ->
                    val userItem = item as UserItem
                    val intent = Intent(this@NewMessageActivity,ChatLogActivity::class.java)
                    intent.putExtra(INTERLOCUTOR_USER,userItem.user)
                    intent.putExtra(LatestMessagesActivity.CURRENT_USER_KEY,currentUser)
                    startActivity(intent)
                    finish()
                }

                binding.recyclerViewSelectUser.adapter = groupAdapter
                binding.recyclerViewSelectUser.addItemDecoration(
                    DividerItemDecoration(this@NewMessageActivity, DividerItemDecoration.VERTICAL))
            }
        })
    }

    private fun tuneActionBar() {
        supportActionBar?.title = "Выбор пользователя"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(window.statusBarColor))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    inner class UserItem(val user: User): Item<GroupieViewHolder>(){
        override fun getLayout() = R.layout.select_user

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.text_view_user_name.text = user.userName
            Glide
                .with(this@NewMessageActivity)
                .load(user.imageUrl)
                .into(viewHolder.itemView.circle_image_view_select_user)
            val ref = FirebaseDatabase.getInstance().getReference("/users/${user.id}")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val interlocutor = p0.getValue(User::class.java)
                    viewHolder.itemView.circle_image_view_new_message_activity_status_user.visibility =
                        if (interlocutor!!.status == "online") View.VISIBLE else View.GONE
                }
            })
        }
    }
}
