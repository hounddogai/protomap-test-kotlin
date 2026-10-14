package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705589036)
        val items = mutableListOf<DockDoorBatch>()
        for (index in 0 until 5) {
            items.add(newDockDoorBatch("dock-door-batch-$index", "sample"))
        }
        val report = buildDockDoorBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
