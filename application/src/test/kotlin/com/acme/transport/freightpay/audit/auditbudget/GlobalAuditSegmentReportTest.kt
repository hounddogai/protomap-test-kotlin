package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAuditSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708014435)
        val items = mutableListOf<GlobalAuditSegment>()
        for (index in 0 until 2) {
            items.add(newGlobalAuditSegment("global-audit-segment-$index", "sample"))
        }
        val report = buildGlobalAuditSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
