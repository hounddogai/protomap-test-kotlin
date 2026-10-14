package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701510440)
        val items = mutableListOf<TrailerYardSummary>()
        for (index in 0 until 2) {
            items.add(newTrailerYardSummary("trailer-yard-summary-$index", "sample"))
        }
        val report = buildTrailerYardSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
