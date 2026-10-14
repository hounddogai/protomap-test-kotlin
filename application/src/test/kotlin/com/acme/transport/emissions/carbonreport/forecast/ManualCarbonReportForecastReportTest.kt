package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCarbonReportForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706481936)
        val items = mutableListOf<ManualCarbonReportForecast>()
        for (index in 0 until 6) {
            items.add(newManualCarbonReportForecast("manual-carbon-report-forecast-$index", "sample"))
        }
        val report = buildManualCarbonReportForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
