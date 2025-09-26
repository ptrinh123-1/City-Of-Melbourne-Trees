package com.example.melbtrees.data.remote // <-- Make sure this package name is correct

import com.google.gson.annotations.SerializedName

public data class ApiResponse(
    @SerializedName("results") val results: List<TreeDto>
)

public data class TreeDto(
    @SerializedName("com_id") val id: String?,
    @SerializedName("common_name") val commonName: String?,
    @SerializedName("scientific_name") val scientificName: String?,
    @SerializedName("family") val family: String?,
    @SerializedName("genus") val genus: String?,
    @SerializedName("age_description") val ageDescription: String?,
    @SerializedName("year_planted") val yearPlanted: String?,
    @SerializedName("date_planted") val datePlanted: String?,
    @SerializedName("diameter_breast_height") val diameterAtBreastHeightCm: Int?,
    @SerializedName("precinct") val precinct: String?,
    @SerializedName("located_in") val locatedIn: String?,
    @SerializedName("geolocation") val location: TreeLocationDto?
)

public data class TreeLocationDto(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double
)