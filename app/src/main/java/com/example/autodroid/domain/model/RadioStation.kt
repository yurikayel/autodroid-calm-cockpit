package com.example.autodroid.domain.model

data class RadioStation(
    val id: String,
    val name: String,
    val frequency: String,
    val genre: String,
    val imageUrl: String = ""
)
