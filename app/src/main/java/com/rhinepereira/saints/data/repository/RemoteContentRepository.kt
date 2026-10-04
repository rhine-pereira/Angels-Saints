package com.rhinepereira.saints.data.repository

import com.rhinepereira.saints.data.model.Category
import com.rhinepereira.saints.data.model.Item
import com.rhinepereira.saints.data.remote.ContentApi
import com.rhinepereira.saints.data.remote.CACHE_ONLY
import com.rhinepereira.saints.data.remote.ContentResponse
import com.rhinepereira.saints.data.remote.net
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.IOException

class RemoteContentRepository(
    private val api: ContentApi
) : ContentRepository {

    override fun getCategories(): Flow<List<Category>> = flow {
        // home.json skips the cache to pick up new categories; offline, fall back to the cached copy.
        val home = try {
            net { api.getHome() }
        } catch (e: IOException) {
            runCatching { api.getHome(CACHE_ONLY) }.getOrElse { throw e }
        }
        emit(home.categories.orEmpty().filter { it.enabled }.sortedBy { it.order })
    }

    override fun getItemsByCategory(categoryId: String): Flow<List<Item>> = flow {
        emit(net { api.getCategoryItems(categoryId) }.items.orEmpty())
    }

    override fun getContent(contentId: String): Flow<ContentResponse> = flow {
        emit(net { api.getContent(contentId) })
    }

    /** Warms the HTTP cache so the library works offline. Failures are irrelevant here. */
    override suspend fun prefetchEverything() {
        coroutineScope {
            val categories = runCatching { net { api.getHome() }.categories.orEmpty() }.getOrDefault(emptyList())
            categories.filter { it.isLibrary }.forEach { category ->
                launch {
                    runCatching { net { api.getCategoryItems(category.id) }.items.orEmpty() }.getOrDefault(emptyList())
                        .forEach { item -> launch { runCatching { net { api.getContent(item.id) } } } }
                }
            }
        }
    }

}

/** Daily and catechism entries in home.json have their own tabs now. */
val Category.isLibrary: Boolean get() = id !in setOf("daily-feast", "daily-readings", "ccc")
