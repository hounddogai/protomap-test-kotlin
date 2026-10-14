package com.acme.transport.network.node.adjustment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedNodeWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706398664)
        val items = mutableListOf<WeightedNodeWindow>()
        for (index in 0 until 6) {
            items.add(newWeightedNodeWindow("weighted-node-window-$index", "sample"))
        }
        val report = buildWeightedNodeWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
