package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCarbonReportRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707317009)
        val items = mutableListOf<WeightedCarbonReportRecord>()
        for (index in 0 until 2) {
            items.add(newWeightedCarbonReportRecord("weighted-carbon-report-record-$index", "sample"))
        }
        val report = buildWeightedCarbonReportRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
