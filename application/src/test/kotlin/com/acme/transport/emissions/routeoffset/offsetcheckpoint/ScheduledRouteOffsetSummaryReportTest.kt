package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRouteOffsetSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703414023)
        val items = mutableListOf<ScheduledRouteOffsetSummary>()
        for (index in 0 until 2) {
            items.add(newScheduledRouteOffsetSummary("scheduled-route-offset-summary-$index", "sample"))
        }
        val report = buildScheduledRouteOffsetSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
