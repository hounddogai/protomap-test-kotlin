package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEvChargeScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706605228)
        val items = mutableListOf<BatchedEvChargeScore>()
        for (index in 0 until 2) {
            items.add(newBatchedEvChargeScore("batched-ev-charge-score-$index", "sample"))
        }
        val report = buildBatchedEvChargeScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
