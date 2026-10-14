package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRouteOffsetSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707408098)
        val items = mutableListOf<SeasonalRouteOffsetSegment>()
        for (index in 0 until 2) {
            items.add(newSeasonalRouteOffsetSegment("seasonal-route-offset-segment-$index", "sample"))
        }
        val report = buildSeasonalRouteOffsetSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
