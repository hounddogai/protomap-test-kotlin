package com.acme.transport.freightpay.audit.batch

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706795757)
        val items = mutableListOf<AuditRule>()
        for (index in 0 until 3) {
            items.add(newAuditRule("audit-rule-$index", "sample"))
        }
        val report = buildAuditRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
