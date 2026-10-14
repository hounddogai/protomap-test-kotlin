package com.acme.transport.network.node.nodeoverride

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705824718)
        val items = mutableListOf<AdaptiveNodeWindow>()
        for (index in 0 until 4) {
            items.add(newAdaptiveNodeWindow("adaptive-node-window-$index", "sample"))
        }
        val report = buildAdaptiveNodeWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
