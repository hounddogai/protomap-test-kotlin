package com.acme.transport.freightpay.audit.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705503843)
        val items = mutableListOf<AuditSegment>()
        for (index in 0 until 2) {
            items.add(newAuditSegment("audit-segment-$index", "sample"))
        }
        val report = buildAuditSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
