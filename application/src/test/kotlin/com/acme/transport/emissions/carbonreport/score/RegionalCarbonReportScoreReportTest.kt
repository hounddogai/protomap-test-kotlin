package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709669696)
        val items = mutableListOf<RegionalCarbonReportScore>()
        for (index in 0 until 5) {
            items.add(newRegionalCarbonReportScore("regional-carbon-report-score-$index", "sample"))
        }
        val report = buildRegionalCarbonReportScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
