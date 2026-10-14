package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRouteOffsetThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701251216)
        val items = mutableListOf<SeasonalRouteOffsetThreshold>()
        for (index in 0 until 3) {
            items.add(newSeasonalRouteOffsetThreshold("seasonal-route-offset-threshold-$index", "sample"))
        }
        val report = buildSeasonalRouteOffsetThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
