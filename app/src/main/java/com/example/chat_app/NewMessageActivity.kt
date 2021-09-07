package com.example.chat_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"
        fetchusers()


    }
    companion object{
        val USER_KEY="USER_KEY"
    }

    private fun fetchusers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.d("NewActivity", it.toString())

                    val user = it.getValue(User::class.java)
                    if (user != null)
                        adapter.add(UserItem(user))
                }
                adapter.setOnItemClickListener{  item ,view ->
                    val UserItem = item as UserItem
                    val intent=Intent(view.context,ChatLogActivity::class.java)
                    //intent.putExtra(USER_KEY,UserItem.user.username)
                    intent.putExtra(USER_KEY,UserItem.user)
                    startActivity(intent)
                    finish()
                }
                findViewById<RecyclerView>(R.id.recyclerview_newmessage).adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) = Unit

        })

    }
}



class UserItem(val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.username_new).text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.dp_viewedit))
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new
    }
}