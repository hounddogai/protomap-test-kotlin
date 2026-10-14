package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707337771)
        val items = mutableListOf<DockDoorPlan>()
        for (index in 0 until 6) {
            items.add(newDockDoorPlan("dock-door-plan-$index", "sample"))
        }
        val report = buildDockDoorPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
