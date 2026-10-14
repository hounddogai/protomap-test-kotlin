package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700667996)
        val items = mutableListOf<AvailabilityMilestone>()
        for (index in 0 until 4) {
            items.add(newAvailabilityMilestone("availability-milestone-$index", "sample"))
        }
        val report = buildAvailabilityMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
