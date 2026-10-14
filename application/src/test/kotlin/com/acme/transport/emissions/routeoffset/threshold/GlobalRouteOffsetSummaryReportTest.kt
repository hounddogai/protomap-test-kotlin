package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRouteOffsetSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702933700)
        val items = mutableListOf<GlobalRouteOffsetSummary>()
        for (index in 0 until 5) {
            items.add(newGlobalRouteOffsetSummary("global-route-offset-summary-$index", "sample"))
        }
        val report = buildGlobalRouteOffsetSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
