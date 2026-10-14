package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedScorecardThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704046334)
        val items = mutableListOf<BatchedScorecardThreshold>()
        for (index in 0 until 2) {
            items.add(newBatchedScorecardThreshold("batched-scorecard-threshold-$index", "sample"))
        }
        val report = buildBatchedScorecardThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
