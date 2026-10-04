package com.rhinepereira.saints.data.repository

import com.rhinepereira.saints.data.model.Category
import com.rhinepereira.saints.data.model.Item
import com.rhinepereira.saints.data.remote.ContentResponse
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getCategories(): Flow<List<Category>>
    fun getItemsByCategory(categoryId: String): Flow<List<Item>>
    fun getContent(contentId: String): Flow<ContentResponse>
    suspend fun prefetchEverything()
}
