package com.example.instaclone.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instaclone.Models.User
import com.example.instaclone.R
import com.example.instaclone.databinding.ActivityAccountSettingsBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri ?= null
    private var storageProfilePicRef: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Picture")


        binding.logoutBtn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.changeImageTextBtn.setOnClickListener {
            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@AccountSettingsActivity)
        }

        userInfo()
        binding.saveInforProfileBtn.setOnClickListener {
            if (checker == "clicked"){
                uploadImageAndUpdateInfo()
            }
            else{
                updateUserInfoOnly()
            }
        }


    }

    private fun uploadImageAndUpdateInfo() {
        if (binding.fullNameProfileFrag.text.toString().isEmpty()){
            Toast.makeText(this@AccountSettingsActivity, "Enter Your Full Name", Toast.LENGTH_LONG).show()
        } else if (binding.userNameProfileFrag.text.toString().isEmpty()){
            Toast.makeText(this@AccountSettingsActivity, "Enter Your User Name", Toast.LENGTH_LONG).show()
        } else if (binding.bioProfileFrag.text.toString().isEmpty()){
            Toast.makeText(this@AccountSettingsActivity, "Enter Your Bio", Toast.LENGTH_LONG).show()
        } else if (imageUri == null){
            Toast.makeText(this@AccountSettingsActivity, "Chose Your Image", Toast.LENGTH_LONG).show()
        } else{
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Account Setting")
            progressDialog.setMessage("Please wait...")
            progressDialog.show()

            val fileRef  =storageProfilePicRef!!.child(firebaseUser.uid + ".jsp")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful){
                    task.exception?.let {
                        throw it
                        progressDialog.dismiss()
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                if (task.isSuccessful){
                    val downloadUri = task.result
                    myUrl = downloadUri.toString()

                    val ref = FirebaseDatabase.getInstance().reference.child("Users")

                    val userMap = HashMap<String, Any>()
                    userMap["fullname"] = binding.fullNameProfileFrag.text.toString().toLowerCase()
                    userMap["username"] = binding.userNameProfileFrag.text.toString().toLowerCase()
                    userMap["bio"] = binding.bioProfileFrag.text.toString().toLowerCase()
                    userMap["image"] = myUrl

                    ref.child(firebaseUser.uid).updateChildren(userMap)
                    progressDialog.dismiss()
                    Toast.makeText(this@AccountSettingsActivity,
                        "Account Information Has Been Updated Successfully", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                }
                else{
                    progressDialog.dismiss()
                    Toast.makeText(this@AccountSettingsActivity, "Account Information And Image Can Not Update!!",
                        Toast.LENGTH_LONG).show()
                }
            })


        }

    }

    private fun updateUserInfoOnly() {
        if (binding.fullNameProfileFrag.text.toString().isEmpty()){
            Toast.makeText(this@AccountSettingsActivity, "Enter Your Full Name", Toast.LENGTH_LONG).show()
        } else if (binding.userNameProfileFrag.text.toString().isEmpty()){
            Toast.makeText(this@AccountSettingsActivity, "Enter Your User Name", Toast.LENGTH_LONG).show()
        } else if (binding.bioProfileFrag.text.toString().isEmpty()){
            Toast.makeText(this@AccountSettingsActivity, "Enter Your Bio", Toast.LENGTH_LONG).show()
        }
        else{
            val usersRef  = FirebaseDatabase.getInstance().reference.child("Users")

            val userMap = HashMap<String, Any>()
            userMap["fullname"] = binding.fullNameProfileFrag.text.toString().toLowerCase()
            userMap["username"] = binding.userNameProfileFrag.text.toString().toLowerCase()
            userMap["bio"] = binding.bioProfileFrag.text.toString().toLowerCase()

            usersRef.child(firebaseUser.uid).updateChildren(userMap)
            Toast.makeText(this@AccountSettingsActivity,
                "Account Information Has Been Updated Successfully", Toast.LENGTH_LONG).show()
            val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()

        }


    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding.profileImageViewInAccountSettingsProfile)
                    binding.fullNameProfileFrag.setText(user.getFullName())
                    binding.userNameProfileFrag.setText(user.getUsername())
                    binding.bioProfileFrag.setText(user.getBio())

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                val resultUri = result.uri
                imageUri = resultUri
                binding.profileImageViewInAccountSettingsProfile.setImageURI(imageUri)
            }
            else{

            }

        }
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE &&
//                requestCode == Activity.RESULT_OK &&
//                data != null){
//            val result = CropImage.getActivityResult(data)
//            imageUri = result.uri
//            binding.profileImageViewInAccountSettingsProfile.setImageURI(imageUri)
//        }
//        else{
//            Log.d("errorOnActivityResult", imageUri.toString())
//        }

    }
}