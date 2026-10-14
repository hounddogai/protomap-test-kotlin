package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700095923)
        val items = mutableListOf<CarbonReportEstimate>()
        for (index in 0 until 5) {
            items.add(newCarbonReportEstimate("carbon-report-estimate-$index", "sample"))
        }
        val report = buildCarbonReportEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
