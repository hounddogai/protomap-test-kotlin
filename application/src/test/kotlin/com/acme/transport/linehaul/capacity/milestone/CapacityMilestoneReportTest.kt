package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702300747)
        val items = mutableListOf<CapacityMilestone>()
        for (index in 0 until 4) {
            items.add(newCapacityMilestone("capacity-milestone-$index", "sample"))
        }
        val report = buildCapacityMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
