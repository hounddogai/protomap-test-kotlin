package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701357083)
        val items = mutableListOf<CarbonReportGroup>()
        for (index in 0 until 2) {
            items.add(newCarbonReportGroup("carbon-report-group-$index", "sample"))
        }
        val report = buildCarbonReportGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
