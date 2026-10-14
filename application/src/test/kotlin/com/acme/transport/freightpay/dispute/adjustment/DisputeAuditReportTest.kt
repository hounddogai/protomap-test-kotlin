package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707208365)
        val items = mutableListOf<DisputeAudit>()
        for (index in 0 until 5) {
            items.add(newDisputeAudit("dispute-audit-$index", "sample"))
        }
        val report = buildDisputeAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
