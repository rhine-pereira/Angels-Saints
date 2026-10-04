package com.rhinepereira.saints.data.remote

import com.rhinepereira.saints.data.model.Category
import com.rhinepereira.saints.data.model.Item
import com.rhinepereira.saints.data.model.ContentBlock
import com.squareup.moshi.Json

data class CategoryResponse(
    @Json(name = "categories") val categories: List<Category>? = emptyList()
)

data class CategoryItemsResponse(
    @Json(name = "items") val items: List<Item>? = emptyList()
)

data class ContentVersion(
    @Json(name = "label") val label: String? = "Version",
    @Json(name = "blocks") val blocks: List<ContentBlock>? = emptyList()
)

data class ContentResponse(
    @Json(name = "id") val id: String? = "",
    @Json(name = "title") val title: String? = "Untitled",
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "blocks") val blocks: List<ContentBlock>? = emptyList(),
    @Json(name = "versions") val versions: List<ContentVersion>? = null
)

data class DailyReadingsResponse(
    @Json(name = "date") val date: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "feast") val feast: String?,
    @Json(name = "readings") val readings: List<Reading>? = emptyList()
)

data class Reading(
    @Json(name = "type") val type: String?,
    @Json(name = "heading") val heading: String?,
    @Json(name = "reference") val reference: String?,
    @Json(name = "verses") val verses: List<String>? = emptyList(),
    @Json(name = "acclamation") val acclamation: String? = null,
    @Json(name = "response") val response: String? = null
)
