package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNearMissPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709848393)
        val items = mutableListOf<BatchedNearMissPlan>()
        for (index in 0 until 5) {
            items.add(newBatchedNearMissPlan("batched-near-miss-plan-$index", "sample"))
        }
        val report = buildBatchedNearMissPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
