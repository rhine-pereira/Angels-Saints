package com.rhinepereira.saints.data.bible

/** One contiguous stretch of verses, possibly crossing a chapter boundary. */
data class VerseSpan(val fromChapter: Int, val fromVerse: Int, val toChapter: Int, val toVerse: Int)

data class ScriptureRef(val book: String, val spans: List<VerseSpan>)

/**
 * Parses lectionary references such as "Isaiah 5:1-7", "Isaiah 5: 1-7", "1 John 4:7-10",
 * "Psalm 80:9, 12-16", "Mark 1:40-2:5", "Matthew 5:1-12a; 6:1-4". Verse letters (12a) are dropped.
 */
object ScriptureRefParser {

    private val head = Regex("""^\s*((?:[1-3]\s*)?[^\d\s][^\d]*?)\s*(\d.*)$""")

    fun parse(ref: String): ScriptureRef? {
        val m = head.matchEntire(ref.trim()) ?: return null
        val book = m.groupValues[1].trim().trimEnd('.')
        val body = m.groupValues[2].replace(Regex("""\s+"""), "").replace(Regex("""(\d)[a-z]+"""), "$1")
        val spans = mutableListOf<VerseSpan>()
        var chapter = -1
        for (group in body.split(';').filter { it.isNotEmpty() }) {
            for (part in group.split(',').filter { it.isNotEmpty() }) {
                val (start, end) = part.split('-', '–').let { it[0] to it.getOrNull(1) }
                val (c1, v1) = if (':' in start) {
                    start.substringBefore(':').toInt() to start.substringAfter(':').toInt()
                } else if (chapter < 0) {
                    // Whole chapter, e.g. "Psalm 23"
                    spans += VerseSpan(start.toInt(), 1, start.toInt(), Int.MAX_VALUE)
                    continue
                } else {
                    chapter to start.toInt()
                }
                chapter = c1
                val (c2, v2) = when {
                    end == null -> c1 to v1
                    ':' in end -> end.substringBefore(':').toInt() to end.substringAfter(':').toInt()
                    else -> c1 to end.toInt()
                }
                chapter = c2
                spans += VerseSpan(c1, v1, c2, v2)
            }
        }
        return if (spans.isEmpty()) null else ScriptureRef(book, spans)
    }
}
