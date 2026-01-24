package com.rhinepereira.saints.ccc

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CCCEntry(
    val id: Int,
    val text: String
)
