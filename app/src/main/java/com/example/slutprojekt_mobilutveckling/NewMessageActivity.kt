package com.example.slutprojekt_mobilutveckling

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)


        supportActionBar?.title = "Select User"
//
//        val adapter = GroupAdapter<ViewHolder>()
//
//        recyclerview_newmessage.adapter = adapter
//
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//
        fetchUsers()

    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    /**
     * fetchUsers function
     * Everytime onDataChange is ran, we loop through all of the children of our DataSnapshot, transform all data into users
     * put it inside of our adapter for our RecyclerView.
     */

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance("https://slutprojektmobilutveckling-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                snapshot.children.forEach {
                    Log.d("ADAPTER", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->

                    val useritem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //intent.putExtra(USER_KEY, useritem.user.username)
                    intent.putExtra(USER_KEY, useritem.user)
                    startActivity(intent)

                    finish() //clears backstack

                }

                recyclerview_newmessage.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}

class UserItem(val user: User) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.newmessage_username.text = user.username //sets username to the username in Firebase
        Picasso.get().load(user.profileImageURL).into(viewHolder.itemView.newmessage_imageview) //loads image URL from database, then caches it for easier load
    }

}