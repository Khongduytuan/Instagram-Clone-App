package com.example.instaclone.Activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instaclone.R
import com.example.instaclone.databinding.ActivityAddPostBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding
    private var myUrl = ""
    private var imageUri: Uri?= null
    private var storagePostRef: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storagePostRef = FirebaseStorage.getInstance().reference.child("Posts Picture")

        binding.saveNewPostBtn.setOnClickListener {
            uploadImage()
        }

        CropImage.activity()
            .setAspectRatio(2, 1)
            .start(this@AddPostActivity)
    }

    private fun uploadImage() {
        if (imageUri == null){
            Toast.makeText(this@AddPostActivity, "Chose Your Image", Toast.LENGTH_LONG).show()
        }
        else if(binding.descriptionPost.text.toString().isEmpty()){
            Toast.makeText(this@AddPostActivity, "Please Add Your Description", Toast.LENGTH_LONG).show()
        }
        else{
            val fileRef  =storagePostRef!!.child(System.currentTimeMillis().toString() + ".jsp")

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                if (task.isSuccessful){
                    val downloadUri = task.result
                    myUrl = downloadUri.toString()

                    val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                    val postId = ref.push().key

                    val postMap = HashMap<String, Any>()
                    postMap["postid"] = postId!!
                    postMap["description"] = binding.descriptionPost.text.toString()
                    postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                    postMap["postimage"] = myUrl

                    ref.child(postId).updateChildren(postMap)
                    Toast.makeText(this@AddPostActivity,
                        "Post Uploaded Successfully", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                }
                else{
                    Toast.makeText(this@AddPostActivity, "Post uploaded Failed!!",
                        Toast.LENGTH_LONG).show()
                }
            })
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                val resultUri = result.uri
                imageUri = resultUri
                binding.imagePost.setImageURI(imageUri)
            }


        }
    }
}