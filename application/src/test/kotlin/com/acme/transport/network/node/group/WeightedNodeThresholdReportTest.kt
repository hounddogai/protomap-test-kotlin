package com.acme.transport.network.node.group

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedNodeThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709525488)
        val items = mutableListOf<WeightedNodeThreshold>()
        for (index in 0 until 3) {
            items.add(newWeightedNodeThreshold("weighted-node-threshold-$index", "sample"))
        }
        val report = buildWeightedNodeThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
