package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702095365)
        val items = mutableListOf<RouteOffsetOverride>()
        for (index in 0 until 2) {
            items.add(newRouteOffsetOverride("route-offset-override-$index", "sample"))
        }
        val report = buildRouteOffsetOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
