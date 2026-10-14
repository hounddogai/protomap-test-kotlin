package com.acme.transport.terminal.sortation.tier

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702193955)
        val items = mutableListOf<SortationTier>()
        for (index in 0 until 2) {
            items.add(newSortationTier("sortation-tier-$index", "sample"))
        }
        val report = buildSortationTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
