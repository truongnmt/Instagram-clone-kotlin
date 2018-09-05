package com.example.nguyenmanhtruong.instagram.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nguyenmanhtruong.instagram.R
import com.example.nguyenmanhtruong.instagram.models.User
import com.example.nguyenmanhtruong.instagram.utils.FirebaseHelper
import com.example.nguyenmanhtruong.instagram.utils.TaskSourceOnCompleteListener
import com.example.nguyenmanhtruong.instagram.utils.ValueEventListenerAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_add_friend.*
import kotlinx.android.synthetic.main.add_friends_item.view.*

class AddFriendsActivity : AppCompatActivity(), FriendsAdapter.Listener {

    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        mFirebase = FirebaseHelper(this)
        mAdapter = FriendsAdapter(this)

        val uid = mFirebase.auth.currentUser!!.uid

        back_image.setOnClickListener { finish() }

        add_friends_recycler.adapter = mAdapter
        add_friends_recycler.layoutManager = LinearLayoutManager(this)
        mFirebase.database.child("users").addValueEventListener(ValueEventListenerAdapter { it ->
            val allUsers = it.children.map { it.asUser()!! }
            val (userList, otherUsersList) = allUsers.partition { it.uid == uid }
            mUser = userList.first()
            mUsers = otherUsersList

            mAdapter.update(mUsers, mUser.follows)
        })
    }

    override fun follow(uid: String) {
        setFollow(uid, true) {
            mAdapter.followed(uid)
        }
    }

    override fun unfollow(uid: String) {
        setFollow(uid, false) {
            mAdapter.unfollowed(uid)
        }
    }

    private fun setFollow(uid: String, follow: Boolean, onSuccess: () -> Unit) {
        fun DatabaseReference.setValueTrueOrRemove(value: Boolean) =
                if (value) setValue(true) else removeValue()

        val followsTask = mFirebase.database.child("users").child(mUser.uid)
                .child("follows").child(uid).setValueTrueOrRemove(follow)
        val followersTask = mFirebase.database.child("users").child(uid)
                .child("followers").child(mUser.uid).setValueTrueOrRemove(follow)

        val feedPostsTask = task<Void> { taskSource ->
            mFirebase.database.child("feed-posts").child(uid)
                    .addListenerForSingleValueEvent(ValueEventListenerAdapter { it ->
                        val postsMap = if (follow) {
                            it.children.map { it.key to it.value }.toMap()
                        } else {
                            it.children.map { it.key to null }.toMap()
                        }
                        mFirebase.database.child("feed-posts").child(mUser.uid).updateChildren(postsMap)
                                .addOnCompleteListener(TaskSourceOnCompleteListener(taskSource))
                    })
        }

        Tasks.whenAll(followsTask, followersTask, feedPostsTask)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSuccess()
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
    }
}

class FriendsAdapter(private val listener: Listener)
    : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface Listener {
        fun follow(uid: String)
        fun unfollow(uid: String)
    }

    private var mUsers = listOf<User>()
    private var mFollows = mapOf<String, Boolean>()
    private var mPositions = mapOf<String, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_friends_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mUsers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val user = mUsers[position]
            view.photo_image.loadImage(user.photo!!)
            view.username_text.text = user.username
            view.name_text.text = user.name
            view.follow_btn.setOnClickListener { listener.follow(user.uid) }
            view.unfollow_btn.setOnClickListener { listener.unfollow(user.uid) }

            val follows = mFollows[user.uid] ?: false
            if (follows) {
                view.follow_btn.visibility = View.GONE
                view.unfollow_btn.visibility = View.VISIBLE
            } else {
                view.follow_btn.visibility = View.VISIBLE
                view.unfollow_btn.visibility = View.GONE
            }
        }
    }


    fun update(users: List<User>, follows: Map<String, Boolean>) {
        mUsers = users
        mPositions = users.withIndex().map { (idx, user) -> user.uid to idx }.toMap()
        mFollows = follows
        notifyDataSetChanged()
    }

    fun followed(uid: String) {
        mFollows += (uid to true)
        notifyItemChanged(mPositions[uid]!!)
    }

    fun unfollowed(uid: String) {
        mFollows -= uid
        notifyItemChanged(mPositions[uid]!!)
    }
}