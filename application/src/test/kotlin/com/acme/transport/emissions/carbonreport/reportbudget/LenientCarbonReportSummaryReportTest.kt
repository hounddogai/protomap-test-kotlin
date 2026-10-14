package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCarbonReportSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704486051)
        val items = mutableListOf<LenientCarbonReportSummary>()
        for (index in 0 until 2) {
            items.add(newLenientCarbonReportSummary("lenient-carbon-report-summary-$index", "sample"))
        }
        val report = buildLenientCarbonReportSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
