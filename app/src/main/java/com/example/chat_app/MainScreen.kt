package com.example.chat_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class MainScreen : AppCompatActivity() {

    companion object{
        var CurrentUser:User? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        findViewById<RecyclerView>(R.id.latest_messages_recyclerview).adapter= adapter
        findViewById<RecyclerView>(R.id.latest_messages_recyclerview).addItemDecoration(DividerItemDecoration
            (this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            Log.d("MainActivity","User clicked from latest messages")
            val intent=Intent(this,ChatLogActivity::class.java)
            val row= item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }
        //setupDummyRows()
        listenForLatestMessages()
        fetchCurrentUser()
        verifylogin()



    }
    val adapter = GroupAdapter<ViewHolder>()

    val LatestMessagesMap= HashMap<String,ChatMessage>()

    private fun refreshRecyclerviewMessages(){
        adapter.clear()
        LatestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it)) }
    }

    private fun listenForLatestMessages(){
        val fromID=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java) ?: return
                LatestMessagesMap[p0.key!!]=chatMessage
                refreshRecyclerviewMessages()
               // adapter.add(LatestMessageRow(chatMessage))

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java) ?: return
                LatestMessagesMap[p0.key!!]=chatMessage
                refreshRecyclerviewMessages()
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })


    }
    class LatestMessageRow(val chatMessage: ChatMessage) : Item<ViewHolder>(){
        var chatPartnerUser :User?= null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.latestmessage_mainscreen).text = chatMessage.text

            val chatPartnerId : String
            if(chatMessage.fromID == FirebaseAuth.getInstance().uid)
                 chatPartnerId=chatMessage.toID
             else
                chatPartnerId=chatMessage.fromID
            val ref=FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser=p0.getValue(User::class.java)
                    viewHolder.itemView.findViewById<TextView>(R.id.username_latestmessage).text=chatPartnerUser?.username

                    val targetImageView=viewHolder.itemView.findViewById<ImageView>(R.id.latestmessage_dp)
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                }

                override fun onCancelled(p0: DatabaseError) =Unit

            })

        }


        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

    }

    private fun fetchCurrentUser(){
        val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                 CurrentUser = p0.getValue(User::class.java)
                Log.d("Main","current user is ${CurrentUser?.username}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }
    private fun verifylogin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)

            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }

        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}