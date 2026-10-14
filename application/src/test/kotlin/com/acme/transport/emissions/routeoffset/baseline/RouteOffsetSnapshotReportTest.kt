package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701614664)
        val items = mutableListOf<RouteOffsetSnapshot>()
        for (index in 0 until 5) {
            items.add(newRouteOffsetSnapshot("route-offset-snapshot-$index", "sample"))
        }
        val report = buildRouteOffsetSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
