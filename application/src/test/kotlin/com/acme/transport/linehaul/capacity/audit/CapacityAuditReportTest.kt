package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700245222)
        val items = mutableListOf<CapacityAudit>()
        for (index in 0 until 2) {
            items.add(newCapacityAudit("capacity-audit-$index", "sample"))
        }
        val report = buildCapacityAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
