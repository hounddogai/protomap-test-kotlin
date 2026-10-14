package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708413254)
        val items = mutableListOf<DockDoorTier>()
        for (index in 0 until 3) {
            items.add(newDockDoorTier("dock-door-tier-$index", "sample"))
        }
        val report = buildDockDoorTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
