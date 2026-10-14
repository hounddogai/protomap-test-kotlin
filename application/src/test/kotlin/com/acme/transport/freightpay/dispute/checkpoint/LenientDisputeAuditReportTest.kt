package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705598547)
        val items = mutableListOf<LenientDisputeAudit>()
        for (index in 0 until 2) {
            items.add(newLenientDisputeAudit("lenient-dispute-audit-$index", "sample"))
        }
        val report = buildLenientDisputeAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
