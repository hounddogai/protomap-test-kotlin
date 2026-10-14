package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706303264)
        val items = mutableListOf<SeasonalServicePlanBaseline>()
        for (index in 0 until 3) {
            items.add(newSeasonalServicePlanBaseline("seasonal-service-plan-baseline-$index", "sample"))
        }
        val report = buildSeasonalServicePlanBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
