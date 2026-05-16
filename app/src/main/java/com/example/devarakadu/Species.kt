package com.example.devarakadu

data class Species(
    val name: String,
    val scientificName: String,
    val description: String,
    val significance: String, // Cultural or ecological significance
    val imageUrl: String = ""
)