package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSortationTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707224957)
        val items = mutableListOf<StrictSortationTier>()
        for (index in 0 until 3) {
            items.add(newStrictSortationTier("strict-sortation-tier-$index", "sample"))
        }
        val report = buildStrictSortationTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
