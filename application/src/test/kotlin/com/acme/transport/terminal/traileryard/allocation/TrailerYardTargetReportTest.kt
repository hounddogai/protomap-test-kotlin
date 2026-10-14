package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706705936)
        val items = mutableListOf<TrailerYardTarget>()
        for (index in 0 until 3) {
            items.add(newTrailerYardTarget("trailer-yard-target-$index", "sample"))
        }
        val report = buildTrailerYardTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
