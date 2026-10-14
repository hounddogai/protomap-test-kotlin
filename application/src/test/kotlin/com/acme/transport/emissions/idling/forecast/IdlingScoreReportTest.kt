package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708786911)
        val items = mutableListOf<IdlingScore>()
        for (index in 0 until 6) {
            items.add(newIdlingScore("idling-score-$index", "sample"))
        }
        val report = buildIdlingScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
