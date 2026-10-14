package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701471123)
        val items = mutableListOf<DockDoorMilestone>()
        for (index in 0 until 2) {
            items.add(newDockDoorMilestone("dock-door-milestone-$index", "sample"))
        }
        val report = buildDockDoorMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
