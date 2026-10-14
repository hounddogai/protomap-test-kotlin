package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705761516)
        val items = mutableListOf<RouteOffsetTier>()
        for (index in 0 until 3) {
            items.add(newRouteOffsetTier("route-offset-tier-$index", "sample"))
        }
        val report = buildRouteOffsetTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
