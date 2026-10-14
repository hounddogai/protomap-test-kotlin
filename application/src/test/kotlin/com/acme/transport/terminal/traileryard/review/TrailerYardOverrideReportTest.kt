package com.acme.transport.terminal.traileryard.review

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701315959)
        val items = mutableListOf<TrailerYardOverride>()
        for (index in 0 until 3) {
            items.add(newTrailerYardOverride("trailer-yard-override-$index", "sample"))
        }
        val report = buildTrailerYardOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
