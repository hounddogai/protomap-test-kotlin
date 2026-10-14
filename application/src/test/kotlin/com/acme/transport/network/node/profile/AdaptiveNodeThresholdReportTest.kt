package com.acme.transport.network.node.profile

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701338294)
        val items = mutableListOf<AdaptiveNodeThreshold>()
        for (index in 0 until 6) {
            items.add(newAdaptiveNodeThreshold("adaptive-node-threshold-$index", "sample"))
        }
        val report = buildAdaptiveNodeThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
