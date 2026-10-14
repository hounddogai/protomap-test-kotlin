package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709795967)
        val items = mutableListOf<CarbonReportQuota>()
        for (index in 0 until 2) {
            items.add(newCarbonReportQuota("carbon-report-quota-$index", "sample"))
        }
        val report = buildCarbonReportQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
