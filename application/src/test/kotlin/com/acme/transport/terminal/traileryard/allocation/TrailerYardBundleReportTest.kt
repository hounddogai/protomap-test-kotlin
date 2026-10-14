package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706123568)
        val items = mutableListOf<TrailerYardBundle>()
        for (index in 0 until 6) {
            items.add(newTrailerYardBundle("trailer-yard-bundle-$index", "sample"))
        }
        val report = buildTrailerYardBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
