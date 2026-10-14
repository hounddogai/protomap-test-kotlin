package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRouteOffsetMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701880970)
        val items = mutableListOf<CappedRouteOffsetMetric>()
        for (index in 0 until 5) {
            items.add(newCappedRouteOffsetMetric("capped-route-offset-metric-$index", "sample"))
        }
        val report = buildCappedRouteOffsetMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
