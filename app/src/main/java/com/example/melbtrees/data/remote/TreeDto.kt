package com.example.melbtrees.data.remote

import com.google.gson.annotations.SerializedName

// Renamed to match the new API structure
public data class ApiResponse(
    @SerializedName("results") val results: List<TreeDto>
)

// Represents one record in the "records" array
//public data class ApiRecord(
//    @SerializedName("fields") val fields: TreeFields
//)
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
    @SerializedName("geolocation") val geolocation: GeolocationDto?
)

public data class GeolocationDto(
    @SerializedName("lat") val latitude: Double?,
    @SerializedName("lon") val longitude: Double?
)