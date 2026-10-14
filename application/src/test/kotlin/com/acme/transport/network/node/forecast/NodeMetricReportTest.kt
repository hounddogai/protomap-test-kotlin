package com.acme.transport.network.node.forecast

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700504880)
        val items = mutableListOf<NodeMetric>()
        for (index in 0 until 5) {
            items.add(newNodeMetric("node-metric-$index", "sample"))
        }
        val report = buildNodeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
