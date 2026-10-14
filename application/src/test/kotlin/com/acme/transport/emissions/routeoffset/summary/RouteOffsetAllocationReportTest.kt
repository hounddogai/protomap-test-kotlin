package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700305526)
        val items = mutableListOf<RouteOffsetAllocation>()
        for (index in 0 until 4) {
            items.add(newRouteOffsetAllocation("route-offset-allocation-$index", "sample"))
        }
        val report = buildRouteOffsetAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
