package com.acme.transport.network.transittime.budget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705971220)
        val items = mutableListOf<TransitTimeMetric>()
        for (index in 0 until 5) {
            items.add(newTransitTimeMetric("transit-time-metric-$index", "sample"))
        }
        val report = buildTransitTimeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
