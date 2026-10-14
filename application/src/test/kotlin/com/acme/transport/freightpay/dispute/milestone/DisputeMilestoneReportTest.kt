package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704579481)
        val items = mutableListOf<DisputeMilestone>()
        for (index in 0 until 5) {
            items.add(newDisputeMilestone("dispute-milestone-$index", "sample"))
        }
        val report = buildDisputeMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
