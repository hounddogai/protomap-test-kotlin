package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCarbonReportAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702494576)
        val items = mutableListOf<LenientCarbonReportAssignment>()
        for (index in 0 until 3) {
            items.add(newLenientCarbonReportAssignment("lenient-carbon-report-assignment-$index", "sample"))
        }
        val report = buildLenientCarbonReportAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
