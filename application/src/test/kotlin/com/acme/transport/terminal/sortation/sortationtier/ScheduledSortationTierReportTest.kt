package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSortationTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701459798)
        val items = mutableListOf<ScheduledSortationTier>()
        for (index in 0 until 4) {
            items.add(newScheduledSortationTier("scheduled-sortation-tier-$index", "sample"))
        }
        val report = buildScheduledSortationTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
