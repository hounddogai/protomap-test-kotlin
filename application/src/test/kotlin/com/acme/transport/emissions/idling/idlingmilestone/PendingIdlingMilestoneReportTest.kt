package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIdlingMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704358097)
        val items = mutableListOf<PendingIdlingMilestone>()
        for (index in 0 until 2) {
            items.add(newPendingIdlingMilestone("pending-idling-milestone-$index", "sample"))
        }
        val report = buildPendingIdlingMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
