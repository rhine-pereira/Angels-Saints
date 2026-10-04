package com.rhinepereira.saints

import com.rhinepereira.saints.data.bible.ScriptureRefParser
import com.rhinepereira.saints.data.bible.VerseSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScriptureRefParserTest {

    private fun spans(ref: String) = ScriptureRefParser.parse(ref)!!.spans

    @Test
    fun parsesCommonForms() {
        assertEquals("Isaiah", ScriptureRefParser.parse("Isaiah 5: 1-7")!!.book)
        assertEquals(listOf(VerseSpan(5, 1, 5, 7)), spans("Isaiah 5:1-7"))
        assertEquals("1 John", ScriptureRefParser.parse("1 John 4:7-10")!!.book)
        assertEquals(listOf(VerseSpan(80, 9, 80, 9), VerseSpan(80, 12, 80, 16)), spans("Psalm 80:9, 12-16"))
        assertEquals(listOf(VerseSpan(1, 40, 2, 5)), spans("Mark 1:40-2:5"))
        assertEquals(listOf(VerseSpan(5, 1, 5, 12), VerseSpan(6, 1, 6, 4)), spans("Matthew 5:1-12a; 6:1-4"))
        assertEquals(listOf(VerseSpan(23, 1, 23, Int.MAX_VALUE)), spans("Psalm 23"))
        assertNull(ScriptureRefParser.parse("no reference"))
    }
}
