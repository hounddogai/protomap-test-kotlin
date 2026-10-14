package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706893121)
        val items = mutableListOf<CarbonReportTier>()
        for (index in 0 until 2) {
            items.add(newCarbonReportTier("carbon-report-tier-$index", "sample"))
        }
        val report = buildCarbonReportTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
