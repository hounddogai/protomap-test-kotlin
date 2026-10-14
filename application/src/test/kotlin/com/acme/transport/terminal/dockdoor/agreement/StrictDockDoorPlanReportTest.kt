package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDockDoorPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703323133)
        val items = mutableListOf<StrictDockDoorPlan>()
        for (index in 0 until 2) {
            items.add(newStrictDockDoorPlan("strict-dock-door-plan-$index", "sample"))
        }
        val report = buildStrictDockDoorPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
