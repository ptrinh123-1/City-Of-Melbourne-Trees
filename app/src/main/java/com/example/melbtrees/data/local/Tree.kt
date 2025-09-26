package com.example.melbtrees.data.local

data class Tree(
    val id: String,
    val commonName: String?,
    val scientificName: String?,
    val family: String?,
    val genus: String?,
    val ageDescription: String?,
    val yearPlanted: String?,
    val datePlanted: String?,
    val diameterAtBreastHeightCm: Int?,
    val precinct: String?,
    val locatedIn: String?,
    val latitude: Double?,
    val longitude: Double?,
    var isFavourite: Boolean = false
)