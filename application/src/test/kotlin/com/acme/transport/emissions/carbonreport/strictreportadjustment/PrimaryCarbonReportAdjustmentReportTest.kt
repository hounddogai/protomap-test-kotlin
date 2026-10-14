package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCarbonReportAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708071739)
        val items = mutableListOf<PrimaryCarbonReportAdjustment>()
        for (index in 0 until 3) {
            items.add(newPrimaryCarbonReportAdjustment("primary-carbon-report-adjustment-$index", "sample"))
        }
        val report = buildPrimaryCarbonReportAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
