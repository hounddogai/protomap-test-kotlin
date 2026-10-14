package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCarbonReportAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706474282)
        val items = mutableListOf<RollingCarbonReportAssignment>()
        for (index in 0 until 2) {
            items.add(newRollingCarbonReportAssignment("rolling-carbon-report-assignment-$index", "sample"))
        }
        val report = buildRollingCarbonReportAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
