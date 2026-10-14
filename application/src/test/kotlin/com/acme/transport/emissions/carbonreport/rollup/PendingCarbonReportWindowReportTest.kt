package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCarbonReportWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704206843)
        val items = mutableListOf<PendingCarbonReportWindow>()
        for (index in 0 until 4) {
            items.add(newPendingCarbonReportWindow("pending-carbon-report-window-$index", "sample"))
        }
        val report = buildPendingCarbonReportWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
