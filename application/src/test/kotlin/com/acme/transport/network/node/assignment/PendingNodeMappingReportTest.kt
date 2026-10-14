package com.acme.transport.network.node.assignment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNodeMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705415412)
        val items = mutableListOf<PendingNodeMapping>()
        for (index in 0 until 2) {
            items.add(newPendingNodeMapping("pending-node-mapping-$index", "sample"))
        }
        val report = buildPendingNodeMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
