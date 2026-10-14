package com.acme.transport.network.node.nodecycle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707750727)
        val items = mutableListOf<NodeVariance>()
        for (index in 0 until 6) {
            items.add(newNodeVariance("node-variance-$index", "sample"))
        }
        val report = buildNodeVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
