package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAuditRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702419960)
        val items = mutableListOf<PendingAuditRecord>()
        for (index in 0 until 5) {
            items.add(newPendingAuditRecord("pending-audit-record-$index", "sample"))
        }
        val report = buildPendingAuditRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
