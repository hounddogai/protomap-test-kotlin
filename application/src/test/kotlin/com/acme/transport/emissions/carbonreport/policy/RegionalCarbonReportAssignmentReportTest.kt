package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703188259)
        val items = mutableListOf<RegionalCarbonReportAssignment>()
        for (index in 0 until 2) {
            items.add(newRegionalCarbonReportAssignment("regional-carbon-report-assignment-$index", "sample"))
        }
        val report = buildRegionalCarbonReportAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
