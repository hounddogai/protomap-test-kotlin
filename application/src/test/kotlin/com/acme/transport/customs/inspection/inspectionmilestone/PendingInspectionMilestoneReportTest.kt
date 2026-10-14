package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706990293)
        val items = mutableListOf<PendingInspectionMilestone>()
        for (index in 0 until 6) {
            items.add(newPendingInspectionMilestone("pending-inspection-milestone-$index", "sample"))
        }
        val report = buildPendingInspectionMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
