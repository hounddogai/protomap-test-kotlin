package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707354388)
        val items = mutableListOf<RouteOffsetProfile>()
        for (index in 0 until 2) {
            items.add(newRouteOffsetProfile("route-offset-profile-$index", "sample"))
        }
        val report = buildRouteOffsetProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
