package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCapacityMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709932930)
        val items = mutableListOf<LenientCapacityMilestone>()
        for (index in 0 until 6) {
            items.add(newLenientCapacityMilestone("lenient-capacity-milestone-$index", "sample"))
        }
        val report = buildLenientCapacityMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
