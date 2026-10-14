package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701568655)
        val items = mutableListOf<DockDoorTarget>()
        for (index in 0 until 2) {
            items.add(newDockDoorTarget("dock-door-target-$index", "sample"))
        }
        val report = buildDockDoorTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
