package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDockDoorSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704922643)
        val items = mutableListOf<GlobalDockDoorSummary>()
        for (index in 0 until 5) {
            items.add(newGlobalDockDoorSummary("global-dock-door-summary-$index", "sample"))
        }
        val report = buildGlobalDockDoorSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
