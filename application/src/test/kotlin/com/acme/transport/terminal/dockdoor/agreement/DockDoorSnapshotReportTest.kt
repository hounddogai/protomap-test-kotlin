package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709437009)
        val items = mutableListOf<DockDoorSnapshot>()
        for (index in 0 until 2) {
            items.add(newDockDoorSnapshot("dock-door-snapshot-$index", "sample"))
        }
        val report = buildDockDoorSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
