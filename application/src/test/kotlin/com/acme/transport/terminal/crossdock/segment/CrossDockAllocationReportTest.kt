package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702884357)
        val items = mutableListOf<CrossDockAllocation>()
        for (index in 0 until 2) {
            items.add(newCrossDockAllocation("cross-dock-allocation-$index", "sample"))
        }
        val report = buildCrossDockAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
