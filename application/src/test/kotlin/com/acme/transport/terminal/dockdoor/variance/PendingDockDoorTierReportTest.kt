package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708222485)
        val items = mutableListOf<PendingDockDoorTier>()
        for (index in 0 until 4) {
            items.add(newPendingDockDoorTier("pending-dock-door-tier-$index", "sample"))
        }
        val report = buildPendingDockDoorTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
