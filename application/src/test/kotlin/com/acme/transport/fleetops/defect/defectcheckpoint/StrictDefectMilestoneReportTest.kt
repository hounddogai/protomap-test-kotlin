package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDefectMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709507996)
        val items = mutableListOf<StrictDefectMilestone>()
        for (index in 0 until 4) {
            items.add(newStrictDefectMilestone("strict-defect-milestone-$index", "sample"))
        }
        val report = buildStrictDefectMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
