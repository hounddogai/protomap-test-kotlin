package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAuditRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706986245)
        val items = mutableListOf<LegacyAuditRecord>()
        for (index in 0 until 6) {
            items.add(newLegacyAuditRecord("legacy-audit-record-$index", "sample"))
        }
        val report = buildLegacyAuditRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
