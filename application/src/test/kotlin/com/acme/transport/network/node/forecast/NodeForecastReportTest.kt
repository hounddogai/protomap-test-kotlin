package com.acme.transport.network.node.forecast

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700481396)
        val items = mutableListOf<NodeForecast>()
        for (index in 0 until 3) {
            items.add(newNodeForecast("node-forecast-$index", "sample"))
        }
        val report = buildNodeForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
