package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704446936)
        val items = mutableListOf<RouteOffsetEstimate>()
        for (index in 0 until 6) {
            items.add(newRouteOffsetEstimate("route-offset-estimate-$index", "sample"))
        }
        val report = buildRouteOffsetEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
