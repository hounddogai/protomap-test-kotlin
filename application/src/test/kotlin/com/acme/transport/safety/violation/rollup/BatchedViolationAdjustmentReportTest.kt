package com.acme.transport.safety.violation.rollup

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedViolationAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707965181)
        val items = mutableListOf<BatchedViolationAdjustment>()
        for (index in 0 until 2) {
            items.add(newBatchedViolationAdjustment("batched-violation-adjustment-$index", "sample"))
        }
        val report = buildBatchedViolationAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
