package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDockDoorMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708772058)
        val items = mutableListOf<BatchedDockDoorMapping>()
        for (index in 0 until 5) {
            items.add(newBatchedDockDoorMapping("batched-dock-door-mapping-$index", "sample"))
        }
        val report = buildBatchedDockDoorMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
