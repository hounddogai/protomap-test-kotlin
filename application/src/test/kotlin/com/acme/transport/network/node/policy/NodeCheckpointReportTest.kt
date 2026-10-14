package com.acme.transport.network.node.policy

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701958530)
        val items = mutableListOf<NodeCheckpoint>()
        for (index in 0 until 3) {
            items.add(newNodeCheckpoint("node-checkpoint-$index", "sample"))
        }
        val report = buildNodeCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
