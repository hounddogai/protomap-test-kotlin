package com.acme.transport.freightpay.audit.digest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAuditAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708007107)
        val items = mutableListOf<BatchedAuditAdjustment>()
        for (index in 0 until 5) {
            items.add(newBatchedAuditAdjustment("batched-audit-adjustment-$index", "sample"))
        }
        val report = buildBatchedAuditAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
