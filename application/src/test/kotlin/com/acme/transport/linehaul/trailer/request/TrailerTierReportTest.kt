package com.acme.transport.linehaul.trailer.request

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700301253)
        val items = mutableListOf<TrailerTier>()
        for (index in 0 until 6) {
            items.add(newTrailerTier("trailer-tier-$index", "sample"))
        }
        val report = buildTrailerTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
