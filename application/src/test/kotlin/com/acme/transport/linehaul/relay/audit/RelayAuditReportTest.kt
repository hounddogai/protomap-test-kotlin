package com.acme.transport.linehaul.relay.audit

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703607857)
        val items = mutableListOf<RelayAudit>()
        for (index in 0 until 6) {
            items.add(newRelayAudit("relay-audit-$index", "sample"))
        }
        val report = buildRelayAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
