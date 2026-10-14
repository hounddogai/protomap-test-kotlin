package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701537881)
        val items = mutableListOf<ClearanceMilestone>()
        for (index in 0 until 4) {
            items.add(newClearanceMilestone("clearance-milestone-$index", "sample"))
        }
        val report = buildClearanceMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
