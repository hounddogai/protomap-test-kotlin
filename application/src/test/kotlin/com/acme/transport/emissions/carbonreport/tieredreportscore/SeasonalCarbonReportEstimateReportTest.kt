package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCarbonReportEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707827979)
        val items = mutableListOf<SeasonalCarbonReportEstimate>()
        for (index in 0 until 4) {
            items.add(newSeasonalCarbonReportEstimate("seasonal-carbon-report-estimate-$index", "sample"))
        }
        val report = buildSeasonalCarbonReportEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
