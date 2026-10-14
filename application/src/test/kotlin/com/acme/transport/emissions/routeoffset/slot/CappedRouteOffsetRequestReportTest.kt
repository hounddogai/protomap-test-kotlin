package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRouteOffsetRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706126499)
        val items = mutableListOf<CappedRouteOffsetRequest>()
        for (index in 0 until 5) {
            items.add(newCappedRouteOffsetRequest("capped-route-offset-request-$index", "sample"))
        }
        val report = buildCappedRouteOffsetRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
