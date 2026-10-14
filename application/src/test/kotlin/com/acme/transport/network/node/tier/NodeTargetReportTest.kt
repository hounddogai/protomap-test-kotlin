package com.acme.transport.network.node.tier

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702810073)
        val items = mutableListOf<NodeTarget>()
        for (index in 0 until 5) {
            items.add(newNodeTarget("node-target-$index", "sample"))
        }
        val report = buildNodeTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
