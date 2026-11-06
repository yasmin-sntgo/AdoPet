package com.adopet.app

data class Animal(
val id: Long = 0,
val name: String,
val species: String,
val age: Int,
val size: String,
val sex: String,
val location: String,
val description: String,
val health: String,
val guardian: String,
val adopted: Boolean = false,
val canManage: Boolean = false
)
