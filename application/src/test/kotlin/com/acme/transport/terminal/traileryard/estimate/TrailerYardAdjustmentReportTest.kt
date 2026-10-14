package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707833042)
        val items = mutableListOf<TrailerYardAdjustment>()
        for (index in 0 until 5) {
            items.add(newTrailerYardAdjustment("trailer-yard-adjustment-$index", "sample"))
        }
        val report = buildTrailerYardAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
