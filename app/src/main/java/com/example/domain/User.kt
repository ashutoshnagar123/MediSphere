package com.example.domain

data class User(
    val id: String,
    val email: String,
    val name: String,
    val age: String? = null,
    val gender: String? = null,
    val bloodGroup: String? = null
)
