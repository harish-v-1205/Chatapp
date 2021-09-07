package com.example.chat_app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val Tag = "Chatlog"
    }
    var toUser: User?= null
    val adapter = GroupAdapter<ViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        findViewById<RecyclerView>(R.id.recyclerview_chatlog).adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username
        //setupdummydata()
        ListenForMessages()

        findViewById<Button>(R.id.send_button_chatlog).setOnClickListener {
            Log.d(Tag, "attempt to send a message")
            perfomsendmessage()
        }
    }


    private fun ListenForMessages() {
        val fromID=FirebaseAuth.getInstance().uid
        val toID=toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toID")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {

                    Log.d(Tag, chatMessage.text)

                    if (chatMessage.fromID == FirebaseAuth.getInstance().uid) {
                        val currentUser=MainScreen.CurrentUser
                        adapter.add(ChatToItem(chatMessage.text,currentUser!!))
                    } else {
                        adapter.add(ChatFromItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun perfomsendmessage() {
        val text = findViewById<EditText>(R.id.enter_message_chatlog).text.toString()
        val fromID = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toID = user?.uid
        if (fromID == null) return

        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toID")
            .push()
        val toreference = FirebaseDatabase.getInstance().getReference("/user-messages/$toID/$fromID")
            .push()
        val chatMessage =
            ChatMessage(reference.key!!, text, fromID, toID!!, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(Tag, "Saved our message ${reference.key}")
                findViewById<EditText>(R.id.enter_message_chatlog).text.clear()
                findViewById<RecyclerView>(R.id.recyclerview_chatlog).scrollToPosition(adapter.itemCount -1)
            }
        toreference.setValue(chatMessage)
    }

}
class ChatToItem(val text:String,val user:User) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.to_msg_chatlog).text=text
        val uri =user.profileImageUrl
        val targetImageView=viewHolder.itemView.findViewById<ImageView>(R.id.imageViewto)
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

class ChatFromItem(val text:String,val user:User) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.from_msg_chatlog).text=text

        val uri =user.profileImageUrl
        val targetImageView=viewHolder.itemView.findViewById<ImageView>(R.id.imageViewfrom)
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}