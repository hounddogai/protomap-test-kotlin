package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705241448)
        val items = mutableListOf<DockDoorAllocation>()
        for (index in 0 until 5) {
            items.add(newDockDoorAllocation("dock-door-allocation-$index", "sample"))
        }
        val report = buildDockDoorAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
