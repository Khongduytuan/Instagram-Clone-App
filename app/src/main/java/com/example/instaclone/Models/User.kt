package com.example.instaclone.Models

class User {

    private var username: String = ""
    private var fullname: String = ""
//    private var email: String = ""
    private var uid: String = ""
    private var image: String = ""
    private var bio: String = ""

    constructor()
    constructor(username: String, fullname: String, uid: String, image: String, bio: String){
        this.username = username
        this.fullname = fullname
        this.uid = uid
        this.image = image
        this.bio = bio

    }

    fun getUsername(): String{
        return username
    }
    fun setUsername(username: String){
        this.username = username
    }


    fun getFullName(): String{
        return fullname
    }
    fun setFullName(fullname: String){
        this.fullname = fullname
    }


    fun getUid(): String{
        return uid
    }
    fun setUid(uid: String){
        this.uid = uid
    }


    fun getImage(): String{
        return image
    }
    fun setImage(image: String){
        this.image = image
    }


    fun getBio(): String{
        return bio
    }
    fun setBio(bio: String){
        this.bio = bio
    }


}