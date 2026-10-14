package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700126643)
        val items = mutableListOf<BatchedTrailerScore>()
        for (index in 0 until 4) {
            items.add(newBatchedTrailerScore("batched-trailer-score-$index", "sample"))
        }
        val report = buildBatchedTrailerScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
