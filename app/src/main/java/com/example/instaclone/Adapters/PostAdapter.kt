package com.example.instaclone.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instaclone.Models.Post
import com.example.instaclone.Models.User
import com.example.instaclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class PostAdapter(private val mContext: Context,
                           private val mPost: List<Post>): RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var username: TextView
        var likes: TextView
        var comments: TextView
        var publisher: TextView
        var description: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            username = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.likes)
            comments = itemView.findViewById(R.id.comments)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (mPost != null){
            return mPost.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        publisherInfo(holder.profileImage, holder.username, holder.publisher, post.getPublisher())
    }

    private fun publisherInfo(profileImage: CircleImageView, username: TextView, publisher: TextView, publisherID: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(publisherID)


        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    username.text = user.getUsername()
                    publisher.text = user.getFullName()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}