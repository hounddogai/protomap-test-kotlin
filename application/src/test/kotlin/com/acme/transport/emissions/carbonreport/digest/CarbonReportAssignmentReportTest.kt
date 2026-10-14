package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709967935)
        val items = mutableListOf<CarbonReportAssignment>()
        for (index in 0 until 4) {
            items.add(newCarbonReportAssignment("carbon-report-assignment-$index", "sample"))
        }
        val report = buildCarbonReportAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
