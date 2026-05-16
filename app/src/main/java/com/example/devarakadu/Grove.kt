package com.example.devarakadu

data class MyLatLng(val latitude: Double, val longitude: Double)

data class Grove(
    val id: String = "",
    val name: String = "",
    val type: String = "", // e.g., Kaavu, Bana
    val deity: String = "",
    val myth: String = "",           // "Traditional Beliefs"
    val scientificFact: String = "",  // "Scientific Facts"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)