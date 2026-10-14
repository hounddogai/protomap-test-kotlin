package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRouteOffsetAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707373843)
        val items = mutableListOf<SeasonalRouteOffsetAdjustment>()
        for (index in 0 until 6) {
            items.add(newSeasonalRouteOffsetAdjustment("seasonal-route-offset-adjustment-$index", "sample"))
        }
        val report = buildSeasonalRouteOffsetAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
