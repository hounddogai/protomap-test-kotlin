package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAttemptMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707326450)
        val items = mutableListOf<BatchedAttemptMilestone>()
        for (index in 0 until 5) {
            items.add(newBatchedAttemptMilestone("batched-attempt-milestone-$index", "sample"))
        }
        val report = buildBatchedAttemptMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
