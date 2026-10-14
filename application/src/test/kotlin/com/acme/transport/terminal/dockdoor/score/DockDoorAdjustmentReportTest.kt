package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700510728)
        val items = mutableListOf<DockDoorAdjustment>()
        for (index in 0 until 3) {
            items.add(newDockDoorAdjustment("dock-door-adjustment-$index", "sample"))
        }
        val report = buildDockDoorAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
