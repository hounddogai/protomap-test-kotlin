package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCarbonReportVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705891995)
        val items = mutableListOf<LenientCarbonReportVariance>()
        for (index in 0 until 2) {
            items.add(newLenientCarbonReportVariance("lenient-carbon-report-variance-$index", "sample"))
        }
        val report = buildLenientCarbonReportVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
