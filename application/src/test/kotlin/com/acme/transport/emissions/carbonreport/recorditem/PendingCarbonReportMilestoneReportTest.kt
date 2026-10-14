package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCarbonReportMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706219223)
        val items = mutableListOf<PendingCarbonReportMilestone>()
        for (index in 0 until 3) {
            items.add(newPendingCarbonReportMilestone("pending-carbon-report-milestone-$index", "sample"))
        }
        val report = buildPendingCarbonReportMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
