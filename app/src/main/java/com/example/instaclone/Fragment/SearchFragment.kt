package com.example.instaclone.Fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instaclone.Adapters.UserAdapter
import com.example.instaclone.Adapters.UserAdapter2
import com.example.instaclone.Models.User
import com.example.instaclone.databinding.FragmentSearchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private var mUser: List<User>? = null
    private var userAdapter2: UserAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycleViewSearch?.setHasFixedSize(true)
        binding.recycleViewSearch?.layoutManager = LinearLayoutManager(context)



//        userAdapter2 = UserAdapter2(mUser)
//        binding.recycleViewSearch?.adapter = userAdapter2
//        retrieveUsers()

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.searchEditText.text.toString() == ""){

                }else{
//                    binding.recycleViewSearch?.visibility = View.VISIBLE
//                    retrieveUsers()
                    searchUser(s.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                if (binding.searchEditText.text.toString() != ""){

                    for (snapshot in dataSnapshot.children){
                        val user = snapshot.getValue(User::class.java)
                        if (user != null){
                            user?.let { userList.add(it) }
                        }
                        mUser = userList

                        if (user != null) {
                            Log.d("errorSearchUser", user.getFullName())
                        }
//                        userAdapter2 = UserAdapter2(userList)
                        userAdapter2 = UserAdapter(requireContext(), userList)

                        binding.recycleViewSearch?.adapter = userAdapter2
                        userAdapter2?.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

//    private fun retrieveUsers() {
//        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
//        usersRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val userList = mutableListOf<User>()
////                if (binding.searchEditText.text.toString() == ""){
//
//                    for (snapshot in dataSnapshot.children){
//                        val user = snapshot.getValue(User::class.java)
//                        if (user != null){
//                            user?.let { userList.add(it) }
//                        }
//                        mUser = userList
//                        userAdapter2 = UserAdapter2(userList)
//                        binding.recycleViewSearch?.adapter = userAdapter2
//                        userAdapter2?.notifyDataSetChanged()
//                    }
////                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//    }


}