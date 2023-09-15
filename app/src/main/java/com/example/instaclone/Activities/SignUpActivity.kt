package com.example.instaclone.Activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.instaclone.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.signinLinkBtn.setOnClickListener {
            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.sigupBtn.setOnClickListener {
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullName = binding.fullNameSignup.text.toString()
        val userName = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()
        val confirmPassword = binding.confirmPasswordSignup.text.toString()

        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Enter Your Full Name", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "Enter Your User Name", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Enter Your Email", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Enter Your Password", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(confirmPassword) -> Toast.makeText(this, "Please Enter Confirm Password", Toast.LENGTH_LONG).show()
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Toast.makeText(this, "Enter Valid Your Email", Toast.LENGTH_LONG).show()
            password != confirmPassword -> Toast.makeText(this, "Password Not Match", Toast.LENGTH_LONG).show()

            else ->{

                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("Sign Up")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()


                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful){
                            SaveUserInfor(fullName, email, userName, progressDialog)

                        }
                        else{
                            val message = task.exception!!.toString()
                            Log.d("errorCreateAccount", message)
                            Toast.makeText(this, "Lỗi: $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }

                    }
            }
        }



    }

    private fun SaveUserInfor(fullName: String, email: String, userName: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance()
            .reference
            .child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "hello"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-clone-app-a42d0.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=1cc54746-2039-4816-bbe0-bd94b7f7592b"


        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account Has Been Create Successfully", Toast.LENGTH_LONG).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow")
                        .child(currentUserID)
                        .child("Following")
                        .child(currentUserID)
                        .setValue(true)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            or Intent.FLAG_ACTIVITY_NEW_TASK )
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception!!.toString()
                    Log.d("errorSaveUserInfor", message)
                    Toast.makeText(this, "Lỗi: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()

                }
            }
    }
}