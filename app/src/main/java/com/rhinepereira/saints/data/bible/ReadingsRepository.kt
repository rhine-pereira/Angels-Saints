package com.rhinepereira.saints.data.bible

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.rhinepereira.saints.data.remote.ContentApi
import com.rhinepereira.saints.data.remote.net
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

data class ReadingPlate(
    val kind: String,
    val reference: String,
    val verses: List<String>,
    val response: String? = null,
    val acclamation: String? = null
)

enum class ReadingsSource { DOUAY_RHEIMS, REMOTE }

data class DayReadings(val plates: List<ReadingPlate>, val source: ReadingsSource)

/**
 * Readings for a date. Plug-and-play: drop these into app/src/main/assets/bible/ and they win
 * over the remote JSON automatically:
 *  - drb.sqlite        table verses(book TEXT, chapter INT, verse INT, text TEXT)
 *  - lectionary.json   {"2026-10-04":[{"type":"First Reading","ref":"Isaiah 5:1-7"},
 *                       {"type":"Responsorial Psalm","ref":"Psalm 79:9-16","response":"..."}, ...]}
 *  - books.json        optional {"Isaiah":"Isaias", ...} when lectionary names differ from the db's.
 */
class ReadingsRepository(
    private val context: Context,
    private val api: ContentApi
) {
    private val db: SQLiteDatabase? by lazy { openBible() }

    private val lectionary: JSONObject? by lazy { assetJson("bible/lectionary.json") }
    private val bookAliases: JSONObject? by lazy { assetJson("bible/books.json") }

    /** Returns null when nothing is published for [date]; throws on network failure. */
    suspend fun readings(date: LocalDate): DayReadings? = withContext(Dispatchers.IO) {
        local(date) ?: remote(date)
    }

    private fun local(date: LocalDate): DayReadings? {
        val db = db ?: return null
        val entries = lectionary?.optJSONArray(date.toString()) ?: return null
        val plates = List(entries.length()) { i ->
            val e = entries.getJSONObject(i)
            val ref = e.getString("ref")
            ReadingPlate(
                kind = e.optString("type"),
                reference = ref,
                verses = ScriptureRefParser.parse(ref)?.let { verses(db, it) }.orEmpty(),
                response = e.optString("response").ifBlank { null },
                acclamation = e.optString("acclamation").ifBlank { null }
            )
        }
        return DayReadings(plates, ReadingsSource.DOUAY_RHEIMS)
    }

    private fun verses(db: SQLiteDatabase, ref: ScriptureRef): List<String> {
        val book = bookAliases?.optString(ref.book)?.ifBlank { null } ?: ref.book
        return ref.spans.flatMap { s ->
            db.rawQuery(
                """SELECT verse, text FROM verses WHERE book = ?
                   AND (chapter * 1000 + verse) BETWEEN CAST(? AS INTEGER) AND CAST(? AS INTEGER)
                   ORDER BY chapter, verse""",
                arrayOf(book, (s.fromChapter * 1000 + s.fromVerse).toString(),
                    (s.toChapter * 1000L + minOf(s.toVerse, 999)).toString())
            ).use { c -> buildList { while (c.moveToNext()) add("${c.getInt(0)} ${c.getString(1)}") } }
        }
    }

    private suspend fun remote(date: LocalDate): DayReadings? {
        val path = "readings/%d/%02d/%s.json".format(date.year, date.monthValue, date)
        val r = try {
            net { api.getDailyReadings(path) }
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) return null else throw e
        }
        val plates = r.readings.orEmpty().map {
            ReadingPlate(
                kind = it.type.orEmpty(),
                reference = it.reference.orEmpty(),
                verses = it.verses.orEmpty(),
                response = it.response,
                acclamation = it.acclamation
            )
        }
        return if (plates.isEmpty()) null else DayReadings(plates, ReadingsSource.REMOTE)
    }

    private fun openBible(): SQLiteDatabase? {
        val name = "bible/drb.sqlite"
        if (context.assets.list("bible")?.contains("drb.sqlite") != true) return null
        // Re-copy after every app update so a replaced database ships with the APK.
        val installed = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        val file = File(context.noBackupFilesDir, "drb.sqlite")
        if (!file.exists() || file.lastModified() < installed) {
            context.assets.open(name).use { input -> file.outputStream().use { input.copyTo(it) } }
        }
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    private fun assetJson(name: String): JSONObject? = runCatching {
        JSONObject(context.assets.open(name).bufferedReader().use { it.readText() })
    }.getOrNull()
}
