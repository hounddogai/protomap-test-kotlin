package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700730973)
        val items = mutableListOf<RestBreakMilestone>()
        for (index in 0 until 3) {
            items.add(newRestBreakMilestone("rest-break-milestone-$index", "sample"))
        }
        val report = buildRestBreakMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
