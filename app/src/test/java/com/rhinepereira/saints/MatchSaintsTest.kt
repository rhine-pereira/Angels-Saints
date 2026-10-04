package com.rhinepereira.saints

import com.rhinepereira.saints.data.calendar.Saint
import com.rhinepereira.saints.data.nameday.NameDay
import com.rhinepereira.saints.nameday.matchSaints
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MatchSaintsTest {
    private val saints = listOf(
        Saint("Francis of Assisi", "Saint Francis of Assisi", 10, 4),
        Saint("Francis Xavier", "Saint Francis Xavier, Priest", 12, 3),
        Saint("Thérèse of the Child Jesus", "Saint Thérèse of the Child Jesus", 10, 1)
    )

    @Test
    fun matchesWordsAndIgnoresAccents() {
        assertEquals(2, matchSaints(saints, "francis").size)
        assertEquals("Francis Xavier", matchSaints(saints, "Francis xav").single().name)
        assertEquals(1, matchSaints(saints, "therese").size)
        assertEquals(0, matchSaints(saints, "x").size)
    }

    @Test
    fun nameDayRollsToNextYearAndHandlesLeapDay() {
        val francis = NameDay("Rhine", "Saint Francis of Assisi", 10, 4)
        assertEquals(LocalDate.of(2026, 10, 4), francis.nextOn(LocalDate.of(2026, 10, 4)))
        assertEquals(LocalDate.of(2027, 10, 4), francis.nextOn(LocalDate.of(2026, 10, 5)))
        assertEquals(LocalDate.of(2027, 2, 28), NameDay("A", "S", 2, 29).nextOn(LocalDate.of(2027, 1, 1)))
    }
}
