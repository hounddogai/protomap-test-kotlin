package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCarbonReportVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702747113)
        val items = mutableListOf<TieredCarbonReportVariance>()
        for (index in 0 until 6) {
            items.add(newTieredCarbonReportVariance("tiered-carbon-report-variance-$index", "sample"))
        }
        val report = buildTieredCarbonReportVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
