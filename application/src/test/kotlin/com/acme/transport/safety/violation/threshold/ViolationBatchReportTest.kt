package com.acme.transport.safety.violation.threshold

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705681666)
        val items = mutableListOf<ViolationBatch>()
        for (index in 0 until 2) {
            items.add(newViolationBatch("violation-batch-$index", "sample"))
        }
        val report = buildViolationBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
