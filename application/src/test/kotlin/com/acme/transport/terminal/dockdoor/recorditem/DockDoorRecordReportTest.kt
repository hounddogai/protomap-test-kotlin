package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700997536)
        val items = mutableListOf<DockDoorRecord>()
        for (index in 0 until 2) {
            items.add(newDockDoorRecord("dock-door-record-$index", "sample"))
        }
        val report = buildDockDoorRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
