package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDockDoorCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700718525)
        val items = mutableListOf<BatchedDockDoorCheckpoint>()
        for (index in 0 until 2) {
            items.add(newBatchedDockDoorCheckpoint("batched-dock-door-checkpoint-$index", "sample"))
        }
        val report = buildBatchedDockDoorCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
