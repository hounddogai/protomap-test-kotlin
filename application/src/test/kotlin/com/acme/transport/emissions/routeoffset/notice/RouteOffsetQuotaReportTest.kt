package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700157140)
        val items = mutableListOf<RouteOffsetQuota>()
        for (index in 0 until 2) {
            items.add(newRouteOffsetQuota("route-offset-quota-$index", "sample"))
        }
        val report = buildRouteOffsetQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
