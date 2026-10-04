package com.rhinepereira.saints

import android.app.Application
import android.content.Context
import com.rhinepereira.saints.data.bible.ReadingsRepository
import com.rhinepereira.saints.data.calendar.LiturgicalCalendar
import com.rhinepereira.saints.data.nameday.NameDayStore
import com.rhinepereira.saints.data.remote.RetrofitClient
import com.rhinepereira.saints.data.repository.RemoteContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Hand-rolled graph: the app is small enough that a DI framework would be pure ceremony. */
class SaintsApp : Application() {
    val api by lazy { RetrofitClient.getApi(this) }
    val content by lazy { RemoteContentRepository(api) }
    val readings by lazy { ReadingsRepository(this, api) }
    val calendar by lazy { LiturgicalCalendar(assets) }
    val nameDays by lazy { NameDayStore(this) }
    val language by lazy { LanguagePref(this) }
}

val Context.app: SaintsApp get() = applicationContext as SaintsApp

enum class AppLanguage(val tag: String) { ENGLISH("en"), MARATHI("mr") }

class LanguagePref(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _value = MutableStateFlow(
        AppLanguage.entries.firstOrNull { it.tag == prefs.getString("lang", null) } ?: AppLanguage.ENGLISH
    )
    val value: StateFlow<AppLanguage> = _value.asStateFlow()

    fun set(lang: AppLanguage) {
        prefs.edit().putString("lang", lang.tag).apply()
        _value.value = lang
    }
}
