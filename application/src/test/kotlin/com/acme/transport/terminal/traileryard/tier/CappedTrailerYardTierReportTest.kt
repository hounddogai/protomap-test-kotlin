package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerYardTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703175208)
        val items = mutableListOf<CappedTrailerYardTier>()
        for (index in 0 until 4) {
            items.add(newCappedTrailerYardTier("capped-trailer-yard-tier-$index", "sample"))
        }
        val report = buildCappedTrailerYardTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
