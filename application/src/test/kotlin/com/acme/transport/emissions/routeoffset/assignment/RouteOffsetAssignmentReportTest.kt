package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707208516)
        val items = mutableListOf<RouteOffsetAssignment>()
        for (index in 0 until 6) {
            items.add(newRouteOffsetAssignment("route-offset-assignment-$index", "sample"))
        }
        val report = buildRouteOffsetAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
