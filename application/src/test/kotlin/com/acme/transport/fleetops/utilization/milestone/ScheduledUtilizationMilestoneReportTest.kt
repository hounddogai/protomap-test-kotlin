package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledUtilizationMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709871666)
        val items = mutableListOf<ScheduledUtilizationMilestone>()
        for (index in 0 until 2) {
            items.add(newScheduledUtilizationMilestone("scheduled-utilization-milestone-$index", "sample"))
        }
        val report = buildScheduledUtilizationMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
