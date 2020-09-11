package com.example.firebaseauthrealtime

data class UserModel(
    val username: String,
    val email: String
) {
    constructor(): this("", "")
}