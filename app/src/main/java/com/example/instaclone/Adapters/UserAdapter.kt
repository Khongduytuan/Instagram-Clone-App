package com.example.instaclone.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instaclone.Fragment.ProfileFragment
import com.example.instaclone.Models.User
import com.example.instaclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.annotations.NotNull
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
// private var  isFragment: Boolean = false
class UserAdapter (private var context: Context,
                    private var mUser: List<User>,
                    ): RecyclerView.Adapter<UserAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNameSearch.text = user.getUsername()
        holder.userFullNameSearch.text = user.getFullName()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImageSearch)

        holder.itemView.setOnClickListener(View.OnClickListener {
            val pref = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.getUid())
            pref.apply()

            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        })

        checkFollowingStatus(user!!.getUid(), holder.followBtnSearch)
        holder.followBtnSearch.setOnClickListener {
            if (holder.followBtnSearch.text.toString().equals("Follow")){
                firebaseUser?.uid.let { it1->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user!!.getUid())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                FirebaseDatabase.getInstance().reference
                                    .child("Follow").child(user!!.getUid())
                                    .child("Followers").child(it1.toString())
                                    .setValue(true).addOnCompleteListener { task ->
                                        if (task.isSuccessful){

                                        }
                                    }
                            }
                        }
                }
            }
            else{

                firebaseUser?.uid.let { it1->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user!!.getUid())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                FirebaseDatabase.getInstance().reference
                                    .child("Follow").child(user!!.getUid())
                                    .child("Followers").child(it1.toString())
                                    .removeValue().addOnCompleteListener { task ->
                                        if (task.isSuccessful){

                                        }
                                    }
                            }
                        }
                }

            }
        }

    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(@NotNull itemView: View) : RecyclerView.ViewHolder(itemView){
        var userNameSearch: TextView = itemView.findViewById(R.id.user_name_search)
        var userFullNameSearch: TextView = itemView.findViewById(R.id.user_full_name_search)
        var userProfileImageSearch: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followBtnSearch: TextView = itemView.findViewById(R.id.follow_btn_search)
    }

    private fun checkFollowingStatus(uid: String, followBtnSearch: TextView) {

        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(uid).exists()){
                    followBtnSearch.text = "Following"
                }
                else{
                    followBtnSearch.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}