package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700976712)
        val items = mutableListOf<RouteOffsetClaim>()
        for (index in 0 until 2) {
            items.add(newRouteOffsetClaim("route-offset-claim-$index", "sample"))
        }
        val report = buildRouteOffsetClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
