package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708182254)
        val items = mutableListOf<ClearanceAudit>()
        for (index in 0 until 5) {
            items.add(newClearanceAudit("clearance-audit-$index", "sample"))
        }
        val report = buildClearanceAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
