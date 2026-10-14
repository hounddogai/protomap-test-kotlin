package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRouteOffsetSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709320232)
        val items = mutableListOf<ScheduledRouteOffsetSnapshot>()
        for (index in 0 until 4) {
            items.add(newScheduledRouteOffsetSnapshot("scheduled-route-offset-snapshot-$index", "sample"))
        }
        val report = buildScheduledRouteOffsetSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
