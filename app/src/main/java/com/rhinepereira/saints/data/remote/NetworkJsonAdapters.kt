package com.rhinepereira.saints.data.remote

import com.rhinepereira.saints.data.model.Category
import com.rhinepereira.saints.data.model.ContentBlock
import com.rhinepereira.saints.data.model.Item
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json

class NetworkJsonAdapters {

    @FromJson
    fun fromJson(response: CategoryResponseJson): CategoryResponse {
        return CategoryResponse(
            categories = response.categories?.map(::mapCategory).orEmpty()
        )
    }

    @FromJson
    fun fromJson(response: CategoryItemsResponseJson): CategoryItemsResponse {
        return CategoryItemsResponse(
            items = response.items?.map(::mapItem).orEmpty()
        )
    }

    @FromJson
    fun fromJson(response: ContentResponseJson): ContentResponse {
        return ContentResponse(
            id = response.id.orEmpty(),
            title = response.title ?: "Untitled",
            imageUrl = response.imageUrl,
            blocks = response.blocks?.map(::mapContentBlock).orEmpty(),
            versions = response.versions?.map(::mapContentVersion)
        )
    }

    @FromJson
    fun fromJson(response: DailyReadingsResponseJson): DailyReadingsResponse {
        return DailyReadingsResponse(
            date = response.date,
            url = response.url,
            title = response.title,
            feast = response.feast,
            readings = response.readings?.map(::mapReading).orEmpty()
        )
    }

    private fun mapCategory(category: CategoryJson): Category {
        return Category(
            id = category.id.orEmpty(),
            title = category.title ?: "Unknown",
            description = category.description,
            imageUrl = category.imageUrl.orEmpty(),
            order = category.order ?: 0,
            enabled = category.enabled ?: true
        )
    }

    private fun mapItem(item: ItemJson): Item {
        return Item(
            id = item.id.orEmpty(),
            title = item.title.orEmpty(),
            summary = item.summary.orEmpty(),
            imageUrl = item.imageUrl.orEmpty(),
            categoryId = item.categoryId.orEmpty()
        )
    }

    private fun mapContentBlock(block: ContentBlockJson): ContentBlock {
        return ContentBlock(
            type = block.type ?: "text",
            value = block.value.orEmpty()
        )
    }

    private fun mapContentVersion(version: ContentVersionJson): ContentVersion {
        return ContentVersion(
            label = version.label ?: "Version",
            blocks = version.blocks?.map(::mapContentBlock).orEmpty()
        )
    }

    private fun mapReading(reading: ReadingJson): Reading {
        return Reading(
            type = reading.type,
            heading = reading.heading,
            reference = reading.reference,
            verses = reading.verses?.filterNotNull().orEmpty(),
            acclamation = reading.acclamation,
            response = reading.response
        )
    }
}

data class CategoryResponseJson(
    @Json(name = "categories") val categories: List<CategoryJson>?
)

data class CategoryItemsResponseJson(
    @Json(name = "items") val items: List<ItemJson>?
)

data class ContentResponseJson(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "imageUrl") val imageUrl: String?,
    @Json(name = "blocks") val blocks: List<ContentBlockJson>?,
    @Json(name = "versions") val versions: List<ContentVersionJson>?
)

data class ContentVersionJson(
    @Json(name = "label") val label: String?,
    @Json(name = "blocks") val blocks: List<ContentBlockJson>?
)

data class DailyReadingsResponseJson(
    @Json(name = "date") val date: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "feast") val feast: String?,
    @Json(name = "readings") val readings: List<ReadingJson>?
)

data class ReadingJson(
    @Json(name = "type") val type: String?,
    @Json(name = "heading") val heading: String?,
    @Json(name = "reference") val reference: String?,
    @Json(name = "verses") val verses: List<String?>?,
    @Json(name = "acclamation") val acclamation: String?,
    @Json(name = "response") val response: String?
)

data class CategoryJson(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "imageUrl") val imageUrl: String?,
    @Json(name = "order") val order: Int?,
    @Json(name = "enabled") val enabled: Boolean?
)

data class ItemJson(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "summary") val summary: String?,
    @Json(name = "imageUrl") val imageUrl: String?,
    @Json(name = "categoryId") val categoryId: String?
)

data class ContentBlockJson(
    @Json(name = "type") val type: String?,
    @Json(name = "value") val value: String?
)
