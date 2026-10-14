package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709043891)
        val items = mutableListOf<RouteOffsetPolicy>()
        for (index in 0 until 4) {
            items.add(newRouteOffsetPolicy("route-offset-policy-$index", "sample"))
        }
        val report = buildRouteOffsetPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
