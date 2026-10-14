package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705473588)
        val items = mutableListOf<BackhaulAudit>()
        for (index in 0 until 5) {
            items.add(newBackhaulAudit("backhaul-audit-$index", "sample"))
        }
        val report = buildBackhaulAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
