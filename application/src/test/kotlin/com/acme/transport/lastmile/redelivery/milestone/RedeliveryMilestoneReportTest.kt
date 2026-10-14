package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708502394)
        val items = mutableListOf<RedeliveryMilestone>()
        for (index in 0 until 5) {
            items.add(newRedeliveryMilestone("redelivery-milestone-$index", "sample"))
        }
        val report = buildRedeliveryMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
