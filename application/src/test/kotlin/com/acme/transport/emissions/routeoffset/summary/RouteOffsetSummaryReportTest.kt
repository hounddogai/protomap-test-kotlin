package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701382452)
        val items = mutableListOf<RouteOffsetSummary>()
        for (index in 0 until 5) {
            items.add(newRouteOffsetSummary("route-offset-summary-$index", "sample"))
        }
        val report = buildRouteOffsetSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
