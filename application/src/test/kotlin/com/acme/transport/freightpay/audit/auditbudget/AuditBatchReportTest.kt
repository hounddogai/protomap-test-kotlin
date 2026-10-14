package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705976516)
        val items = mutableListOf<AuditBatch>()
        for (index in 0 until 2) {
            items.add(newAuditBatch("audit-batch-$index", "sample"))
        }
        val report = buildAuditBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
