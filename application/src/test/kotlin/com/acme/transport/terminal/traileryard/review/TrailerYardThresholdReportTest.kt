package com.acme.transport.terminal.traileryard.review

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705267644)
        val items = mutableListOf<TrailerYardThreshold>()
        for (index in 0 until 4) {
            items.add(newTrailerYardThreshold("trailer-yard-threshold-$index", "sample"))
        }
        val report = buildTrailerYardThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
