package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNearMissSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707222225)
        val items = mutableListOf<BatchedNearMissSegment>()
        for (index in 0 until 2) {
            items.add(newBatchedNearMissSegment("batched-near-miss-segment-$index", "sample"))
        }
        val report = buildBatchedNearMissSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
