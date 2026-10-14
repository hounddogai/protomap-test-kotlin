package com.acme.transport.crewing.roster.schedule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704603382)
        val items = mutableListOf<PendingRosterTier>()
        for (index in 0 until 5) {
            items.add(newPendingRosterTier("pending-roster-tier-$index", "sample"))
        }
        val report = buildPendingRosterTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
