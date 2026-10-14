package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701070333)
        val items = mutableListOf<AdaptiveNodeMetric>()
        for (index in 0 until 2) {
            items.add(newAdaptiveNodeMetric("adaptive-node-metric-$index", "sample"))
        }
        val report = buildAdaptiveNodeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
