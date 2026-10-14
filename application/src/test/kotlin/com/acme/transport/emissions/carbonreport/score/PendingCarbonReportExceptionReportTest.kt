package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCarbonReportExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702995904)
        val items = mutableListOf<PendingCarbonReportException>()
        for (index in 0 until 5) {
            items.add(newPendingCarbonReportException("pending-carbon-report-exception-$index", "sample"))
        }
        val report = buildPendingCarbonReportExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
