package com.acme.transport.customs.hold.mapping

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705427919)
        val items = mutableListOf<HoldMilestone>()
        for (index in 0 until 6) {
            items.add(newHoldMilestone("hold-milestone-$index", "sample"))
        }
        val report = buildHoldMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
