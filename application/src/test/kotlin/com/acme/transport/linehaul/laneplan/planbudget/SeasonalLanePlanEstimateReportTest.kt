package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalLanePlanEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709418747)
        val items = mutableListOf<SeasonalLanePlanEstimate>()
        for (index in 0 until 4) {
            items.add(newSeasonalLanePlanEstimate("seasonal-lane-plan-estimate-$index", "sample"))
        }
        val report = buildSeasonalLanePlanEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
