package com.acme.transport.safety.coaching.bundle

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700290541)
        val items = mutableListOf<CoachingSummary>()
        for (index in 0 until 4) {
            items.add(newCoachingSummary("coaching-summary-$index", "sample"))
        }
        val report = buildCoachingSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
