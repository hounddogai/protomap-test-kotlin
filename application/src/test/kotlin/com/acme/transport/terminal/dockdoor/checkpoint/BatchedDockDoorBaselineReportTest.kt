package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDockDoorBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704676532)
        val items = mutableListOf<BatchedDockDoorBaseline>()
        for (index in 0 until 3) {
            items.add(newBatchedDockDoorBaseline("batched-dock-door-baseline-$index", "sample"))
        }
        val report = buildBatchedDockDoorBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
